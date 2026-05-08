import { describe, test, expect } from 'vitest';
import {
  calculateOrderPrice,
  checkInventoryAvailability,
  calculateSubtotal,
  calculateTax,
  calculateDiscount,
  calculateTotal,
  formatPrice,
  parsePrice,
} from '@utils/priceCalculation';
import { calculateCartTotal } from '@utils/cartValidation';

// ─── a) calculateOrderPrice() ────────────────────────────────────────────────

describe('calculateOrderPrice()', () => {

  // Tính tổng giá trước giảm giá
  describe('Tính tổng giá trước giảm giá', () => {
    test('TC01: Nhiều sản phẩm, không coupon → subtotal đúng, discount = 0', () => {
      const items = [
        { price: 15_000_000, quantity: 2 },
        { price: 500_000, quantity: 1 },
      ];
      const result = calculateOrderPrice(items, null, 50_000);
      expect(result.subtotal).toBe(30_500_000);
      expect(result.discount).toBe(0);
      expect(result.shipping).toBe(50_000);
      expect(result.total).toBe(30_550_000);
    });

    test('TC02: 1 sản phẩm, không coupon → subtotal = price × quantity', () => {
      const items = [{ price: 200_000, quantity: 3 }];
      const result = calculateOrderPrice(items, null, 0);
      expect(result.subtotal).toBe(600_000);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(600_000);
    });

    test('TC03: Giỏ hàng rỗng → tất cả giá trị = 0', () => {
      const result = calculateOrderPrice([], null, 30_000);
      expect(result.subtotal).toBe(0);
      expect(result.discount).toBe(0);
      expect(result.shipping).toBe(30_000);
      expect(result.total).toBe(30_000);
    });
  });

  // Áp dụng coupon giảm %
  describe('Áp dụng coupon giảm %', () => {
    test('TC04: Coupon giảm 10% → discount = subtotal × 10%', () => {
      const items = [
        { price: 100_000, quantity: 2 },
        { price: 200_000, quantity: 1 },
      ];
      const coupon = { type: 'percent' as const, value: 10 };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.subtotal).toBe(400_000);
      expect(result.discount).toBe(40_000);
      expect(result.total).toBe(360_000);
    });

    test('TC05: Coupon giảm 20% → discount = subtotal × 20%', () => {
      const items = [
        { price: 500_000, quantity: 2 },
        { price: 100_000, quantity: 1 },
      ];
      const coupon = { type: 'percent' as const, value: 20 };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.subtotal).toBe(1_100_000);
      expect(result.discount).toBe(220_000);
      expect(result.total).toBe(880_000);
    });

    test('TC06: Coupon giảm 50% → discount = subtotal × 50%', () => {
      const items = [{ price: 300_000, quantity: 4 }];
      const coupon = { type: 'percent' as const, value: 50 };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.subtotal).toBe(1_200_000);
      expect(result.discount).toBe(600_000);
      expect(result.total).toBe(600_000);
    });

    test('TC06b: Coupon percent > 100 → bị clamp về 100%, total không âm', () => {
      const items = [{ price: 100_000, quantity: 2 }];
      const coupon = { type: 'percent' as const, value: 150 };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.discount).toBe(result.subtotal);
      expect(result.total).toBe(0);
    });

    test('TC06c: Coupon percent âm → bị clamp về 0%, không giảm giá', () => {
      const items = [{ price: 100_000, quantity: 2 }];
      const coupon = { type: 'percent' as const, value: -10 };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(result.subtotal);
    });

    test('TC06d: Coupon value = NaN → bỏ qua coupon, discount = 0', () => {
      const items = [{ price: 100_000, quantity: 2 }];
      const coupon = { type: 'percent' as const, value: NaN };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(result.subtotal);
    });
  });

  // Áp dụng coupon giảm số tiền cố định
  describe('Áp dụng coupon giảm số tiền cố định', () => {
    test('TC07: Coupon giảm cố định 50.000 → discount = 50.000', () => {
      const items = [{ price: 300_000, quantity: 2 }];
      const coupon = { type: 'fixed' as const, value: 50_000 };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.subtotal).toBe(600_000);
      expect(result.discount).toBe(50_000);
      expect(result.total).toBe(550_000);
    });

    test('TC08: Coupon giảm cố định 100.000 trên đơn 500.000 + ship → total đúng', () => {
      const items = [{ price: 100_000, quantity: 5 }];
      const coupon = { type: 'fixed' as const, value: 100_000 };
      const result = calculateOrderPrice(items, coupon, 30_000);
      expect(result.subtotal).toBe(500_000);
      expect(result.discount).toBe(100_000);
      expect(result.shipping).toBe(30_000);
      expect(result.total).toBe(430_000);
    });

    test('TC09: Coupon giảm cố định lớn hơn subtotal → discount bằng subtotal, không âm', () => {
      const items = [{ price: 50_000, quantity: 1 }];
      const coupon = { type: 'fixed' as const, value: 200_000 };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.subtotal).toBe(50_000);
      expect(result.discount).toBe(50_000);
      expect(result.total).toBe(0);
    });

    test('TC09b: Coupon fixed value âm → discount = 0, không tăng total', () => {
      const items = [{ price: 200_000, quantity: 1 }];
      const coupon = { type: 'fixed' as const, value: -50_000 };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(result.subtotal);
    });

    test('TC09c: Coupon fixed value = NaN → bỏ qua coupon, discount = 0', () => {
      const items = [{ price: 200_000, quantity: 1 }];
      const coupon = { type: 'fixed' as const, value: NaN };
      const result = calculateOrderPrice(items, coupon, 0);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(result.subtotal);
    });
  });

  // Tính phí vận chuyển
  describe('Tính phí vận chuyển', () => {
    test('TC10: Phí vận chuyển 50.000 được cộng vào tổng', () => {
      const items = [{ price: 200_000, quantity: 1 }];
      const result = calculateOrderPrice(items, null, 50_000);
      expect(result.shipping).toBe(50_000);
      expect(result.total).toBe(250_000);
    });

    test('TC11: Phí vận chuyển 0 → total = subtotal', () => {
      const items = [{ price: 150_000, quantity: 2 }];
      const result = calculateOrderPrice(items, null, 0);
      expect(result.shipping).toBe(0);
      expect(result.total).toBe(300_000);
    });

    test('TC12: Phí vận chuyển mặc định (không truyền) = 0', () => {
      const items = [{ price: 100_000, quantity: 1 }];
      const result = calculateOrderPrice(items, null);
      expect(result.shipping).toBe(0);
      expect(result.total).toBe(100_000);
    });

    test('TC12b: shippingFee = NaN → được clamp về 0, total không phải NaN', () => {
      const items = [{ price: 100_000, quantity: 1 }];
      const result = calculateOrderPrice(items, null, NaN);
      expect(result.shipping).toBe(0);
      expect(result.total).toBe(100_000);
    });

    test('TC12c: shippingFee âm → được clamp về 0, total không giảm', () => {
      const items = [{ price: 100_000, quantity: 1 }];
      const result = calculateOrderPrice(items, null, -20_000);
      expect(result.shipping).toBe(0);
      expect(result.total).toBe(100_000);
    });

    test('TC12d: shippingFee = Infinity → được clamp về 0', () => {
      const items = [{ price: 100_000, quantity: 1 }];
      const result = calculateOrderPrice(items, null, Infinity);
      expect(result.shipping).toBe(0);
      expect(result.total).toBe(100_000);
    });
  });

  // Tổng cuối cùng (subtotal + shipping - discount)
  describe('Tổng cuối cùng = subtotal + shipping − discount', () => {
    test('TC13: Có coupon % và phí vận chuyển → total = subtotal + ship - discount', () => {
      const items = [
        { price: 1_000_000, quantity: 2 },
        { price: 500_000, quantity: 1 },
      ];
      const coupon = { type: 'percent' as const, value: 10 };
      const result = calculateOrderPrice(items, coupon, 50_000);
      expect(result.subtotal).toBe(2_500_000);
      expect(result.discount).toBe(250_000);
      expect(result.shipping).toBe(50_000);
      expect(result.total).toBe(2_300_000);
    });

    test('TC14: Có coupon cố định và phí vận chuyển → total = subtotal + ship - discount', () => {
      const items = [{ price: 800_000, quantity: 3 }];
      const coupon = { type: 'fixed' as const, value: 150_000 };
      const result = calculateOrderPrice(items, coupon, 40_000);
      expect(result.subtotal).toBe(2_400_000);
      expect(result.discount).toBe(150_000);
      expect(result.shipping).toBe(40_000);
      expect(result.total).toBe(2_290_000);
    });
  });
});

// ─── b) checkInventoryAvailability() ─────────────────────────────────────────

describe('checkInventoryAvailability()', () => {

  // Tất cả sản phẩm đủ tồn kho
  describe('Tất cả sản phẩm đủ tồn kho', () => {
    test('TC15: Mỗi sản phẩm đều đủ số lượng → available = true, unavailableItems rỗng', () => {
      const cartItems = [
        { productId: 'P001', quantity: 2 },
        { productId: 'P002', quantity: 5 },
      ];
      const inventory = [
        { productId: 'P001', quantity: 10 },
        { productId: 'P002', quantity: 5 },
      ];
      const result = checkInventoryAvailability(cartItems, inventory);
      expect(result.available).toBe(true);
      expect(result.unavailableItems).toHaveLength(0);
    });

    test('TC16: Số lượng đặt đúng bằng tồn kho (boundary) → available = true', () => {
      const cartItems = [{ productId: 'P001', quantity: 10 }];
      const inventory = [{ productId: 'P001', quantity: 10 }];
      const result = checkInventoryAvailability(cartItems, inventory);
      expect(result.available).toBe(true);
      expect(result.unavailableItems).toHaveLength(0);
    });

    test('TC17: Giỏ hàng rỗng → available = true', () => {
      const inventory = [{ productId: 'P001', quantity: 10 }];
      const result = checkInventoryAvailability([], inventory);
      expect(result.available).toBe(true);
      expect(result.unavailableItems).toHaveLength(0);
    });
  });

  // Sản phẩm không đủ tồn kho
  describe('Sản phẩm không đủ tồn kho', () => {
    test('TC18: Một sản phẩm vượt tồn kho → available = false, trả về productId đó', () => {
      const cartItems = [
        { productId: 'P001', quantity: 3 },
        { productId: 'P002', quantity: 15 },
      ];
      const inventory = [
        { productId: 'P001', quantity: 10 },
        { productId: 'P002', quantity: 5 },
      ];
      const result = checkInventoryAvailability(cartItems, inventory);
      expect(result.available).toBe(false);
      expect(result.unavailableItems).toContain('P002');
      expect(result.unavailableItems).toHaveLength(1);
    });

    test('TC19: Nhiều sản phẩm vượt tồn kho → trả về tất cả productId thiếu hàng', () => {
      const cartItems = [
        { productId: 'P001', quantity: 20 },
        { productId: 'P002', quantity: 10 },
        { productId: 'P003', quantity: 1 },
      ];
      const inventory = [
        { productId: 'P001', quantity: 5 },
        { productId: 'P002', quantity: 3 },
        { productId: 'P003', quantity: 10 },
      ];
      const result = checkInventoryAvailability(cartItems, inventory);
      expect(result.available).toBe(false);
      expect(result.unavailableItems).toContain('P001');
      expect(result.unavailableItems).toContain('P002');
      expect(result.unavailableItems).not.toContain('P003');
      expect(result.unavailableItems).toHaveLength(2);
    });

    test('TC20: Sản phẩm không tồn tại trong inventory (stock = 0) → unavailable', () => {
      const cartItems = [{ productId: 'P999', quantity: 1 }];
      const inventory = [{ productId: 'P001', quantity: 10 }];
      const result = checkInventoryAvailability(cartItems, inventory);
      expect(result.available).toBe(false);
      expect(result.unavailableItems).toContain('P999');
    });

    test('TC21: Hỗn hợp đủ và không đủ → chỉ trả về sản phẩm thiếu hàng', () => {
      const cartItems = [
        { productId: 'P001', quantity: 5 },
        { productId: 'P002', quantity: 2 },
        { productId: 'P003', quantity: 100 },
      ];
      const inventory = [
        { productId: 'P001', quantity: 10 },
        { productId: 'P002', quantity: 2 },
        { productId: 'P003', quantity: 50 },
      ];
      const result = checkInventoryAvailability(cartItems, inventory);
      expect(result.available).toBe(false);
      expect(result.unavailableItems).toEqual(['P003']);
    });

    test('TC22: Cùng productId xuất hiện 2 lần → cộng dồn quantity, không duplicate trong kết quả', () => {
      const cartItems = [
        { productId: 'P001', quantity: 6 },
        { productId: 'P001', quantity: 6 },
      ];
      const inventory = [{ productId: 'P001', quantity: 10 }];
      const result = checkInventoryAvailability(cartItems, inventory);
      expect(result.available).toBe(false);
      expect(result.unavailableItems).toEqual(['P001']);
      expect(result.unavailableItems).toHaveLength(1);
    });

    test('TC23: Cùng productId nhiều lần nhưng tổng quantity vừa đủ → available = true', () => {
      const cartItems = [
        { productId: 'P001', quantity: 3 },
        { productId: 'P001', quantity: 4 },
      ];
      const inventory = [{ productId: 'P001', quantity: 10 }];
      const result = checkInventoryAvailability(cartItems, inventory);
      expect(result.available).toBe(true);
      expect(result.unavailableItems).toHaveLength(0);
    });
  });
});

// ─── c) Utility helpers (coverage) ───────────────────────────────────────────

describe('Utility helpers', () => {
  test('calculateSubtotal: price × quantity', () => {
    expect(calculateSubtotal(100_000, 3)).toBe(300_000);
  });

  test('calculateTax: subtotal × taxRate mặc định 10%', () => {
    expect(calculateTax(200_000)).toBe(20_000);
    expect(calculateTax(200_000, 0.05)).toBe(10_000);
  });

  test('calculateDiscount: subtotal × percent / 100', () => {
    expect(calculateDiscount(500_000, 20)).toBe(100_000);
  });

  test('calculateTotal: subtotal + tax + ship - discount', () => {
    expect(calculateTotal(500_000, 50_000, 30_000, 100_000)).toBe(480_000);
    expect(calculateTotal(300_000)).toBe(300_000);
  });

  test('formatPrice: định dạng VND', () => {
    expect(formatPrice(100_000)).toContain('100');
  });

  test('parsePrice: tách số từ chuỗi tiền tệ', () => {
    expect(parsePrice('100.000đ')).toBe(100000);
    expect(parsePrice('abc')).toBe(0);
  });

  test('calculateCartTotal: tổng giỏ hàng', () => {
    const items = [
      { price: 100_000, quantity: 2 },
      { price: 50_000, quantity: 3 },
    ];
    expect(calculateCartTotal(items)).toBe(350_000);
    expect(calculateCartTotal([])).toBe(0);
  });
});
