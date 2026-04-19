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
