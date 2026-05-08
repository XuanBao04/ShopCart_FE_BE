import type { CartItemRequest, CartItemResponse } from '../types/cart';
import { calculateDiscount } from './priceCalculation';
import { ERROR_MESSAGES } from './constants';

export type CartItemInput = Pick<CartItemRequest, 'productId'> & {
  quantity: number | null | undefined;
  stock: number;
};

export interface ValidationResult {
  isValid: boolean;
  error: string | null;
}

export type CartItemForTotal = Pick<CartItemResponse, 'price' | 'quantity'>;

const VALID_DISCOUNT_CODES: Record<string, number> = {
  SALE10: 10,
  SALE20: 20,
  SAVE50: 50,
};

export function validateCartItem(item: CartItemInput): ValidationResult {
  const { quantity, stock } = item;

  if (quantity === null || quantity === undefined) {
    return { isValid: false, error: ERROR_MESSAGES.QUANTITY_REQUIRED };
  }

  if (!Number.isFinite(quantity) || !Number.isInteger(quantity)) {
    return { isValid: false, error: ERROR_MESSAGES.QUANTITY_MUST_BE_INTEGER };
  }

  if (quantity <= 0) {
    return { isValid: false, error: ERROR_MESSAGES.QUANTITY_MUST_BE_POSITIVE };
  }

  if (!Number.isFinite(stock) || !Number.isInteger(stock) || stock < 0) {
    return { isValid: false, error: ERROR_MESSAGES.INVALID_STOCK };
  }

  if (quantity > stock) {
    return { isValid: false, error: ERROR_MESSAGES.QUANTITY_EXCEEDS_STOCK };
  }

  return { isValid: true, error: null };
}

export function calculateCartTotal(
  items: CartItemForTotal[],
  discountCode?: string
): number {
  if (items.length === 0) return 0;

  const subtotal = items.reduce(
    (total, item) => total + item.price * item.quantity,
    0
  );

  if (
    discountCode &&
    Object.prototype.hasOwnProperty.call(VALID_DISCOUNT_CODES, discountCode)
  ) {
    const discountPercent = VALID_DISCOUNT_CODES[discountCode];
    return subtotal - calculateDiscount(subtotal, discountPercent);
  }

  return subtotal;
}
