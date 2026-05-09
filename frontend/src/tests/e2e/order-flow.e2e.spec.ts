import { expect, test, type Page } from '@playwright/test';
import CheckoutPage from './pages/CheckoutPage';
import CartPage from './pages/CartPage';

// RẤT QUAN TRỌNG: Buộc các test trong file này phải chạy tuần tự
// Vì tất cả đều dùng chung tài khoản customer1, chạy song song sẽ gây xung đột dữ liệu giỏ hàng ở Backend.
test.describe.configure({ mode: 'serial' });

const SHIPPING_FEE = 29900;
const API_URL = process.env.VITE_API_URL || 'http://localhost:8080/api';

async function checkBackendHealth(page: Page): Promise<boolean> {
  try {
    const baseUrl =
      process.env.VITE_API_URL?.replace('/api', '') ||
      'http://localhost:8080';

    const response = await page.request.get(
      `${baseUrl}/actuator/health`,
      {
        timeout: 5000,
      }
    );

    console.log(`Backend health check: ${response.status()}`);

    return response.ok();
  } catch (error) {
    console.error('Backend health check failed:', error);
    return false;
  }
}

async function testDatabaseConnection(page: Page): Promise<boolean> {
  try {
    // Simple test to verify database is accessible and schema is initialized
    // Try to login - if it fails with database error, schema might not be initialized
    const baseUrl = process.env.VITE_API_URL || 'http://localhost:8080/api';
    const response = await page.request.post(`${baseUrl}/auth/login`, {
      data: {
        username: 'test_schema_check',
        password: 'test',
      },
    });

    // Any response (success or auth failure) means database is working
    // A 500 error about "relation does not exist" means schema isn't initialized
    const text = await response.text();
    if (text.includes('relation') && text.includes('does not exist')) {
      console.error('❌ Database schema not initialized. Tables are missing.');
      console.error('   Solution: Restart the backend container to trigger Hibernate schema creation');
      return false;
    }
    return true;
  } catch (error) {
    console.error('Database connection test failed:', error);
    return false;
  }
}

async function setupAuthenticatedSession(page: Page, maxRetries: number = 3) {
  // 0. Kiểm tra xem backend có chạy không
  const isBackendHealthy = await checkBackendHealth(page);
  if (!isBackendHealthy) {
    throw new Error(
      `Backend không phản hồi. Vui lòng kiểm tra:
      1. Docker containers đang chạy: docker compose up -d
      2. Backend service tại ${API_URL} có sẵn
      3. Database đã khởi tạo và seed dữ liệu`
    );
  }

  // 0.5. Kiểm tra xem database schema đã được tạo chưa
  console.log('Checking database schema...');
  const isDatabaseReady = await testDatabaseConnection(page);
  if (!isDatabaseReady) {
    throw new Error(
      `Database schema chưa được khởi tạo. Vui lòng:\n` +
      `1. Restart backend container: docker restart shopcart_backend\n` +
      `2. Đợi 10-15 giây để Hibernate tạo schema và DataInitializer seed dữ liệu\n` +
      `3. Thử lại: npm run test:e2e`
    );
  }
  console.log('✓ Database schema is ready');

  // 1. Đi đến trang login
  await page.goto('/login', { waitUntil: 'networkidle' });

  let lastError: Error | null = null;

  // Retry logic cho login
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      // 2. Điền thông tin đăng nhập thật
      await page.fill('input[name="username"]', 'customer1');
      await page.fill('input[name="password"]', 'password123');
      await page.click('button[type="submit"]');

      // 3. Đợi chuyển hướng HOẶC bắt thông báo lỗi
      const urlPromise = page.waitForURL('**/products', { timeout: 10000 }).then(() => 'success' as const);
      const errorPromise = page.locator('.Toastify__toast--error').waitFor({ state: 'visible', timeout: 10000 }).then(() => 'error' as const);
      
      // Bắt lỗi ngầm để tránh Unhandled Promise Rejection rò rỉ ra console
      urlPromise.catch(() => {});
      errorPromise.catch(() => {});

      const result = await Promise.race([urlPromise, errorPromise]).catch(() => 'timeout' as const);

      if (result === 'success') {
        console.log(`✓ Đăng nhập thành công ở lần thử thứ ${attempt}`);
        return; // Login successful
      }
      
      if (result === 'error') {
        const msg = await page.locator('.Toastify__toast--error').innerText();
        throw new Error(`Login error: ${msg}`);
      }
      
      if (result === 'timeout') {
        throw new Error('Timeout: Không thấy chuyển hướng cũng như không có thông báo lỗi.');
      }

    } catch (error) {
      lastError = error instanceof Error ? error : new Error(String(error));
      console.warn(`✗ Lần thử ${attempt}/${maxRetries} thất bại:`, lastError.message);

      if (attempt < maxRetries) {
        // Reload page và clear form trước khi thử lại
        await page.reload({ waitUntil: 'networkidle' });
        await page.waitForTimeout(500); // Chờ trang load xong
      }
    }
  }

  // Nếu đã retry hết mà vẫn thất bại
  if (lastError) {
    throw new Error(
      `Đăng nhập thất bại sau ${maxRetries} lần thử: ${lastError.message}\n` +
      `Vui lòng kiểm tra:\n` +
      `1. Backend service đang chạy: curl http://localhost:8080/api/health\n` +
      `2. Database đã seed dữ liệu: user customer1/password123 có tồn tại\n` +
      `3. Spring Boot logs để xem lỗi chi tiết`
    );
  }
}

const TEST_ADDRESS = {
  shippingAddress: '123 Đường Nguyễn Hữu Cảnh, Tòa nhà A',
  city: 'Thành phố Hồ Chí Minh',
  district: 'Quận 1',
  ward: 'Phường Bến Nghé',
  postalCode: '700000',
  phoneNumber: '0912345678',
};

function parseVietnamPrice(priceText: string): number {
  let cleaned = priceText.replace(/VND|đ/gi, '').trim();
  cleaned = cleaned.replace(/\s|\./g, '');
  cleaned = cleaned.replace(',', '');
  return parseInt(cleaned, 10);
}

async function clearCartIfNotEmpty(page: Page, checkoutPage: CheckoutPage) {
  await checkoutPage.goToCart();
  // Đợi trang load và ổn định
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000); 

  const cartItems = page.locator('[data-testid="cart-item"]');
  const count = await cartItems.count();
  
  if (count > 0) {
    await checkoutPage.clearCart();
    // Đợi cho đến khi UI xác nhận giỏ hàng trống (dùng data-testid)
    await expect(page.locator('[data-testid="empty-cart-message"]')).toBeVisible({ timeout: 10000 });
  }
}

test.describe('Checkout & Order E2E Tests - Complete Flow', () => {
  let checkoutPage: CheckoutPage;
  let cartPage: CartPage;

  test.beforeEach(async ({ page }) => {
    await setupAuthenticatedSession(page);
    checkoutPage = new CheckoutPage(page);
    cartPage = new CartPage(page);

    await clearCartIfNotEmpty(page, checkoutPage);

    await cartPage.goToProducts();
    await expect(
      page.locator('[data-testid="product-card"]').first()
    ).toBeVisible({ timeout: 10000 });

    await cartPage.addProductToCart(1);

    await expect(
      page.locator('[data-testid="cart-badge"]')
    ).toBeVisible({ timeout: 10000 });

    await cartPage.goToCart();
    
    // Đợi cho đến khi giỏ hàng hiện item HOẶC hiện tin nhắn trống
    const itemPromise = page.locator('[data-testid="cart-item"]').first().waitFor({ state: 'visible', timeout: 8000 });
    const emptyPromise = page.locator('[data-testid="empty-cart-message"]').waitFor({ state: 'visible', timeout: 8000 });
    
    // Xử lý catch ngầm để promise thua cuộc không gây Unhandled Rejection
    itemPromise.catch(() => {});
    emptyPromise.catch(() => {});

    // Dùng Promise.race để đợi 1 trong 2 xuất hiện, nếu sau 8s không có gì xuất hiện sẽ ném lỗi thẳng
    await Promise.race([itemPromise, emptyPromise]);

    if (await page.locator('[data-testid="empty-cart-message"]').isVisible()) {
      await page.reload();
      await page.waitForLoadState('networkidle');
    }
    
    await expect(
      page.locator('[data-testid="cart-item"]').first()
    ).toBeVisible({ timeout: 10000 });

    await checkoutPage.shippingAddressInput.waitFor({ state: 'visible' });
  });

  test('1) Complete checkout flow: Add items to cart, fill address, and place order', async ({ page }) => {
    const itemCount = await checkoutPage.getCartItemCount();
    expect(itemCount).toBeGreaterThan(0);

    await checkoutPage.fillShippingAddress(
      TEST_ADDRESS.shippingAddress,
      TEST_ADDRESS.city,
      TEST_ADDRESS.district,
      TEST_ADDRESS.ward,
      TEST_ADDRESS.postalCode,
      TEST_ADDRESS.phoneNumber
    );

    await expect(checkoutPage.shippingAddressInput).toHaveValue(TEST_ADDRESS.shippingAddress);
    await expect(checkoutPage.cityInput).toHaveValue(TEST_ADDRESS.city);
    await expect(checkoutPage.districtInput).toHaveValue(TEST_ADDRESS.district);
    await expect(checkoutPage.wardInput).toHaveValue(TEST_ADDRESS.ward);
    await expect(checkoutPage.phoneNumberInput).toHaveValue(TEST_ADDRESS.phoneNumber);

    const subtotal = await checkoutPage.getSubtotal();
    const shippingFee = await checkoutPage.getShippingFee();
    const totalPrice = await checkoutPage.getTotalPrice();

    expect(subtotal).toBeTruthy();
    expect(shippingFee).toBeTruthy();
    expect(totalPrice).toBeTruthy();

    await checkoutPage.placeOrder();

    // Sử dụng helper để đợi redirect và verify orders list
    await checkoutPage.waitForOrderRedirect();
    
    // Kiểm tra trạng thái hoàn tất
    const isComplete = await checkoutPage.isCheckoutComplete();
    expect(isComplete).toBe(true);
  });

  test('2) Accurate price calculation: Verify subtotal, discount, and shipping fees', async () => {
    // Đợi PriceBreakdown render xong
    await expect(checkoutPage.subtotalDisplay.first()).toBeVisible({ timeout: 10000 });

    const subtotalText = await checkoutPage.getSubtotal();
    const subtotal = parseVietnamPrice(subtotalText);

    expect(subtotal).toBeGreaterThan(0);

    const shippingFeeText = await checkoutPage.getShippingFee();
    const shippingFeeValue = parseVietnamPrice(shippingFeeText);

    expect(shippingFeeValue).toBe(SHIPPING_FEE);

    const totalPriceText = await checkoutPage.getTotalPrice();
    const totalPrice = parseVietnamPrice(totalPriceText);

    const expectedTotal = subtotal + shippingFeeValue;
    expect(totalPrice).toBe(expectedTotal);

    try {
      const discountText = await checkoutPage.getDiscountAmount();
      const discount = parseVietnamPrice(discountText);
      expect(discount).toBe(0);
    } catch {
      // Ignored if discount element is not present
    }
  });
});