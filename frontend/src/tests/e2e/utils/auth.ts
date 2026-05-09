/**
 * Auth: Chứa logic xác thực cho E2E tests
 * 
 * ⚠️ DEPRECATED in beforeEach: 
 *   - Global Setup (global-setup.ts) xử lý đăng nhập 1 lần duy nhất
 *   - Các test files không cần gọi setupAuthenticatedSession() nữa
 *   - Auth state được tải tự động từ .auth/user.json
 * 
 * ✅ Vẫn có thể dùng cho:
 *   - Kiểm tra invalid login scenarios
 *   - Testing multi-user workflows (tạo auth state riêng)
 *   - Manual testing khi Global Setup fail
 */

import { type Page } from '@playwright/test';
import { API_URL } from './constants';
import { checkBackendHealth, testDatabaseConnection } from './systemCheck';


export async function setupAuthenticatedSession(
  page: Page,
  maxRetries: number = 3
) {
  // Kiểm tra xem backend có chạy không
  const isBackendHealthy = await checkBackendHealth(page);
  if (!isBackendHealthy) {
    throw new Error(
      `Backend không phản hồi. Vui lòng kiểm tra:
      1. Docker containers đang chạy: docker compose up -d
      2. Backend service tại ${API_URL} có sẵn
      3. Database đã khởi tạo và seed dữ liệu`
    );
  }

  //  Kiểm tra xem database schema đã được tạo chưa
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
  console.log('Database schema is ready');

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
      const urlPromise = page
        .waitForURL('**/products', { timeout: 10000 })
        .then(() => 'success' as const);
      const errorPromise = page
        .locator('.Toastify__toast--error')
        .waitFor({ state: 'visible', timeout: 10000 })
        .then(() => 'error' as const);

      // Bắt lỗi ngầm để tránh Unhandled Promise Rejection rò rỉ ra console
      urlPromise.catch(() => {});
      errorPromise.catch(() => {});

      const result = await Promise.race([urlPromise, errorPromise]).catch(
        () => 'timeout' as const
      );

      if (result === 'success') {
        console.log(
          `Đăng nhập thành công ở lần thử thứ ${attempt}`
        );
        return; // Login successful
      }

      if (result === 'error') {
        const msg = await page
          .locator('.Toastify__toast--error')
          .innerText();
        throw new Error(`Login error: ${msg}`);
      }

      if (result === 'timeout') {
        throw new Error(
          'Timeout: Không thấy chuyển hướng cũng như không có thông báo lỗi.'
        );
      }
    } catch (error) {
      lastError =
        error instanceof Error ? error : new Error(String(error));
      console.warn(
        `Lần thử ${attempt}/${maxRetries} thất bại:`,
        lastError.message
      );

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
