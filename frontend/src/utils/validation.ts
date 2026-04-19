/**
 * Validate email format
 */
export function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Validate phone number (10 digits)
 */
export function isValidPhone(phone: string): boolean {
  const phoneRegex = /^[0-9]{10}$/;
  return phoneRegex.test(phone.replace(/\D/g, ''));
}

/**
 * Validate quantity (must be positive integer)
 */
export function isValidQuantity(quantity: number): boolean {
  return Number.isInteger(quantity) && quantity > 0;
}

/**
 * Validate price (must be positive)
 */
export function isValidPrice(price: number): boolean {
  return typeof price === 'number' && price > 0;
}

/**
 * Validate zip code (5 digits)
 */
export function isValidZipCode(zipCode: string): boolean {
  const zipRegex = /^[0-9]{5}$/;
  return zipRegex.test(zipCode.replace(/\D/g, ''));
}

/**
 * Validate product ID (not empty)
 */
export function isValidProductId(productId: string): boolean {
  return productId.trim().length > 0;
}

/**
 * Validate user ID (not empty)
 */
export function isValidUserId(userId: string): boolean {
  return userId.trim().length > 0;
}
