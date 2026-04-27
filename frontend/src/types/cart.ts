export interface CartItem {
  id: number;
  productId: string;
  quantity: number;
}

export interface CartItemRequest {
  productId: string;
  quantity: number;
}

export interface CartItemResponse {
  id: number;
  productId: string;
  quantity: number;
  price: number;
}

export interface CartResponse {
  userId: string;
  items: CartItemResponse[];
  totalItems: number;
  totalPrice: number;
}
