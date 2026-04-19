import apiClient from './apiClient';
import { Product, ProductResponse } from '../../types/product';

const PRODUCT_API = '/products';

export const productService = {
  /**
   * Get all products
   */
  async getAllProducts(): Promise<Product[]> {
    const response = await apiClient.get<Product[]>(PRODUCT_API);
    return response.data;
  },

  /**
   * Get product by ID
   */
  async getProductById(productId: string): Promise<Product> {
    const response = await apiClient.get<Product>(`${PRODUCT_API}/${productId}`);
    return response.data;
  },

  /**
   * Search products by keyword
   */
  async searchProducts(keyword: string): Promise<Product[]> {
    const response = await apiClient.get<Product[]>(`${PRODUCT_API}/search`, {
      params: { keyword },
    });
    return response.data;
  },

  /**
   * Get available stock for product
   */
  async getAvailableStock(productId: string): Promise<number> {
    const response = await apiClient.get<number>(`${PRODUCT_API}/${productId}/stock`);
    return response.data;
  },

  /**
   * Check if product is available
   */
  async isProductAvailable(productId: string): Promise<boolean> {
    const response = await apiClient.get<boolean>(`${PRODUCT_API}/${productId}/availability`);
    return response.data;
  },
};
