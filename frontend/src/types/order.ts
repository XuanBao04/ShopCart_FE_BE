export interface OrderItem {
  productId: string;
  quantity: number;
  price: number;
}

export interface OrderItemRequest {
  productId: string;
  quantity: number;
  price: number;
}

export interface OrderItemResponse {
  id: number;
  productId: string;
  quantity: number;
  price: number;
  name: string;
}

export interface ShippingAddress {
  shippingAddress: string;
  city: string;
  district: string;
  ward: string;
  postalCode?: string;
  phoneNumber: string;
}

export interface OrderRequest extends ShippingAddress {
  userId: string;
  orderItems: OrderItemRequest[];
  couponCode?: string;
}

export interface OrderResponse extends ShippingAddress {
  id: string;
  userId: string;
  items: OrderItemResponse[];
  status: 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  createdAt: string;
  lastModifiedDate: string;
  subtotal: number;
  discountAmount: number;
  couponCode?: string;
  totalPrice: number;
  shippingFee: number;
}

export interface OrderPreviewResponse {
  userId: string;
  items: OrderItemResponse[];
  subtotal: number;
  discountAmount: number;
  couponCode?: string;
  shippingFee: number;
  totalPrice: number;
}

export interface Coupon {
  code: string;
  discountPercent: number;
  active: boolean;
  minimumOrderAmount: number;
  expiryDate?: string;
  createdAt: string;
  updatedAt: string;
}
