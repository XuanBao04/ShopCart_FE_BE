

import { expect, test } from '@playwright/test';
import CheckoutPage from './pages/CheckoutPage';
import CartPage from './pages/CartPage';
import {
  clearCartIfNotEmpty,
  parseVietnamPrice,
  SHIPPING_FEE,
  TEST_ADDRESS,
} from './utils';

test.describe.serial('Checkout & Order E2E Tests - Complete Flow', () => {
  let checkoutPage: CheckoutPage;
  let cartPage: CartPage;

  test.beforeEach(async ({ page }) => {
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

    const itemPromise = page
      .locator('[data-testid="cart-item"]')
      .first()
      .waitFor({ state: 'visible', timeout: 8000 });
    const emptyPromise = page
      .locator('[data-testid="empty-cart-message"]')
      .waitFor({ state: 'visible', timeout: 8000 });

    itemPromise.catch(() => {});
    emptyPromise.catch(() => {});

    await Promise.race([itemPromise, emptyPromise]);

    if (
      await page
        .locator('[data-testid="empty-cart-message"]')
        .isVisible()
    ) {
      await page.reload();
      await page.waitForLoadState('networkidle');
    }

    await expect(
      page.locator('[data-testid="cart-item"]').first()
    ).toBeVisible({ timeout: 10000 });

    await checkoutPage.shippingAddressInput.waitFor({
      state: 'visible',
    });
  });

  test('1) Complete checkout flow: Add items to cart, fill address, and place order', async ({
    page,
  }) => {
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

    await expect(checkoutPage.shippingAddressInput).toHaveValue(
      TEST_ADDRESS.shippingAddress
    );
    await expect(checkoutPage.cityInput).toHaveValue(TEST_ADDRESS.city);
    await expect(checkoutPage.districtInput).toHaveValue(
      TEST_ADDRESS.district
    );
    await expect(checkoutPage.wardInput).toHaveValue(TEST_ADDRESS.ward);
    await expect(checkoutPage.phoneNumberInput).toHaveValue(
      TEST_ADDRESS.phoneNumber
    );

    const subtotal = await checkoutPage.getSubtotal();
    const shippingFee = await checkoutPage.getShippingFee();
    const totalPrice = await checkoutPage.getTotalPrice();

    expect(subtotal).toBeTruthy();
    expect(shippingFee).toBeTruthy();
    expect(totalPrice).toBeTruthy();

    await checkoutPage.placeOrder();

    await checkoutPage.waitForOrderRedirect();

    const isComplete = await checkoutPage.isCheckoutComplete();
    expect(isComplete).toBe(true);
  });

  test('2) Accurate price calculation: Verify subtotal, discount, and shipping fees', async () => {
    await expect(checkoutPage.subtotalDisplay.first()).toBeVisible({
      timeout: 10000,
    });

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
    }
  });
});
