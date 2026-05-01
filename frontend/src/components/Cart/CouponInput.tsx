import { useState } from "react";
import { couponService } from "../../services/api/couponService";

interface CouponInputProps {
  onCouponApply: (couponCode: string | null) => void;
  onDiscountChange: (discount: number) => void;
  orderAmount: number;
}

export default function CouponInput({
  onCouponApply,
  onDiscountChange,
  orderAmount,
}: CouponInputProps) {
  const [couponCode, setCouponCode] = useState("");
  const [isValidating, setIsValidating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [appliedCoupon, setAppliedCoupon] = useState<string | null>(null);

  const handleValidateCoupon = async () => {
    if (!couponCode.trim()) {
      setError("Vui lòng nhập mã giảm giá");
      return;
    }

    setIsValidating(true);
    setError(null);
    setSuccess(false);

    try {
      // Check if coupon is valid
      const isValid = await couponService.validateCoupon(couponCode);

      if (!isValid) {
        setError("Mã giảm giá không hợp lệ hoặc đã hết hạn");
        onCouponApply(null);
        onDiscountChange(0);
        return;
      }

      // Calculate discount
      const discount = await couponService.calculateDiscount(
        couponCode,
        orderAmount
      );

      if (discount > 0) {
        setSuccess(true);
        setAppliedCoupon(couponCode);
        onCouponApply(couponCode);
        onDiscountChange(discount);
        setError(null);
      } else {
        setError(
          "Đơn hàng không đủ điều kiện để áp dụng mã giảm giá này"
        );
        onCouponApply(null);
        onDiscountChange(0);
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "Lỗi khi kiểm tra mã giảm giá";
      setError(errorMessage);
      onCouponApply(null);
      onDiscountChange(0);
    } finally {
      setIsValidating(false);
    }
  };

  const handleRemoveCoupon = () => {
    setCouponCode("");
    setAppliedCoupon(null);
    setError(null);
    setSuccess(false);
    onCouponApply(null);
    onDiscountChange(0);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      handleValidateCoupon();
    }
  };

  return (
    <div className="border rounded-lg p-4 bg-blue-50 mb-4">
      <h3 className="font-semibold text-lg mb-3">Mã giảm giá</h3>

      {!appliedCoupon ? (
        <div className="flex gap-2">
          <input
            type="text"
            value={couponCode}
            onChange={(e) => {
              setCouponCode(e.target.value.toUpperCase());
              setError(null);
            }}
            onKeyPress={handleKeyPress}
            placeholder="Nhập mã giảm giá..."
            className="flex-1 px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={isValidating}
          />
          <button
            onClick={handleValidateCoupon}
            disabled={isValidating || !couponCode.trim()}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
          >
            {isValidating ? "Đang kiểm tra..." : "Áp dụng"}
          </button>
        </div>
      ) : (
        <div className="flex items-center justify-between bg-green-100 border border-green-300 rounded p-3">
          <div className="flex items-center gap-2">
            <span className="text-green-700 font-semibold text-lg">✓</span>
            <span className="text-green-800">
              Đã áp dụng mã: <strong>{appliedCoupon}</strong>
            </span>
          </div>
          <button
            onClick={handleRemoveCoupon}
            className="text-sm text-green-600 hover:text-green-800 underline"
          >
            Hủy
          </button>
        </div>
      )}

      {error && (
        <div className="mt-2 text-red-600 text-sm bg-red-50 p-2 rounded">
          ⚠️ {error}
        </div>
      )}

      {success && !error && (
        <div className="mt-2 text-green-600 text-sm bg-green-50 p-2 rounded">
          ✓ Mã giảm giá đã được áp dụng thành công!
        </div>
      )}
    </div>
  );
}
