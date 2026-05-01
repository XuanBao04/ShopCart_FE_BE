export interface CouponRequest {
  code: string;
  discountPercent: number;
  active?: boolean;
  minimumOrderAmount?: number;
  expiryDate?: string;
}

export interface UpdateCouponRequest {
  discountPercent?: number;
  active?: boolean;
  minimumOrderAmount?: number;
  expiryDate?: string;
}

export interface CouponResponse {
  code: string;
  discountPercent: number;
  active: boolean;
  minimumOrderAmount: number;
  expiryDate: string | null;
  createdAt: string;
  updatedAt: string;
}
