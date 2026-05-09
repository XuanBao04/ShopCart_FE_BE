/**
 * Helpers: Chứa các hàm tiện ích chung dùng trong E2E tests
 * Bao gồm parse dữ liệu, xử lý giỏ hàng, và các tác vụ UI phổ biến
 */

import { expect, type Page } from '@playwright/test';
import CheckoutPage from '../pages/CheckoutPage';

/**
 * Parse giá tiền Vietnam (VND) từ text thành số nguyên
 * Ví dụ: "1.500.000 VND" -> 1500000
 */
export function parseVietnamPrice(priceText: string): number {
  let cleaned = priceText.replace(/VND|đ/gi, '').trim();
  cleaned = cleaned.replace(/\s|\./g, '');
  cleaned = cleaned.replace(',', '');
  return parseInt(cleaned, 10);
}

/**
 * Xóa giỏ hàng nếu không trống
 * Chờ đến khi UI xác nhận giỏ hàng rỗng
 */
export async function clearCartIfNotEmpty(
  page: Page,
  checkoutPage: CheckoutPage
) {
  await checkoutPage.goToCart();
  // Đợi trang load và ổn định
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);

  const cartItems = page.locator('[data-testid="cart-item"]');
  const count = await cartItems.count();

  if (count > 0) {
    await checkoutPage.clearCart();
    // Đợi cho đến khi UI xác nhận giỏ hàng trống (dùng data-testid)
    await expect(page.locator('[data-testid="empty-cart-message"]')).toBeVisible({
      timeout: 10000,
    });
  }
}
