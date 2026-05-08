import { calculateDiscount } from './priceCalculation';

export interface CartItemInput {
  productId: string;
  quantity: number | null | undefined;
  stock: number;
}

export interface ValidationResult {
  isValid: boolean;
  error: string | null;
}

export interface CartItemForTotal {
  price: number;
  quantity: number;
}

const VALID_DISCOUNT_CODES: Record<string, number> = {
  SALE10: 10,
  SALE20: 20,
  SAVE50: 50,
};

export function validateCartItem(item: CartItemInput): ValidationResult {
  const { quantity, stock } = item;

  if (quantity === null || quantity === undefined) {
    return { isValid: false, error: 'Số lượng không được để trống' };
  }

  if (!Number.isFinite(quantity) || !Number.isInteger(quantity)) {
    return { isValid: false, error: 'Số lượng phải là số nguyên hợp lệ' };
  }

  if (quantity <= 0) {
    return { isValid: false, error: 'Số lượng phải lớn hơn 0' };
  }

  if (quantity > stock) {
    return { isValid: false, error: 'Số lượng vượt quá tồn kho' };
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
