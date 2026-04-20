# 📋 SHOPCART - KẾ HOẠCH HOÀN THÀNH DỰ ÁN

**Deadline:** 23h59, 10/05/2026 (16 ngày còn lại)  
**Ngày lập kế hoạch:** 20/04/2026  
**Tổng điểm:** 10.0

---

## 📊 TỔNG QUAN TIẾN ĐỘ

| Câu hỏi | Yêu cầu | Hiện tại | Mục tiêu | Trạng thái |
|--------|---------|---------|---------|-----------|
| **Câu 1** | Test Case Design (0.5đ) | 0% | 100% | ❌ |
| **Câu 2** | Unit Testing + TDD (2.0đ) | 30% | 100% | ⚠️ |
| **Câu 3** | Integration Testing (2.0đ) | 0% | 100% | ❌ |
| **Câu 4** | Mock Testing (2.0đ) | 40% | 100% | ⚠️ |
| **Câu 5** | Automation & CI/CD (2.0đ) | 40% | 100% | ⚠️ |
| **Câu 6** | Advanced Testing (1.5đ) | 0% | 100% | ❌ |
| **TỔNG** | 10.0đ | **22%** | **100%** | - |

---

## 📅 TUẦN 1: BACKEND UNIT TESTS (20/04 - 26/04)

### **Ngày 1-2 (20-21/04) - OrderService & InventoryService Tests**
- [ ] Đọc `OrderServiceImpl.java` và `InventoryServiceImpl.java` - 1h
- [ ] Tạo `OrderServiceTest.java` với các test cases:
  - [ ] `testCreateOrder_Success` (Happy path)
  - [ ] `testCreateOrder_InvalidInput` (Negative)
  - [ ] `testUpdateOrderStatus_Success`
  - [ ] `testGetOrderById_NotFound`
  - [ ] `testCalculateOrderTotal_WithDiscount`
  - [ ] Coverage >= 85%
- [ ] Tạo `InventoryServiceTest.java`:
  - [ ] `testGetStock_Success`
  - [ ] `testUpdateStock_InsufficientStock`
  - [ ] `testReserveStock_Success`
  - [ ] Coverage >= 85%
- [ ] Chạy test: `mvn clean test`
- [ ] Verify: Coverage reports tại `target/site/jacoco/`

### **Ngày 3-4 (22-23/04) - Controller Integration Tests**
- [ ] Tạo `CartControllerTest.java`:
  - [ ] `testAddToCart_Success` (MockMvc)
  - [ ] `testRemoveFromCart_InvalidId`
  - [ ] `testUpdateQuantity_Boundary`
  - [ ] `testGetCart_Success`
- [ ] Tạo `OrderControllerTest.java`:
  - [ ] `testCreateOrder_Success` (MockMvc)
  - [ ] `testGetOrderById_NotFound`
  - [ ] `testUpdateOrder_Unauthorized`
- [ ] Tạo `ProductControllerTest.java`:
  - [ ] `testGetAllProducts_Success`
  - [ ] `testGetProductById_NotFound`
  - [ ] `testSearchProducts_WithFilter`
- [ ] Chạy: `mvn clean test`

### **Ngày 5 (24/04) - Code Coverage & Documentation**
- [ ] Cấu hình JaCoCo trong `pom.xml`
- [ ] Chạy: `mvn clean test jacoco:report`
- [ ] Kiểm tra coverage: mục tiêu >= 85%
- [ ] Tạo `BACKEND_TEST_REPORT.md`:
  - List tất cả test classes
  - Coverage stats
  - Failed/Skipped tests (nếu có)

### **Ngày 6 (25/04) - CI/CD Enhancement (Backend)**
- [ ] Cập nhật `.github/workflows/ci.yml`:
  - [ ] Thêm JaCoCo report generation
  - [ ] Upload coverage reports như artifacts
  - [ ] Fail job nếu coverage < 85%
- [ ] Test workflow: push code lên branch
- [ ] Verify GitHub Actions runs successfully ✅

---

## 📅 TUẦN 2: FRONTEND UNIT TESTS (27/04 - 03/05)

### **Ngày 7-8 (27-28/04) - Service & Utility Tests**
- [ ] Tạo `src/tests/services/cartService.test.ts`:
  - [ ] `addToCart()` - success & error cases
  - [ ] `removeFromCart()` - valid & invalid IDs
  - [ ] `updateQuantity()` - boundary cases
  - [ ] Mock axios API calls
- [ ] Tạo `src/tests/services/orderService.test.ts`:
  - [ ] `createOrder()` - success & validation errors
  - [ ] `getOrderById()` - found & not found
  - [ ] `calculateOrderTotal()` - with/without discount
- [ ] Tạo `src/tests/services/productService.test.ts`:
  - [ ] `getAllProducts()`
  - [ ] `getProductById()`
  - [ ] `searchProducts()` - with filter params
- [ ] Tạo `src/tests/utils/validation.test.ts`:
  - [ ] `isValidEmail()` - valid & invalid emails
  - [ ] `isValidPhone()` - 10 digits
  - [ ] `isValidQuantity()` - positive integers only
  - [ ] `isValidPrice()` - positive numbers
  - [ ] `isValidZipCode()` - 5 digits
- [ ] Tạo `src/tests/utils/priceCalculation.test.ts`:
  - [ ] `calculateCartTotal()` - with/without tax
  - [ ] `calculateDiscount()` - various discount rates
  - [ ] `calculateShippingCost()` - different methods
  - [ ] `calculateOrderTotal()` - complete flow

### **Ngày 9-10 (29-30/04) - Hook & Component Tests**
- [ ] Tạo `src/tests/hooks/useCart.test.ts`:
  - [ ] `addItem()`, `removeItem()`, `updateQuantity()`
  - [ ] Local storage persistence
- [ ] Tạo `src/tests/hooks/useAsync.test.ts`:
  - [ ] Loading, success, error states
  - [ ] Cleanup on unmount
- [ ] Tạo `src/tests/components/Cart.test.tsx`:
  - [ ] Render cart items
  - [ ] Add/remove item interactions
  - [ ] Quantity update
  - [ ] API call mocking (vi.mock)
- [ ] Tạo `src/tests/components/Checkout.test.tsx`:
  - [ ] Form validation
  - [ ] Submit success & error
  - [ ] Discount code validation
- [ ] Tạo `src/tests/components/ProductList.test.tsx`:
  - [ ] Render product list
  - [ ] Filter/search interactions
  - [ ] Pagination

### **Ngày 11 (01/05) - Code Coverage & Documentation**
- [ ] Chạy: `npm run test:coverage`
- [ ] Verify coverage >= 90%
- [ ] Tạo `FRONTEND_TEST_REPORT.md`:
  - List tất cả test files
  - Coverage stats per file
  - Failed/Skipped tests (nếu có)

### **Ngày 12 (02/05) - CI/CD Enhancement (Frontend)**
- [ ] Cập nhật `.github/workflows/ci.yml`:
  - [ ] Thêm coverage report generation
  - [ ] Upload coverage artifacts
  - [ ] Fail job nếu coverage < 90%
- [ ] Test workflow push
- [ ] Verify GitHub Actions success ✅

---

## 📅 TUẦN 3: E2E & ADVANCED TESTING (04/05 - 10/05)

### **Ngày 13-14 (04-05/05) - E2E Tests (Playwright)**
- [ ] Tạo `frontend/e2e/home.spec.ts`:
  - [ ] Page loads successfully
  - [ ] Product list displays
  - [ ] Navigation works
  - [ ] Tests trên 3 browsers: Chromium, Firefox, WebKit
- [ ] Tạo `frontend/e2e/cart.spec.ts`:
  - [ ] Add to cart success
  - [ ] Remove from cart
  - [ ] Update quantity
  - [ ] Empty cart warning
  - [ ] Tests trên 3 browsers
- [ ] Tạo `frontend/e2e/checkout.spec.ts`:
  - [ ] Complete checkout flow
  - [ ] Form validation (email, phone, zip)
  - [ ] Discount code application
  - [ ] Order confirmation
  - [ ] Tests trên 3 browsers
- [ ] Chạy: `npm run e2e`
- [ ] Generate HTML reports

### **Ngày 15 (06/05) - CI/CD Complete + Câu 1 Documentation**
- [ ] Cập nhật `.github/workflows/ci.yml`:
  - [ ] Thêm Playwright E2E tests
  - [ ] Upload test reports + videos
  - [ ] Fail if any test fails
  - [ ] Run on 3 browsers in parallel
- [ ] Tạo `TEST_CASES_DESIGN.md` (Câu 1):
  - [ ] Cart validation rules analysis
  - [ ] 5 test cases cho Cart (Happy, Negative, Boundary, Edge, Exception)
  - [ ] Checkout validation rules analysis
  - [ ] 5 test cases cho Checkout
  - [ ] Screenshots/Evidence
- [ ] Test workflow: push code
- [ ] Verify all tests pass ✅

### **Ngày 16 (07/05) - Advanced Testing**
- [ ] **Performance Testing:**
  - [ ] Tạo `backend/k6-script.js` (hoặc JMeter):
    - [ ] Kịch bản load test: 100 concurrent users
    - [ ] Endpoints: POST /api/orders, GET /api/products
    - [ ] Measure: Response time, throughput, error rate
  - [ ] Tạo `PERFORMANCE_TEST_REPORT.md`:
    - [ ] Results, graphs, recommendations
- [ ] **Security Testing:**
  - [ ] Tạo `SECURITY_TEST_PLAN.md`:
    - [ ] SQL Injection test cases (Order creation)
    - [ ] XSS test cases (Product name, description)
    - [ ] IDOR test cases (Get other user's orders)
    - [ ] CSRF test cases (State-changing operations)
  - [ ] Document findings + fixes

### **Ngày 17-18 (08-09/05) - Final Review & Documentation**
- [ ] Tạo `TEST_REPORT.pdf` (Final Report - max 20 pages):
  - [ ] Executive Summary
  - [ ] Test strategy & approach
  - [ ] Unit test results (Backend + Frontend)
  - [ ] Integration test results
  - [ ] E2E test results (3 browsers)
  - [ ] Code coverage metrics
  - [ ] Performance test results
  - [ ] Security test findings
  - [ ] CI/CD pipeline status
  - [ ] Recommendations
- [ ] Tạo/Update `README.md`:
  - [ ] Complete setup instructions
  - [ ] How to run tests (all types)
  - [ ] Coverage report access
  - [ ] CI/CD status badge
- [ ] Review commit history (Conventional Commits)
- [ ] Final code cleanup & quality check

### **Ngày 19 (09/05) - Demo & Final Checks**
- [ ] Record video (10-15 min):
  - [ ] Run unit tests (Backend)
  - [ ] Run unit tests (Frontend)
  - [ ] Run E2E tests
  - [ ] Show CI/CD pipeline
  - [ ] Show coverage reports
- [ ] Final verification:
  - [ ] All tests pass ✅
  - [ ] Coverage >= 85% (Backend), >= 90% (Frontend)
  - [ ] All 6 questions completed
  - [ ] Documentation complete

### **Ngày 20 (10/05) - SUBMISSION**
- [ ] Push final code to GitHub
- [ ] Create release/tag
- [ ] Submit:
  - [ ] GitHub link
  - [ ] Test_Report_YourName.pdf
  - [ ] Video demo (optional)
- [ ] ⏰ **DEADLINE: 23h59**

---

## 🎯 CHIẾN LƯỢC TOÀN BỘ

### **Test Coverage Targets:**
```
Backend:  >= 85% (JaCoCo)
Frontend: >= 90% (Vitest)
E2E:      3 browsers (Chromium, Firefox, WebKit)
```

### **Yêu cầu CI/CD Pipeline:**
```yaml
Jobs:
  - Backend Tests (mvn clean test)
  - Frontend Tests (npm run test)
  - E2E Tests (npm run e2e) [3 browsers]
  - JaCoCo Coverage Report
  - Playwright HTML Reports
```

### **Deliverables:**
- ✅ Source code (GitHub)
- ✅ Test_Report_YourName.pdf (max 20 pages)
- ✅ Video demo (10-15 min, optional)
- ✅ CI/CD pipeline (green ✅)
- ✅ Code coverage reports

---

## 📝 CHECKLISTS HÀNG NGÀY

### **Template để track daily:**
```markdown
## Ngày X (DD/MM)

### Tasks:
- [ ] Task 1
- [ ] Task 2
- [ ] Task 3

### Progress:
- Time spent: X hours
- Completed: X%
- Blockers: (if any)

### Commits:
- feat: ...
- test: ...
```

---

## 🚀 QUICK START NGAY HÔM NAY

**Bây giờ (20/04):**
```bash
# 1. Tạo OrderServiceTest
cd backend
# Xem OrderServiceImpl.java để hiểu logic

# 2. Tạo test file từ template CartServiceTest
# Adapt để test OrderService

# 3. Run tests
mvn clean test

# 4. Check coverage
mvn jacoco:report
```

**Commit format:**
```
test(backend): add OrderServiceTest with CRUD operations
test(backend): add InventoryServiceTest with stock management
```

---

## 📌 NOTES

- **TDD Approach:** Hiểu logic trước → viết test → run → refactor
- **Naming Convention:** `[Feature]Test.java`, `[function].test.ts`
- **Git Commits:** Conventional Commits (feat:, test:, fix:, docs:)
- **Communication:** Update report hàng 2 ngày

---

**Status:** 🟡 STARTING PHASE  
**Next Update:** 21/04/2026
