import apiClient from "./apiClient";
import { CartResponse, CartItemRequest } from "../../types/cart";

const CART_API = "/cart";

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
  async addToCart(
    userId: string,
    item: CartItemRequest,
  ): Promise<CartResponse> {
    const response = await apiClient.post<CartResponse>(
      `${CART_API}/${userId}/add`,
      item,
    );
    return response.data;
  },

  /**
   * Remove item from cart
   */
  async removeFromCart(
    userId: string,
    cartItemId: number,
  ): Promise<CartResponse> {
    const response = await apiClient.delete<CartResponse>(
      `${CART_API}/${userId}/items/${cartItemId}`,
    );
    return response.data;
  },

  /**
   * Update cart item quantity
   */
  async updateCartItem(
    userId: string,
    cartItemId: number,
    quantity: number,
  ): Promise<CartResponse> {
    try {
      const response = await apiClient.patch<CartResponse>(
        `${CART_API}/${userId}/items/${cartItemId}`,
        { quantity: quantity },
      );
      return response.data;
    } catch (error) {
      console.error("Error updating cart item quantity:", error);
      throw new Error("Failed to update cart item quantity");
    }
  },

  /**
   * Clear entire cart
   */
  async clearCart(userId: string): Promise<void> {
    await apiClient.delete(`${CART_API}/${userId}`);
  },
};
