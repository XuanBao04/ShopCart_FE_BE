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
}

export interface OrderRequest {
  userId: string;
  orderItems: OrderItemRequest[];
}

export interface OrderResponse {
  id: string;
  userId: string;
  items: OrderItemResponse[];
  status: 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  createdDate: string;
  lastModifiedDate: string;
}
