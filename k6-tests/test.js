import http from 'k6/http';
import { check, sleep, group } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 500 },  
        { duration: '30s', target: 500 }, 
        { duration: '10s', target: 0 },    
    ],
};

const BASE_URL = 'http://backend:8080';

export default function () {
    let userId;
    let csrfToken = '';
    // Chọn ngẫu nhiên sản phẩm từ P001 đến P010 để tránh hết hàng quá nhanh
    const productIds = ['P001', 'P002', 'P003', 'P004', 'P005', 'P006', 'P007', 'P008', 'P009', 'P010'];
    let productId = productIds[Math.floor(Math.random() * productIds.length)];

    group('1. Authentication', function () {
        let loginPayload = JSON.stringify({
            username: 'customer1',
            password: 'password123'
        });

        let loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
            headers: { 'Content-Type': 'application/json' },
        });

        let loginOk = check(loginRes, {
            'Login success (200)': (r) => r.status === 200,
        });

        if (loginOk) {
            // Tạo userId duy nhất cho mỗi VU để tránh tranh chấp giỏ hàng
            userId = "user-" + loginRes.json().userId + "-" + __VU;
        }
        
        if (loginRes.cookies['XSRF-TOKEN']) {
            csrfToken = loginRes.cookies['XSRF-TOKEN'][0].value;
        } else if (loginRes.headers['X-XSRF-TOKEN']) {
            csrfToken = loginRes.headers['X-XSRF-TOKEN'];
        }
    });

    if (userId) {
        group('2. Add to Cart', function () {
            let cartPayload = JSON.stringify({
                productId: productId,
                quantity: 1
            });

            let cartRes = http.post(`${BASE_URL}/api/cart/${userId}/add`, cartPayload, {
                headers: { 
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
            });

            check(cartRes, {
                'Add to cart success (201)': (r) => r.status === 201 || r.status === 200,
            });
        });

        group('3. Create Order', function () {
            let orderPayload = JSON.stringify({
                userId: userId.toString(),
                orderItems: [
                    {
                        productId: productId,
                        quantity: 1,
                        price: 100000 // Giả định giá
                    }
                ],
                shippingAddress: '123 Test St',
                city: 'Ho Chi Minh',
                district: 'District 1',
                ward: 'Ben Nghe',
                phoneNumber: '0123456789'
            });

            let orderRes = http.post(`${BASE_URL}/api/orders/${userId}`, orderPayload, {
                headers: { 
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
            });

            check(orderRes, {
                'Create order success (201)': (r) => r.status === 201 || r.status === 200,
            });

            if (orderRes.status !== 201 && orderRes.status !== 200) {
                console.log(`Order failed! Status: ${orderRes.status}, Body: ${orderRes.body}`);
            }
        });
    }

    sleep(1);
}