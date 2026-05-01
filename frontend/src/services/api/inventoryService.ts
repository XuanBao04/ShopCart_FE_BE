import apiClient from "./apiClient";
import { InventoryItem } from "../../types/inventory";
const INVENTORY_API = "/inventory";

export const inventoryService = {
  /**
   * Get all inventory items for a product
   * @param productId the product ID
   * @return list of inventory items
   */
  async getInventoryItems(productId: string): Promise<InventoryItem[]> {
    const response = await apiClient.get<InventoryItem[]>(
      `${INVENTORY_API}/${productId}`,
    );
    return response.data;
  },

  /**
   * Update an inventory item
   * @param productId the product ID
   * @param quantity the quantity to update
   * @return updated inventory item
   */
  async updateInventoryItem(
    productId: string,
    quantity: number,
  ): Promise<InventoryItem> {
    const response = await apiClient.patch<InventoryItem>(
      `${INVENTORY_API}/${productId}`,
      { quantity },
    );
    return response.data;
  },

  /**
   * Delete an inventory item
   * @param productId the product ID
   * @return deleted inventory item
   */
  async deleteInventoryItem(productId: string): Promise<InventoryItem> {
    const response = await apiClient.delete<InventoryItem>(
      `${INVENTORY_API}/${productId}`,
    );
    return response.data;
  },
};
