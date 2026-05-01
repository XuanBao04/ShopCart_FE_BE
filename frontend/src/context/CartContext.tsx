import React, { createContext, useState, useCallback, useEffect } from 'react';
import { cartService } from '../services/api/cartService';
import { CartResponse, CartItemRequest } from '../types/cart';

export interface CartContextType {
  cart: CartResponse | null;
  isLoading: boolean;
  error: string | null;
  fetchCart: () => Promise<void>;
  addItem: (item: CartItemRequest) => Promise<void>;
  removeItem: (cartItemId: number) => Promise<void>;
  updateItem: (cartItemId: number, quantity: number) => Promise<void>;
  clear: () => Promise<void>;
}

export const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const userId = localStorage.getItem("userId") || "user1";
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchCart = useCallback(async () => {
    setIsLoading(true);
    try {
      const response = await cartService.getCart(userId);
      setCart(response);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  const addItem = useCallback(async (item: CartItemRequest) => {
    try {
      const response = await cartService.addToCart(userId, item);
      setCart(response);
      setError(null);
    } catch (err) {
      const errorMessage = (err as any).response?.data?.message || (err as Error).message;
      setError(errorMessage);
      throw new Error(errorMessage);
    }
  }, [userId]);

  const removeItem = useCallback(async (cartItemId: number) => {
    try {
      const response = await cartService.removeFromCart(userId, cartItemId);
      setCart(response);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  }, [userId]);

  const updateItem = useCallback(async (cartItemId: number, quantity: number) => {
    try {
      const response = await cartService.updateCartItem(userId, cartItemId, quantity);
      setCart(response);
      setError(null);
    } catch (err) {
      const errorMessage = (err as any).response?.data?.message || (err as Error).message;
      setError(errorMessage);
      throw new Error(errorMessage);
    }
  }, [userId]);

  const clear = useCallback(async () => {
    await cartService.clearCart(userId);
    setCart({ userId, items: [], totalItems: 0, totalPrice: 0 });
    setError(null);
  }, [userId]);

  useEffect(() => {
    if (localStorage.getItem("userId") || userId === "user1") {
      fetchCart();
    }
  }, [fetchCart, userId]);

  return (
    <CartContext.Provider value={{ cart, isLoading, error, fetchCart, addItem, removeItem, updateItem, clear }}>
      {children}
    </CartContext.Provider>
  );
};
