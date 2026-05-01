import { useContext } from "react";
import { CartContext, CartContextType } from "../context/CartContext";

export function useCart(userId?: string): CartContextType {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within a CartProvider");
  }
  return context;
}
