import { formatPrice } from "../../utils/priceCalculation";

interface PriceBreakdownProps {
  subtotal: number;
  discountAmount: number;
  couponCode?: string;
  shippingFee: number;
  totalPrice: number;
  compact?: boolean;
}

export default function PriceBreakdown({
  subtotal,
  discountAmount,
  couponCode,
  shippingFee,
  totalPrice,
  compact = false,
}: PriceBreakdownProps) {
  if (compact) {
    return (
      <div className="space-y-2 text-sm">
        <div className="flex justify-between">
          <span>Tổng hàng:</span>
          <span data-testid="subtotal-display">{formatPrice(subtotal)} VND</span>
        </div>

        {discountAmount > 0 && (
          <div className="flex justify-between text-green-600 font-semibold">
            <span>Giảm giá {couponCode ? `(${couponCode})` : ""}:</span>
            <span data-testid="discount-display">-{formatPrice(discountAmount)} VND</span>
          </div>
        )}

        <div className="flex justify-between">
          <span>Phí vận chuyển:</span>
          <span data-testid="shipping-fee-display">{formatPrice(shippingFee)} VND</span>
        </div>

        <div className="flex justify-between border-t pt-2 font-bold">
          <span>Tổng cộng:</span>
          <span className="text-lg" data-testid="total-price-display">{formatPrice(totalPrice)} VND</span>
        </div>
      </div>
    );
  }

  return (
    <div className="border rounded-lg p-4 bg-gray-50">
      <h3 className="text-lg font-bold mb-4">Chi tiết giá</h3>

      <div className="space-y-3">
        <div className="flex justify-between text-base">
          <span className="text-gray-700">Tổng hàng hóa:</span>
          <span className="font-semibold" data-testid="subtotal-display">{formatPrice(subtotal)} VND</span>
        </div>

        {discountAmount > 0 && (
          <div className="flex justify-between text-base bg-green-50 p-2 rounded border-l-4 border-green-500">
            <span className="text-green-700 font-semibold">
              Giảm giá {couponCode && `mã ${couponCode}`}:
            </span>
            <span className="text-green-700 font-bold" data-testid="discount-display">
              -{formatPrice(discountAmount)} VND
            </span>
          </div>
        )}

        <div className="flex justify-between text-base">
          <span className="text-gray-700">Phí vận chuyển:</span>
          <span className="font-semibold" data-testid="shipping-fee-display">{formatPrice(shippingFee)} VND</span>
        </div>

        <div className="border-t-2 border-gray-300 pt-3 flex justify-between text-xl">
          <span className="font-bold">Tổng thanh toán:</span>
          <span className="font-bold text-blue-600" data-testid="total-price-display">
            {formatPrice(totalPrice)} VND
          </span>
        </div>
      </div>
    </div>
  );
}
