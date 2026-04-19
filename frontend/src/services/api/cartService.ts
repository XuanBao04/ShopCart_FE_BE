import apiClient from './apiClient';
import { CartResponse, CartItemRequest } from '../../types/cart';

const CART_API = '/cart';

export const cartService = {
  /**
   * Get cart for user
   */
  async getCart(userId: string): Promise<CartResponse> {
    const response = await apiClient.get<CartResponse>(`${CART_API}/${userId}`);
    return response.data;
  },

  /**
   * Add item to cart
   */
  async addToCart(userId: string, item: CartItemRequest): Promise<CartResponse> {
    const response = await apiClient.post<CartResponse>(`${CART_API}/${userId}/add`, item);
    return response.data;
  },

  /**
   * Remove item from cart
   */
  async removeFromCart(userId: string, cartItemId: number): Promise<CartResponse> {
    const response = await apiClient.delete<CartResponse>(
      `${CART_API}/${userId}/items/${cartItemId}`
    );
    return response.data;
  },

  /**
   * Update cart item quantity
   */
  async updateCartItem(
    userId: string,
    cartItemId: number,
    quantity: number
  ): Promise<CartResponse> {
    const response = await apiClient.put<CartResponse>(
      `${CART_API}/${userId}/items/${cartItemId}`,
      null,
      { params: { quantity } }
    );
    return response.data;
  },

  /**
   * Clear entire cart
   */
  async clearCart(userId: string): Promise<void> {
    await apiClient.delete(`${CART_API}/${userId}`);
  },
};
