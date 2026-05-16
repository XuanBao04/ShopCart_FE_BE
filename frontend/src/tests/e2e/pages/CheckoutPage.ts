import { Page, Locator, expect } from '@playwright/test';

export class CheckoutPage {
  readonly page: Page;

  readonly cartItemsContainer: Locator;
  readonly cartItems: Locator;

  readonly shippingAddressInput: Locator;
  readonly cityInput: Locator;
  readonly districtInput: Locator;
  readonly wardInput: Locator;
  readonly postalCodeInput: Locator;
  readonly phoneNumberInput: Locator;

  readonly couponInput: Locator;
  readonly applyCouponBtn: Locator;
  readonly removeCouponBtn: Locator;
  readonly couponSuccess: Locator;
  readonly couponError: Locator;

  readonly subtotalDisplay: Locator;
  readonly discountDisplay: Locator;
  readonly shippingFeeDisplay: Locator;
  readonly totalPriceDisplay: Locator;
  readonly couponBadge: Locator;

  readonly checkoutBtn: Locator;
  readonly clearCartBtn: Locator;

  readonly successMessage: Locator;
  readonly errorMessage: Locator;
  readonly inventoryWarning: Locator;
  readonly emptyCartMessage: Locator;

  constructor(page: Page) {
    this.page = page;

    this.cartItemsContainer = page.locator('[data-testid="cart-items-container"]');
    this.cartItems = page.locator('[data-testid="cart-item"]');

    this.shippingAddressInput = page.locator('[data-testid="shipping-address-input"]');
    this.cityInput = page.locator('[data-testid="city-input"]');
    this.districtInput = page.locator('[data-testid="district-input"]');
    this.wardInput = page.locator('[data-testid="ward-input"]');
    this.postalCodeInput = page.locator('[data-testid="postal-code-input"]');
    this.phoneNumberInput = page.locator('[data-testid="phone-number-input"]');

    this.couponInput = page.locator('[data-testid="coupon-input"]');
    this.applyCouponBtn = page.locator('[data-testid="apply-coupon-btn"]');
    this.removeCouponBtn = page.locator('[data-testid="remove-coupon-btn"]');
    this.couponSuccess = page.locator('[data-testid="coupon-success"]');
    this.couponError = page.locator('[data-testid="coupon-error"]');

    this.subtotalDisplay = page.locator('[data-testid="subtotal-display"]');
    this.discountDisplay = page.locator('[data-testid="discount-display"]');
    this.shippingFeeDisplay = page.locator('[data-testid="shipping-fee-display"]');
    this.totalPriceDisplay = page.locator('[data-testid="total-price-display"]');
    this.couponBadge = page.locator('[data-testid="coupon-badge"]');

    this.checkoutBtn = page.locator('[data-testid="checkout-btn"]');
    this.clearCartBtn = page.locator('[data-testid="clear-cart-btn"]');

    this.successMessage = page.locator('[data-testid="success-message"]');
    this.errorMessage = page.locator('[data-testid="error-message"]');
    this.inventoryWarning = page.locator('[data-testid="inventory-warning"]');
    this.emptyCartMessage = page.locator('[data-testid="empty-cart-message"]');
  }

  
  async goToCart() {
    await this.page.goto('/authenticated/cart');
    await this.page.waitForLoadState('networkidle');
  }


  async fillShippingAddress(
    address: string,
    city: string,
    district: string,
    ward: string,
    postalCode: string,
    phoneNumber: string
  ) {
    await this.shippingAddressInput.fill(address);
    await this.cityInput.fill(city);
    await this.districtInput.fill(district);
    await this.wardInput.fill(ward);
    await this.postalCodeInput.fill(postalCode);
    await this.phoneNumberInput.fill(phoneNumber);
  }

 
  async applyCoupon(code: string) {
    await this.couponInput.fill(code);
    await this.applyCouponBtn.click();
    await this.page.waitForTimeout(1000);
  }

 
  async removeCoupon() {
    await this.removeCouponBtn.click();
    await this.page.waitForTimeout(500);
  }

 
  async getSubtotal(): Promise<string> {
    return await this.subtotalDisplay.innerText();
  }


  async getDiscountAmount(): Promise<string> {
    return await this.discountDisplay.innerText();
  }


  async getShippingFee(): Promise<string> {
    return await this.shippingFeeDisplay.innerText();
  }


  async getTotalPrice(): Promise<string> {
    return await this.totalPriceDisplay.innerText();
  }


  async isCouponErrorVisible(): Promise<boolean> {
    return await this.couponError.isVisible();
  }


  async isCouponSuccessVisible(): Promise<boolean> {
    return await this.couponSuccess.isVisible();
  }

 
  async isInventoryWarningVisible(): Promise<boolean> {
    try {
      await this.inventoryWarning.isVisible({ timeout: 2000 });
      return true;
    } catch {
      return false;
    }
  }

  async placeOrder() {
    await this.checkoutBtn.waitFor({ state: 'visible' });
    await this.checkoutBtn.click();

    // Đợi hoặc là URL thay đổi (thành công), hoặc là Toast lỗi hiện lên (thất bại)
    try {
      await Promise.race([
        this.page.waitForURL('**/authenticated/orders', { timeout: 30000 }),
        this.page.waitForSelector('.Toastify__toast--error', { timeout: 30000 }).then(async (el) => {
          const msg = await el.innerText();
          throw new Error(`Thanh toán thất bại: ${msg}`);
        })
      ]);
    } catch (error) {
      if (this.page.url().includes('/authenticated/orders')) {
        return;
      }
      throw error;
    }
  }


  async clearCart() {
    await this.clearCartBtn.click();
  }

  async waitForOrderRedirect() {
    // URL đã được check ở placeOrder, giờ chỉ cần đợi element render
    await this.page.locator('[data-testid="orders-list"]').first().waitFor({ state: 'visible', timeout: 15000 });
  }


  async clearCartAndVerifyEmpty() {
    await this.page.goto('/authenticated/cart');
    await this.page.waitForLoadState('networkidle');
    const emptyVisible = await this.emptyCartMessage.isVisible({ timeout: 5000 }).catch(() => false);
    if (!emptyVisible) {
      const count = await this.cartItems.count();
      expect(count).toBe(0);
    } else {
      expect(emptyVisible).toBe(true);
    }
  }


  async getCartItemCount(): Promise<number> {
    await this.cartItems.first().waitFor({ state: 'visible', timeout: 10000 }).catch(() => {});
    return await this.cartItems.count();
  }


  async getErrorMessage(): Promise<string> {
    return await this.errorMessage.innerText();
  }


  async isCheckoutComplete(): Promise<boolean> {
    const url = this.page.url();
    if (!url.includes('/authenticated/orders')) {
      return false;
    }
    const ordersVisible = await this.page.locator('[data-testid="orders-list"]').isVisible();
    return ordersVisible;
  }


  async isCartEmpty(): Promise<boolean> {
    try {
      await this.emptyCartMessage.isVisible({ timeout: 2000 });
      return true;
    } catch {
      return false;
    }
  }
}

export default CheckoutPage;
