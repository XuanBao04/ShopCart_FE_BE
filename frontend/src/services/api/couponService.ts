import apiClient from "./apiClient";
import { Coupon, OrderRequest, OrderPreviewResponse } from "../../types/order";
import { CouponResponse, CouponRequest, UpdateCouponRequest } from "../../types/coupon";

const COUPON_API = "/api/coupons";
const ORDER_API = "/api/orders";

export const couponService = {
  /**
   * Get coupon by code
   */
  async getCoupon(code: string): Promise<Coupon> {
    const response = await apiClient.get<Coupon>(`${COUPON_API}/${code}`);
    return response.data;
  },

  /**
   * Get all coupons (Admin only)
   */
  async getAllCoupons(): Promise<CouponResponse[]> {
    const response = await apiClient.get<CouponResponse[]>(`${COUPON_API}`);
    return response.data;
  },

  /**
   * Validate if coupon is valid and active
   */
  async validateCoupon(code: string): Promise<boolean> {
    try {
      const response = await apiClient.get<boolean>(
        `${COUPON_API}/${code}/validate`
      );
      return response.data;
    } catch {
      return false;
    }
  },

  /**
   * Calculate discount amount for coupon and order amount
   */
  async calculateDiscount(code: string, orderAmount: number): Promise<number> {
    try {
      const response = await apiClient.get<number>(
        `${COUPON_API}/${code}/discount?orderAmount=${orderAmount}`
      );
      return response.data;
    } catch {
      return 0;
    }
  },

  /**
   * Create new coupon (Admin only)
   */
  async createCoupon(request: CouponRequest): Promise<CouponResponse> {
    const response = await apiClient.post<CouponResponse>(
      COUPON_API,
      request
    );
    return response.data;
  },

  /**
   * Update coupon (Admin only)
   */
  async updateCoupon(code: string, request: UpdateCouponRequest): Promise<CouponResponse> {
    const response = await apiClient.put<CouponResponse>(
      `${COUPON_API}/${code}`,
      request
    );
    return response.data;
  },

  /**
   * Delete coupon (Admin only)
   */
  async deleteCoupon(code: string): Promise<void> {
    await apiClient.delete(`${COUPON_API}/${code}`);
  },

  /**
   * Preview order with coupon applied
   */
  async previewOrder(
    request: OrderRequest
  ): Promise<OrderPreviewResponse> {
    const response = await apiClient.post<OrderPreviewResponse>(
      `${ORDER_API}/preview`,
      request
    );
    return response.data;
  },
};
