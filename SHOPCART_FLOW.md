# [cite_start]Bài Tập Lớn: Kiểm Thử Phần Mềm - Dự án SHOPCART (Version 1.1) [cite: 3, 4, 5, 6]

[cite_start]**Trường:** Đại học Sài Gòn - Khoa Công Nghệ Thông Tin [cite: 1]
[cite_start]**Môn học:** Kiểm Thử Phần Mềm (Niên khóa: 2025 - 2026) [cite: 10, 11]
[cite_start]**Giảng viên hướng dẫn:** Từ Lãng Phiêu [cite: 7, 8]

---

## [cite_start]1. Giới thiệu Dự án [cite: 13]

[cite_start]ShopCart là một ứng dụng web thương mại điện tử được xây dựng để phục vụ việc thực hành kiểm thử phần mềm ở cả Frontend và Backend[cite: 16]. Các nghiệp vụ cốt lõi bao gồm:
* [cite_start]**Giỏ hàng (Cart):** Thêm, xóa sản phẩm, cập nhật số lượng[cite: 17].
* [cite_start]**Tính giá (Pricing):** Tính tổng tiền, áp dụng mã giảm giá, phí vận chuyển[cite: 18].
* [cite_start]**Tồn kho (Inventory):** Kiểm tra số lượng, cảnh báo/khóa thao tác khi hết hàng[cite: 19, 20].
* [cite_start]**Mua hàng (Checkout):** Xác nhận đơn, trừ tồn kho, tạo mã đơn[cite: 21].

### [cite_start]Công nghệ sử dụng [cite: 26]
* [cite_start]**Frontend:** React 19.x, Vite, Axios, TailwindCSS[cite: 29, 33, 34, 35]. [cite_start]Kiểm thử với Vitest, React Testing Library, và Playwright[cite: 30, 31, 32].
* [cite_start]**Backend:** Spring Boot 3.5.x/4.x, Java 21, Spring Data JPA, Spring Security, H2/PostgreSQL[cite: 40, 41, 46, 47, 48]. [cite_start]Kiểm thử với JUnit 5 và Mockito[cite: 42, 43].
* [cite_start]**Phương pháp:** Khuyến khích Test-Driven Development (TDD)[cite: 25].

---

## [cite_start]2. Yêu Cầu Bài Tập Lớn [cite: 89]

[cite_start]Dự án yêu cầu thực hiện 6 nội dung kiểm thử chính, tổng điểm 10.0[cite: 1442].

### [cite_start]Câu 1: Phân tích và Thiết kế Test Cases (0.5 điểm) [cite: 89]
* [cite_start]**Giỏ hàng (Cart):** Phân tích validation rules (số lượng, ID sản phẩm, tồn kho) và thiết kế 5 test cases chi tiết (Happy path, Negative, Boundary, Edge cases)[cite: 90, 94, 100, 106, 112].
* [cite_start]**Mua hàng (Purchase):** Phân tích validation rules (tổng giá, mã giảm giá, địa chỉ) và thiết kế 5 test cases chi tiết[cite: 119, 123, 128, 137, 145].

### [cite_start]Câu 2: Unit Testing và TDD (2.0 điểm) [cite: 150]
* **Frontend (Vitest):**
    * [cite_start]Viết unit tests cho `validateCartItem()` và `calculateCartTotal()`[cite: 154, 159].
    * [cite_start]Viết unit tests cho `calculateOrderPrice()` và `checkInventoryAvailability()`[cite: 263, 269].
    * [cite_start]Mục tiêu coverage > 90%[cite: 164, 270].
* **Backend (JUnit 5 + Mockito):**
    * [cite_start]Test `CartService`: `addToCart()`, `removeFromCart()`, `updateQuantity()`[cite: 180, 188].
    * [cite_start]Test `OrderService`: CRUD operations, `calculateOrderTotal()`, `checkStockBeforeOrder()`[cite: 314].
    * [cite_start]Mục tiêu coverage >= 85%[cite: 189, 322].

### [cite_start]Câu 3: Integration Testing (2.0 điểm) [cite: 423]
* **Frontend Component Integration:**
    * [cite_start]Sử dụng Vitest + React Testing Library để test `CartComponent` (rendering, interactions, API calls)[cite: 425, 429, 430].
    * [cite_start]Test `CheckoutSummary`, `PriceCalculator`, `InventoryWarning`[cite: 542].
* **Backend API Integration:**
    * [cite_start]Sử dụng `MockMvc` để test các endpoint như `POST /api/cart/add` và `POST /api/orders`[cite: 465, 467, 589, 590].

### [cite_start]Câu 4: Mock Testing (2.0 điểm) [cite: 671]
* [cite_start]**Frontend Mocking:** Dùng `vi.mock()` để mock `cartService`, `orderService`, và `inventoryService` trong lúc test các component[cite: 674, 676, 813, 815].
* [cite_start]**Backend Mocking:** Dùng `@MockBean` để mock các Service layer khi test Controller, và mock các Repository layer khi test Service[cite: 749, 877, 878].

### [cite_start]Câu 5: Automation Testing & CI/CD (2.0 điểm) [cite: 943]
* [cite_start]**E2E Testing (Playwright):** * Thiết lập Playwright với 3 browsers: Chromium, Firefox, WebKit[cite: 948].
    * [cite_start]Áp dụng Page Object Model (POM) cho các trang Cart và Checkout[cite: 949, 1103].
    * [cite_start]Viết E2E tests hoàn chỉnh cho Add-to-cart flow và Checkout flow[cite: 953, 1199].
* [cite_start]**CI/CD (GitHub Actions):** Xây dựng workflow tự động chạy Backend tests, Frontend tests, Playwright tests và lưu lại Test Reports khi push code[cite: 1015, 1253, 1254].

### [cite_start]Câu 6: Advanced Testing (1.5 điểm) [cite: 1421]
* [cite_start]**Performance Testing:** Dùng k6 hoặc JMeter thiết kế kịch bản tải, đo lường response time, throughput, error rate cho API và đề xuất tối ưu[cite: 1423, 1425, 1426, 1427].
* **Security Testing:** Thiết kế và thực thi test cases cho các lỗ hổng như SQL Injection, XSS, IDOR, CSRF. [cite_start]Đưa ra biện pháp khắc phục[cite: 1431, 1432, 1434].

---

## [cite_start]3. Tiêu Chí Đánh Giá [cite: 1439]

* [cite_start]**Code Quality (30%):** Clean code, cấu trúc AAA (Arrange - Act - Assert), test coverage >= 80%, toàn bộ test pass[cite: 1444, 1445, 1446, 1448, 1449].
* [cite_start]**Completeness (30%):** Hoàn thành đủ 6 câu hỏi bắt buộc, CI/CD pipeline chạy thành công (màu xanh), Playwright test chạy trên tối thiểu 2 trình duyệt[cite: 1455, 1456, 1458, 1459].
* [cite_start]**Documentation (20%):** Test cases đúng template, có screenshots minh chứng, kèm các báo cáo HTML/JaCoCo và file README hoàn chỉnh[cite: 1450, 1451, 1452, 1453, 1454].
* [cite_start]**Best Practices (20%):** Áp dụng chuẩn TDD (Red-Green-Refactor), chiến lược Mock hợp lý, sử dụng Page Object Model chuẩn chỉ[cite: 1463, 1464, 1466, 1468].

---

## [cite_start]4. Hướng Dẫn Nộp Bài [cite: 1472]

* [cite_start]**Source Code:** Đẩy lên Public Git repository (GitHub/GitLab) với lịch sử commit rõ ràng (Conventional Commits)[cite: 1476, 1478]. [cite_start]File `README.md` phải hướng dẫn chi tiết cách cài đặt và chạy test[cite: 1479].
* **Báo cáo:**
    * [cite_start]Định dạng: File PDF tối đa 20 trang[cite: 1483].
    * [cite_start]Tên file: `Test_Report_YourName.pdf`[cite: 1484].
    * [cite_start]**Lưu ý:** Nộp bài CÁ NHÂN (không chấp nhận báo cáo chung)[cite: 1486, 1487].
* [cite_start]**Deadline:** 23h59, ngày **10/05/2026** (Không chấp nhận nộp trễ)[cite: 1497, 1498].
* [cite_start]**Demo (Tùy chọn):** Quay video 10-15 phút demo quá trình chạy unit test, E2E test, CI/CD pipeline và hỏi đáp với giảng viên[cite: 1502, 1503, 1504, 1505, 1506, 1507].

---

## [cite_start]5. Kiến Trúc Hệ Thống Đơn Giản [cite: 1510]

### [cite_start]Flow Chính [cite: 1511]
1. **Đăng nhập:** User nhập email/password → Backend validate → trả về JWT token
2. **Giỏ hàng:** Add product → tính giá → hiển thị trên UI
3. **Thanh toán:** Click checkout → tạo order → trừ inventory → hiển thị hóa đơn

### [cite_start]Database Schema [cite: 1512]
- **User:** id, email, password (hash), createdAt
- **Product:** id, name, price, quantity (inventory)
- **Cart:** id, userId, items (JSON: [{productId, quantity, price}])
- **Order:** id, userId, items (JSON), totalPrice, status, createdAt

### [cite_start]API Endpoints [cite: 1513]
- `POST /auth/login` - Đăng nhập
- `GET /products` - Lấy danh sách sản phẩm
- `POST /cart/add` - Thêm vào giỏ
- `POST /orders` - Tạo hóa đơn
- `GET /orders/:id` - Chi tiết hóa đơn