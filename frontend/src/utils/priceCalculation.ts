export interface OrderItem {
  price: number;
  quantity: number;
}

export interface Coupon {
  type: 'percent' | 'fixed';
  value: number;
}

export interface OrderPriceResult {
  subtotal: number;
  discount: number;
  shipping: number;
  total: number;
}

export interface CartItemForInventory {
  productId: string;
  quantity: number;
}

export interface InventoryStock {
  productId: string;
  quantity: number;
}

export interface InventoryAvailabilityResult {
  available: boolean;
  unavailableItems: string[];
}

export function calculateOrderPrice(
  items: OrderItem[],
  coupon: Coupon | null,
  shippingFee: number = 0
): OrderPriceResult {
  const subtotal = items.reduce(
    (total, item) => total + item.price * item.quantity,
    0
  );

  let discount = 0;
  if (coupon) {
    if (coupon.type === 'percent') {
      discount = Math.round((subtotal * coupon.value) / 100);
    } else if (coupon.type === 'fixed') {
      discount = Math.min(coupon.value, subtotal);
    }
  }

  return {
    subtotal,
    discount,
    shipping: shippingFee,
    total: subtotal + shippingFee - discount,
  };
}

export function checkInventoryAvailability(
  cartItems: CartItemForInventory[],
  inventory: InventoryStock[]
): InventoryAvailabilityResult {
  const stockMap = new Map(inventory.map((item) => [item.productId, item.quantity]));

  const unavailableItems = cartItems
    .filter((cartItem) => cartItem.quantity > (stockMap.get(cartItem.productId) ?? 0))
    .map((cartItem) => cartItem.productId);

  return { available: unavailableItems.length === 0, unavailableItems };
}

/**
 * Calculate subtotal (price * quantity)
 */
export function calculateSubtotal(price: number, quantity: number): number {
  return price * quantity;
}

/**
 * Calculate tax (subtotal * taxRate)
 */
export function calculateTax(subtotal: number, taxRate: number = 0.1): number {
  return Math.round(subtotal * taxRate);
}

/**
 * Calculate discount from coupon
 */
export function calculateDiscount(subtotal: number, discountPercent: number): number {
  return Math.round((subtotal * discountPercent) / 100);
}

/**
 * Calculate final total
 */
export function calculateTotal(
  subtotal: number,
  tax: number = 0,
  shippingFee: number = 0,
  discount: number = 0
): number {
  return subtotal + tax + shippingFee - discount;
}

/**
 * Format price to VND currency
 */
export function formatPrice(price: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
}

/**
 * Parse price string to number
 */
export function parsePrice(priceString: string): number {
  return parseInt(priceString.replace(/\D/g, ''), 10) || 0;
}

/**
 * Calculate cart total
 */
export function calculateCartTotal(
  items: Array<{ price: number; quantity: number }>
): number {
  return items.reduce((total, item) => total + item.price * item.quantity, 0);
}
