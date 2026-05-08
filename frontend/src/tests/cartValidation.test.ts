import { describe, test, expect } from 'vitest';
import {
  validateCartItem,
  calculateCartTotal,
} from '@utils/cartValidation';

// ─── a) validateCartItem() ────────────────────────────────────────────────────

describe('validateCartItem()', () => {
  // Quantity rỗng / null / undefined
  describe('Quantity rỗng / null / undefined', () => {
    test('TC01: quantity = null → trả về lỗi "Số lượng không được để trống"', () => {
      const result = validateCartItem({ productId: 'P001', quantity: null, stock: 10 });
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Số lượng không được để trống');
    });

    test('TC02: quantity = undefined → trả về lỗi "Số lượng không được để trống"', () => {
      const result = validateCartItem({ productId: 'P001', quantity: undefined, stock: 10 });
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Số lượng không được để trống');
    });
  });

  // Quantity âm hoặc bằng 0
  describe('Quantity âm hoặc bằng 0', () => {
    test('TC03: quantity = 0 → trả về lỗi "Số lượng phải lớn hơn 0"', () => {
      const result = validateCartItem({ productId: 'P001', quantity: 0, stock: 10 });
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Số lượng phải lớn hơn 0');
    });

    test('TC04: quantity = -1 → trả về lỗi "Số lượng phải lớn hơn 0"', () => {
      const result = validateCartItem({ productId: 'P001', quantity: -1, stock: 10 });
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Số lượng phải lớn hơn 0');
    });

    test('TC05: quantity = -100 → trả về lỗi "Số lượng phải lớn hơn 0"', () => {
      const result = validateCartItem({ productId: 'P001', quantity: -100, stock: 10 });
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Số lượng phải lớn hơn 0');
    });
  });

  // Quantity vượt quá tồn kho
  describe('Quantity vượt quá tồn kho', () => {
    test('TC06: quantity = 11 > stock = 10 → trả về lỗi "Số lượng vượt quá tồn kho"', () => {
      const result = validateCartItem({ productId: 'P001', quantity: 11, stock: 10 });
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Số lượng vượt quá tồn kho');
    });

    test('TC07: quantity = 100 > stock = 5 → trả về lỗi "Số lượng vượt quá tồn kho"', () => {
      const result = validateCartItem({ productId: 'P002', quantity: 100, stock: 5 });
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Số lượng vượt quá tồn kho');
    });
  });

  // Quantity hợp lệ
  describe('Quantity hợp lệ', () => {
    test('TC08: quantity = 1 ≤ stock = 10 → hợp lệ, không có lỗi', () => {
      const result = validateCartItem({ productId: 'P001', quantity: 1, stock: 10 });
      expect(result.isValid).toBe(true);
      expect(result.error).toBeNull();
    });

    test('TC09: quantity = 5 ≤ stock = 10 → hợp lệ, không có lỗi', () => {
      const result = validateCartItem({ productId: 'P001', quantity: 5, stock: 10 });
      expect(result.isValid).toBe(true);
      expect(result.error).toBeNull();
    });

    test('TC10: quantity = stock = 10 (đúng giới hạn) → hợp lệ', () => {
      const result = validateCartItem({ productId: 'P001', quantity: 10, stock: 10 });
      expect(result.isValid).toBe(true);
      expect(result.error).toBeNull();
    });
  });
});

// ─── b) calculateCartTotal() ──────────────────────────────────────────────────

describe('calculateCartTotal()', () => {
  // Giỏ hàng rỗng
  test('TC11: Giỏ hàng rỗng → trả về 0', () => {
    expect(calculateCartTotal([])).toBe(0);
  });

  // Tính tổng giá đúng với nhiều sản phẩm
  describe('Tính tổng giá với nhiều sản phẩm', () => {
    test('TC12: 1 sản phẩm → price * quantity', () => {
      const items = [{ price: 100_000, quantity: 2 }];
      expect(calculateCartTotal(items)).toBe(200_000);
    });

    test('TC13: Nhiều sản phẩm → tổng đúng (100.000×2 + 50.000×3 = 350.000)', () => {
      const items = [
        { price: 100_000, quantity: 2 },
        { price: 50_000, quantity: 3 },
      ];
      expect(calculateCartTotal(items)).toBe(350_000);
    });

    test('TC14: Ba sản phẩm → tổng đúng', () => {
      const items = [
        { price: 200_000, quantity: 1 },
        { price: 150_000, quantity: 2 },
        { price: 80_000, quantity: 3 },
      ];
      expect(calculateCartTotal(items)).toBe(740_000);
    });
  });

  // Áp dụng mã giảm giá
  describe('Áp dụng mã giảm giá', () => {
    test('TC15: Mã "SALE10" giảm 10% → 200.000 × 90% = 180.000', () => {
      const items = [{ price: 100_000, quantity: 2 }];
      expect(calculateCartTotal(items, 'SALE10')).toBe(180_000);
    });

    test('TC16: Mã "SALE20" giảm 20% → 500.000 × 80% = 400.000', () => {
      const items = [
        { price: 100_000, quantity: 3 },
        { price: 200_000, quantity: 1 },
      ];
      expect(calculateCartTotal(items, 'SALE20')).toBe(400_000);
    });

    test('TC17: Mã không hợp lệ → không giảm giá, trả về tổng gốc', () => {
      const items = [{ price: 100_000, quantity: 2 }];
      expect(calculateCartTotal(items, 'INVALID')).toBe(200_000);
    });
  });

  // Tổng giá sau khi xóa sản phẩm
  describe('Tổng giá sau khi xóa sản phẩm', () => {
    test('TC18: Xóa 1 trong 2 sản phẩm → tổng chỉ còn sản phẩm còn lại', () => {
      const items = [
        { price: 100_000, quantity: 2 },
        { price: 50_000, quantity: 1 },
      ];
      const afterRemoval = items.filter((_, index) => index !== 1);
      expect(calculateCartTotal(afterRemoval)).toBe(200_000);
    });

    test('TC19: Xóa tất cả sản phẩm → giỏ hàng rỗng trả về 0', () => {
      const items = [{ price: 100_000, quantity: 2 }];
      const afterRemoval = items.filter(() => false);
      expect(calculateCartTotal(afterRemoval)).toBe(0);
    });

    test('TC20: Xóa sản phẩm đắt nhất → tổng giảm đúng', () => {
      const items = [
        { price: 500_000, quantity: 1 },
        { price: 100_000, quantity: 2 },
        { price: 50_000, quantity: 3 },
      ];
      const afterRemoval = items.filter((_, index) => index !== 0);
      expect(calculateCartTotal(afterRemoval)).toBe(350_000);
    });
  });
});
