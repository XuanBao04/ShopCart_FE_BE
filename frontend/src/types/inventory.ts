// src/types/inventory.ts

export interface InventoryItem {
  id: string;
  productId: string;
  quantity: number; // Thực tế
  reservedQuantity: number; // Đang giữ
  soldQuantity: number; // Đã bán
  availableQuantity: number; // Đặt khả dụng
}
