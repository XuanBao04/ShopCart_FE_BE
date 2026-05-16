import apiClient from "./apiClient";
import { OrderRequest, OrderResponse, OrderPreviewResponse } from "../../types/order";

const ORDER_API = "/api/orders";

export const orderService = {
  /**
   * Create new order
   */
  async createOrder(request: OrderRequest): Promise<OrderResponse> {
    const response = await apiClient.post<OrderResponse>(
      `${ORDER_API}/${request.userId}`,
      request,
    );
    return response.data;
  },

  /**
   * Preview order before checkout
   */
  async previewOrder(request: OrderRequest): Promise<OrderPreviewResponse> {
    const response = await apiClient.post<OrderPreviewResponse>(
      `${ORDER_API}/preview`,
      request,
    );
    return response.data;
  },

  /**
   * Get order by ID
   */
  async getOrderById(orderId: string): Promise<OrderResponse> {
    const response = await apiClient.get<OrderResponse>(
      `${ORDER_API}/${orderId}`,
    );
    return response.data;
  },

  /**
   * Get all orders for user
   */
  async getUserOrders(userId: string): Promise<OrderResponse[]> {
    const response = await apiClient.get<OrderResponse[]>(
      `${ORDER_API}/user/${userId}`,
    );
    return response.data;
  },

  /**
   * Cancel order
   */
  async cancelOrder(orderId: string): Promise<OrderResponse> {
    const response = await apiClient.delete<OrderResponse>(
      `${ORDER_API}/${orderId}`,
    );
    return response.data;
  },

  /**
   * Update order status
   */
  async updateOrderStatus(
    orderId: string,
    status: string,
  ): Promise<OrderResponse> {
    const response = await apiClient.patch<OrderResponse>(
      `${ORDER_API}/${orderId}/${status}`,
    );
    return response.data;
  },

  /**
   * Get all orders (admin only)
   */
  async getAllOrders(): Promise<OrderResponse[]> {
    const response = await apiClient.get<OrderResponse[]>(
      `${ORDER_API}/all`,
    );
    return response.data;
  },
};

