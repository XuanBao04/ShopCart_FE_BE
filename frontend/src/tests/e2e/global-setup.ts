

import { chromium, FullConfig } from '@playwright/test';
import { checkBackendHealth, testDatabaseConnection } from './utils/systemCheck';

const AUTH_FILE = 'src/tests/e2e/.auth/user.json';

export default async function globalSetup(config: FullConfig) {
  console.log('Starting Global Setup - Authenticating user...');

  const browser = await chromium.launch();
  const page = await browser.newPage();

  try {
    const isBackendHealthy = await checkBackendHealth(page);
    if (!isBackendHealthy) {
      throw new Error(
        ` Backend không phản hồi. Vui lòng kiểm tra:
        1. Docker containers đang chạy: docker compose up -d
        2. Backend service tại http://localhost:8080/api có sẵn
        3. Database đã khởi tạo và seed dữ liệu`
      );
    }
    console.log(' Backend is healthy');

    console.log('Checking database schema...');
    const isDatabaseReady = await testDatabaseConnection(page);
    if (!isDatabaseReady) {
      throw new Error(
        ` Database schema chưa được khởi tạo. Vui lòng:\n` +
        `1. Restart backend container: docker restart shopcart_backend\n` +
        `2. Đợi 10-15 giây để Hibernate tạo schema và DataInitializer seed dữ liệu\n` +
        `3. Thử lại: npm run e2e:ui`
      );
    }
    console.log(' Database schema is ready');

    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle' });
    console.log('Navigated to login page');

    await page.fill('input[name="username"]', 'customer1');
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    console.log('Login form submitted');

    const urlPromise = page
      .waitForURL('**/products', { timeout: 15000 })
      .then(() => 'success' as const);
    const errorPromise = page
      .locator('.Toastify__toast--error')
      .waitFor({ state: 'visible', timeout: 5000 })
      .then(() => 'error' as const);

    urlPromise.catch(() => {});
    errorPromise.catch(() => {});

    const result = await Promise.race([urlPromise, errorPromise]).catch(
      () => 'timeout' as const
    );

    if (result === 'error') {
      const msg = await page.locator('.Toastify__toast--error').innerText();
      throw new Error(` Login failed: ${msg}`);
    }

    if (result === 'timeout') {
      throw new Error(
        ' Login timeout: Không thấy chuyển hướng cũng như không có thông báo lỗi'
      );
    }

    console.log(' Login successful!');


    await page.context().storageState({ path: AUTH_FILE });
    console.log(` Auth state saved to: ${AUTH_FILE}`);

  } catch (error) {
    console.error(' Global Setup failed:', error);
    throw error;
  } finally {
    await browser.close();
  }

  console.log(' Global Setup completed successfully!\n');
}
