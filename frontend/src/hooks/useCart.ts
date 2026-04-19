import { useState, useCallback } from 'react';
import { cartService } from '../services/api/cartService';
import { CartResponse, CartItemRequest } from '../types/cart';
import { useAsync } from './useAsync';

export function useCart(userId: string) {
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

  const addItem = useCallback(
    async (item: CartItemRequest) => {
      try {
        const response = await cartService.addToCart(userId, item);
        setCart(response);
        setError(null);
      } catch (err) {
        setError((err as Error).message);
      }
    },
    [userId]
  );

  const removeItem = useCallback(
    async (cartItemId: number) => {
      try {
        const response = await cartService.removeFromCart(userId, cartItemId);
        setCart(response);
        setError(null);
      } catch (err) {
        setError((err as Error).message);
      }
    },
    [userId]
  );

  const updateItem = useCallback(
    async (cartItemId: number, quantity: number) => {
      try {
        const response = await cartService.updateCartItem(userId, cartItemId, quantity);
        setCart(response);
        setError(null);
      } catch (err) {
        setError((err as Error).message);
      }
    },
    [userId]
  );

  const clear = useCallback(async () => {
    try {
      await cartService.clearCart(userId);
      setCart(null);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  }, [userId]);

  return {
    cart,
    isLoading,
    error,
    fetchCart,
    addItem,
    removeItem,
    updateItem,
    clear,
  };
}
