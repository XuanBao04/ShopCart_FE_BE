
import { expect, test } from '@playwright/test';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import { clearCartIfNotEmpty } from './utils';

test.describe.serial('Cart E2E - Add to cart with real backend', () => {
  let cartPage: CartPage;
  let checkoutPage: CheckoutPage;

  test.beforeEach(async ({ page }) => {
    cartPage = new CartPage(page);
    checkoutPage = new CheckoutPage(page);

    await clearCartIfNotEmpty(page, checkoutPage);
  });

  test('1) Complete add-to-cart flow: add item, check cart, update, remove', async ({ page }) => {
    await cartPage.goToProducts();

    const expectedProductName = await cartPage.getFirstProductName();
    await cartPage.addProductToCart(1);
    await cartPage.goToCartAndWait();
    
    await expect(page.getByText(expectedProductName)).toBeVisible();

    const quantityInput = page.locator('input[type="number"]').first();
    const currentValue = Number(await quantityInput.inputValue());
    await page.getByRole('button', { name: '+' }).first().click();
    await expect(quantityInput).toHaveValue(String(currentValue + 1), { timeout: 5000 });

    await page.getByRole('button', { name: 'Xóa' }).first().click();
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
    await quantityInput.fill(String(maxStock + 99));
    await expect(quantityInput).toHaveValue(String(maxStock));
  });
});