import { expect, test, type Page } from '@playwright/test';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';

async function setupAuthenticatedSession(page: Page) {
  await page.goto('/login');
  await page.fill('input[name="username"]', 'customer1');
  await page.fill('input[name="password"]', 'password123');
  await page.click('button[type="submit"]');
  await page.waitForURL('**/products');
}

test.describe('Cart E2E - Add to cart with real backend', () => {
  let cartPage: CartPage;
  let checkoutPage: CheckoutPage;

  test.beforeEach(async ({ page }) => {
    cartPage = new CartPage(page);
    checkoutPage = new CheckoutPage(page);
    await setupAuthenticatedSession(page);

    // Clear cart before each test to ensure a clean state
    await checkoutPage.goToCart();
    
    // Đợi trang giỏ hàng load xong
    await page.waitForLoadState('networkidle');
    const items = await page.locator('[data-testid="cart-item"]').count();
    
    if (items > 0) {
      await checkoutPage.clearCart();
      // Chờ cho đến khi giỏ hàng thực sự trống
      await expect(page.locator('[data-testid="cart-item"]')).toHaveCount(0, { timeout: 10000 });
    }
  });

  test('1) Complete add-to-cart flow: add item, check cart, update, remove', async ({ page }) => {
    // 1. Đi đến trang sản phẩm
    await cartPage.goToProducts();

    // 2. Lấy tên sản phẩm LÚC ĐANG Ở TRANG PRODUCTS
    const expectedProductName = await cartPage.getFirstProductName();
    
    // 3. Thêm vào giỏ hàng
    await cartPage.addProductToCart(1);
    
    // 4. Chuyển sang trang giỏ hàng
    await cartPage.goToCartAndWait();
    
    // SỬA LỖI Ở ĐÂY: Sử dụng biến expectedProductName đã lưu ở trên để so sánh
    // KHÔNG gọi lại await cartPage.getFirstProductName() vì lúc này không còn ở trang Products nữa
    await expect(page.getByText(expectedProductName)).toBeVisible();

    // Update quantity
    const quantityInput = page.locator('input[type="number"]').first();
    const currentValue = Number(await quantityInput.inputValue());
    await page.getByRole('button', { name: '+' }).first().click();
    
    // Chờ API cập nhật số lượng thành công (nếu UI bạn có loading state thì cần chờ)
    await expect(quantityInput).toHaveValue(String(currentValue + 1), { timeout: 5000 });

    // Remove item
    // Sử dụng locator rõ ràng hơn cho nút Xóa
    await page.getByRole('button', { name: 'Xóa' }).first().click();
    
    // Kiểm tra UI hiển thị giỏ hàng trống (Sửa lại regex cho chuẩn xác với tiếng Việt)
    await expect(page.getByText(/giỏ hàng trống/i)).toBeVisible({ timeout: 5000 });
  });

  test('2) Stock validation on UI: input quantity should not exceed max stock', async () => {
    await cartPage.goToProducts();

    const productCard = await cartPage.getFirstAvailableProduct();
    if (!productCard) {
      test.skip(true, 'No available product found to test stock validation.');
      return;
    }

    const quantityInput = productCard.locator('[data-testid="quantity-input"]');
    const maxAttr = await quantityInput.getAttribute('max');

    if (!maxAttr || Number(maxAttr) < 1) {
      test.skip(true, 'Invalid stock data from backend.');
      return;
    }

    const maxStock = Number(maxAttr);
    // Cố tình điền một số lượng vượt quá số lượng trong kho
    await quantityInput.fill(String(maxStock + 99));

    // UI should clamp the value to max stock (Trình duyệt sẽ tự động đưa về max hoặc app của bạn xử lý)
    await expect(quantityInput).toHaveValue(String(maxStock));
  });
});