import React, { createContext, useState, useCallback, useEffect } from 'react';
import { cartService } from '../services/api/cartService';
import { AxiosError } from 'axios';
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

const getCurrentUserId = () => localStorage.getItem("userId");

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchCart = useCallback(async () => {
    const userId = getCurrentUserId();
    if (!userId) {
      setCart(null);
      return;
    }

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
  }, []);

  const addItem = useCallback(async (item: CartItemRequest) => {
    const userId = getCurrentUserId();
    if (!userId) {
      throw new Error("Vui l�ng dang nh?p d? th�m s?n ph?m v�o gi? h�ng.");
    }

    try {
      const response = await cartService.addToCart(userId, item);
      setCart(response);
      setError(null);
    } catch (err) {
      const errorMessage = (err as AxiosError<{message: string}>).response?.data?.message || (err as Error).message;
      setError(errorMessage);
      throw new Error(errorMessage);
    }
  }, []);

  const removeItem = useCallback(async (cartItemId: number) => {
    const userId = getCurrentUserId();
    if (!userId) {
      throw new Error("Vui l�ng dang nh?p d? thao t�c gi? h�ng.");
    }

    try {
      const response = await cartService.removeFromCart(userId, cartItemId);
      setCart(response);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  }, []);

  const updateItem = useCallback(async (cartItemId: number, quantity: number) => {
    const userId = getCurrentUserId();
    if (!userId) {
      throw new Error("Vui l�ng dang nh?p d? thao t�c gi? h�ng.");
    }

    try {
      const response = await cartService.updateCartItem(userId, cartItemId, quantity);
      setCart(response);
      setError(null);
    } catch (err) {
      const errorMessage = (err as AxiosError<{message: string}>).response?.data?.message || (err as Error).message;
      setError(errorMessage);
      throw new Error(errorMessage);
    }
  }, []);

  const clear = useCallback(async () => {
    const userId = getCurrentUserId();
    if (!userId) {
      setCart(null);
      return;
    }

    await cartService.clearCart(userId);
    setCart({ userId, items: [], totalItems: 0, totalPrice: 0 });
    setError(null);
  }, []);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  return (
    <CartContext.Provider value={{ cart, isLoading, error, fetchCart, addItem, removeItem, updateItem, clear }}>
      {children}
    </CartContext.Provider>
  );
};
