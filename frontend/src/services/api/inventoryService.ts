import apiClient from "./apiClient";
import { InventoryItem } from "../../types/inventory";
const INVENTORY_API = "/api/inventory";

export const inventoryService = {
  /**
   * Get available stock for a product
   * @param productId the product ID
   * @return available quantity
   */
  async getStock(productId: string): Promise<number> {
    const response = await apiClient.get<number>(
      `${INVENTORY_API}/${productId}`,
    );
    return response.data;
  },

  /**
   * Get full inventory details for a product
   * @param productId the product ID
   * @return inventory details
   */
  async getInventoryDetails(productId: string): Promise<InventoryItem> {
    const response = await apiClient.get<InventoryItem>(
      `${INVENTORY_API}/${productId}/details`,
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

  /**
   * Check if a product has enough stock for the requested quantity.
   * This helper is used by checkout flow before creating an order.
   * @param productId the product ID
   * @param requiredQuantity quantity requested by customer
   * @return true if inventory is enough, otherwise false
   */
  async checkStock(
    productId: string,
    requiredQuantity: number,
  ): Promise<boolean> {
    try {
      const response = await apiClient.get<boolean>(
        `${INVENTORY_API}/${productId}/check`,
        { params: { quantity: requiredQuantity } }
      );
      return response.data;
    } catch (error) {
      // Fallback to getStock if /check endpoint is not available or fails
      const stock = await this.getStock(productId);
      return stock >= requiredQuantity;
    }
  },
};
