/**
 * System Check: Chứa các hàm kiểm tra sức khỏe hệ thống
 * Bao gồm kiểm tra backend, database, và các điều kiện tiên quyết
 */

import { type Page } from '@playwright/test';

/**
 * Kiểm tra backend có sẵn và phản hồi không
 * Sử dụng actuator/health endpoint
 */
export async function checkBackendHealth(page: Page): Promise<boolean> {
  try {
    const baseUrl =
      process.env.VITE_API_URL?.replace('/api', '') ||
      'http://localhost:8080';

    const response = await page.request.get(`${baseUrl}/actuator/health`, {
      timeout: 5000,
    });

    console.log(`Backend health check: ${response.status()}`);

    return response.ok();
  } catch (error) {
    console.error('Backend health check failed:', error);
    return false;
  }
}

/**
 * Kiểm tra kết nối database và schema đã được tạo chưa
 * Thử login với user test để phát hiện lỗi schema
 */
export async function testDatabaseConnection(page: Page): Promise<boolean> {
  try {
    const baseUrl =
      process.env.VITE_API_URL || 'http://localhost:8080/api';
    const response = await page.request.post(`${baseUrl}/auth/login`, {
      data: {
        username: 'test_schema_check',
        password: 'test',
      },
    });

    const text = await response.text();
    if (text.includes('relation') && text.includes('does not exist')) {
      console.error('Database chưa được tạo');
      console.error('Giải pháp: restart lại service backend');
      return false;
    }
    return true;
  } catch (error) {
    console.error('Database connection test failed:', error);
    return false;
  }
}
