import { Page, Locator, expect } from '@playwright/test';

export class CartPage {
  readonly page: Page;

  readonly productCard: Locator;
  readonly productName: Locator;
  readonly productPrice: Locator;
  readonly quantityInput: Locator;
  readonly addToCartBtn: Locator;

  readonly successToast: Locator;
  readonly errorToast: Locator;

  get cartBadge(): Locator {
    return this.page.locator('[data-testid="cart-badge"]');
  }

  get cartItem(): Locator {
    return this.page.locator('[data-testid="cart-item"]');
  }

  readonly cartIcon: Locator;

  constructor(page: Page) {
    this.page = page;

    this.productCard = page.locator('[data-testid="product-card"]');
    this.productName = page.locator('[data-testid="product-name"]');
    this.productPrice = page.locator('[data-testid="product-price"]');
    this.quantityInput = page.locator('[data-testid="quantity-input"]');
    this.addToCartBtn = page.locator('[data-testid="add-to-cart-btn"]');

    this.successToast = page.locator('[data-testid="success-toast"]');
    this.errorToast = page.locator('[data-testid="error-toast"]');

    this.cartIcon = page.locator('[data-testid="cart-icon"]');
  }


  async goToProducts() {
    await this.page.goto('/authenticated/products');
    await this.page.waitForLoadState('networkidle');
  }

 
  async getFirstAvailableProduct(): Promise<Locator | null> {
    const cards = this.productCard;
    await cards.first().waitFor({ state: 'visible', timeout: 10000 });
    const count = await cards.count();

    for (let i = 0; i < count; i++) {
      const card = cards.nth(i);
      try {
        const addBtn = card.locator('[data-testid="add-to-cart-btn"]');
        const isEnabled = await addBtn.isEnabled({ timeout: 1000 });
        
        if (isEnabled) {
          // Thêm logic: Ưu tiên sản phẩm có stock > 5 để tránh flaky khi chạy nhiều test
          const stockElement = card.locator('text=/Có sẵn/');
          if (await stockElement.isVisible()) {
            const text = await stockElement.innerText();
            const match = text.match(/\d+/);
            if (match && parseInt(match[0]) > 5) {
              return card;
            }
          }
        }
      } catch {
        continue;
      }
    }

    return cards.first();
  }

  
  async addProductToCart(quantity: number = 1) {
    const product = await this.getFirstAvailableProduct();
    if (!product) {
      throw new Error('No available product found');
    }

    const quantityInput = product.locator('[data-testid="quantity-input"]');
    if (await quantityInput.isVisible().catch(() => false)) {
      await quantityInput.fill(quantity.toString());
    }

    const addBtn = product.locator('[data-testid="add-to-cart-btn"]');
    await addBtn.click();

    await expect(this.page.locator('.Toastify__toast--success')).toBeVisible({ timeout: 10000 });
    
    await expect(this.page.locator('[data-testid="cart-badge"]')).toBeVisible({ timeout: 5000 });
    
    await this.page.waitForTimeout(1000);
  }

  
  async getFirstProductName(): Promise<string> {
    const product = await this.getFirstAvailableProduct();
    if (!product) {
      throw new Error('No available product found');
    }
    return await product.locator('[data-testid="product-name"]').innerText();
  }


  async goToCartAndWait() {
    await this.cartIcon.click();
    await this.page.waitForURL('**/cart', { timeout: 5000 });
    await this.page.waitForLoadState('networkidle');
    // Wait for a cart item to be rendered
    await this.page.locator('[data-testid="cart-item"]').first().waitFor({ state: 'visible', timeout: 10000 }).catch(() => {});
  }

  async goToCart() {
    await this.goToCartAndWait();
  }
}

export default CartPage;
