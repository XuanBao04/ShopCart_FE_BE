import { useState, useCallback } from "react";
import { couponService } from "../services/api/couponService";
import { OrderRequest, OrderPreviewResponse } from "../types/order";

export const useCoupon = () => {
  const [couponCode, setCouponCode] = useState<string | null>(null);
  const [discountAmount, setDiscountAmount] = useState(0);
  const [isValidating, setIsValidating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const validateCoupon = useCallback(
    async (code: string, orderAmount: number) => {
      if (!code) {
        setCouponCode(null);
        setDiscountAmount(0);
        setError(null);
        return false;
      }

      setIsValidating(true);
      setError(null);

      try {
        // First validate the coupon
        const isValid = await couponService.validateCoupon(code);

        if (!isValid) {
          setError("Mã giảm giá không hợp lệ hoặc đã hết hạn");
          setCouponCode(null);
          setDiscountAmount(0);
          setIsValidating(false);
          return false;
        }

        // Calculate discount
        const discount = await couponService.calculateDiscount(code, orderAmount);

        if (discount > 0) {
          setCouponCode(code);
          setDiscountAmount(discount);
          setError(null);
          setIsValidating(false);
          return true;
        } else {
          setError(
            "Đơn hàng không đủ điều kiện để áp dụng mã giảm giá này"
          );
          setCouponCode(null);
          setDiscountAmount(0);
          setIsValidating(false);
          return false;
        }
      } catch (err: any) {
        const errorMessage = err instanceof Error ? err.message : "Lỗi khi kiểm tra mã giảm giá";
        setError(errorMessage);
        setCouponCode(null);
        setDiscountAmount(0);
        setIsValidating(false);
        return false;
      }
    },
    []
  );

  const removeCoupon = useCallback(() => {
    setCouponCode(null);
    setDiscountAmount(0);
    setError(null);
  }, []);

  return {
    couponCode,
    discountAmount,
    isValidating,
    error,
    validateCoupon,
    removeCoupon,
  };
};
