import { useEffect, useState } from "react";
import { orderService } from "../../services/api/orderService";
import { inventoryService } from "../../services/api/inventoryService";
import { OrderResponse } from "../../types/order";
import { formatPrice } from "../../utils/priceCalculation";
import { FaSync, FaChevronDown, FaCheck, FaTimes } from "react-icons/fa";
import { toast } from "react-toastify";

const ORDER_STATUSES = [
  { value: "PENDING", label: "Chờ xác nhận", color: "bg-yellow-100 text-yellow-800" },
  { value: "PROCESSING", label: "Đang xử lý", color: "bg-blue-100 text-blue-800" },
  { value: "SHIPPED", label: "Đã gửi", color: "bg-purple-100 text-purple-800" },
  { value: "DELIVERED", label: "Đã giao", color: "bg-green-100 text-green-800" },
  { value: "CANCELLED", label: "Đã hủy", color: "bg-red-100 text-red-800" },
];

const OrderManagement = () => {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>("PENDING");
  const [expandedOrderId, setExpandedOrderId] = useState<string | null>(null);
  const [updatingOrderId, setUpdatingOrderId] = useState<string | null>(null);

  const fetchOrders = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const allOrders = await orderService.getAllOrders();
      setOrders(allOrders);
    } catch (err) {
      setError((err as Error).message || "Không thể tải danh sách đơn hàng");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleUpdateStatus = async (orderId: string, newStatus: string) => {
    setUpdatingOrderId(orderId);
    try {
      const updatedOrder = await orderService.updateOrderStatus(orderId, newStatus);
      
      // Update local state with new order
      setOrders(orders.map(o => o.id === orderId ? updatedOrder : o));
      
      // Refresh inventory data for all products in the order after successful status update
      // This ensures UI shows the latest soldQuantity and availableStock
      if (newStatus === "SHIPPED" && updatedOrder.items) {
        try {
          // Refresh inventory for all products in this order
          await Promise.all(
            updatedOrder.items.map(item =>
              inventoryService.getInventoryDetails(item.productId)
            )
          );
          // Inventory data is now fresh; component consuming this data will show updated values
        } catch (err) {
          console.warn("Failed to refresh inventory data:", err);
          // Don't fail the operation if inventory refresh fails - it's secondary
        }
      }
      
      // Show success message
      toast.success("Cập nhật trạng thái đơn hàng thành công!");
    } catch (err) {
      toast.error("Lỗi cập nhật trạng thái: " + (err as Error).message);
    } finally {
      setUpdatingOrderId(null);
    }
  };

  // Filter orders
  let filteredOrders = orders;
  if (statusFilter !== "ALL") {
    filteredOrders = filteredOrders.filter(o => o.status === statusFilter);
  }

  const getStatusInfo = (status: string) => {
    return ORDER_STATUSES.find(s => s.value === status) || ORDER_STATUSES[0];
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("vi-VN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getNextStatuses = (currentStatus: string): string[] => {
    const statusFlow: { [key: string]: string[] } = {
      PENDING: ["PROCESSING", "CANCELLED"],
      PROCESSING: ["SHIPPED", "CANCELLED"],
      SHIPPED: ["DELIVERED"],
      DELIVERED: [],
      CANCELLED: [],
    };
    return statusFlow[currentStatus] || [];
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h2 className="text-2xl font-bold mb-4">Quản lý Đơn hàng</h2>

        {error && (
          <div className="bg-red-100 text-red-700 p-4 rounded mb-4">
            {error}
          </div>
        )}

        {/* Controls */}
        <div className="flex flex-col md:flex-row gap-4 mb-6">
          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 border rounded"
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="PENDING">Chờ xác nhận</option>
            <option value="PROCESSING">Đang xử lý</option>
            <option value="SHIPPED">Đã gửi</option>
            <option value="DELIVERED">Đã giao</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>

          {/* Refresh Button */}
          <button
            onClick={fetchOrders}
            disabled={isLoading}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400"
          >
            <FaSync className={isLoading ? "animate-spin" : ""} />
            {isLoading ? "Đang tải..." : "Làm mới"}
          </button>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
          <div className="bg-blue-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Tất cả</p>
            <p className="text-2xl font-bold text-blue-600">{orders.length}</p>
          </div>
          <div className="bg-yellow-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Chờ xác nhận</p>
            <p className="text-2xl font-bold text-yellow-600">
              {orders.filter(o => o.status === "PENDING").length}
            </p>
          </div>
          <div className="bg-purple-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Đang xử lý</p>
            <p className="text-2xl font-bold text-purple-600">
              {orders.filter(o => o.status === "PROCESSING").length}
            </p>
          </div>
          <div className="bg-green-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Đã giao</p>
            <p className="text-2xl font-bold text-green-600">
              {orders.filter(o => o.status === "DELIVERED").length}
            </p>
          </div>
          <div className="bg-red-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Đã hủy</p>
            <p className="text-2xl font-bold text-red-600">
              {orders.filter(o => o.status === "CANCELLED").length}
            </p>
          </div>
        </div>

        {/* Orders List */}
        {isLoading ? (
          <div className="text-center py-8">
            <p className="text-gray-600">Đang tải dữ liệu...</p>
          </div>
        ) : filteredOrders.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-600">Không có đơn hàng nào</p>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredOrders.map((order) => (
              <div key={order.id} className="border rounded-lg overflow-hidden shadow-sm hover:shadow-md transition">
                {/* Order Header */}
                <div
                  className="bg-gray-50 p-4 cursor-pointer hover:bg-gray-100 transition flex items-center justify-between"
                  onClick={() =>
                    setExpandedOrderId(
                      expandedOrderId === order.id ? null : order.id
                    )
                  }
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-4">
                      <div>
                        <p className="font-semibold text-gray-900">
                          Đơn #{order.id.substring(0, 8).toUpperCase()}
                        </p>
                        <p className="text-sm text-gray-600">
                          Khách: {order.userId}
                        </p>
                      </div>
                      <div className="ml-auto text-right">
                        <p className="font-bold text-lg">
                          {formatPrice(order.totalPrice)}
                        </p>
                        <p className="text-sm text-gray-600">
                          {formatDate(order.createdAt)}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Status Badge */}
                  <div className="ml-4">
                    <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${
                      getStatusInfo(order.status).color
                    }`}>
                      {getStatusInfo(order.status).label}
                    </span>
                  </div>

                  {/* Expand Icon */}
                  <FaChevronDown
                    className={`ml-4 transition-transform ${
                      expandedOrderId === order.id ? "rotate-180" : ""
                    }`}
                  />
                </div>

                {/* Order Details */}
                {expandedOrderId === order.id && (
                  <div className="p-4 border-t bg-white">
                    {/* Items */}
                    <div className="mb-6">
                      <h4 className="font-semibold mb-3">Chi tiết sản phẩm:</h4>
                      <table className="w-full text-sm">
                        <thead className="border-b">
                          <tr>
                            <th className="text-left py-2">Sản phẩm</th>
                            <th className="text-right py-2">Số lượng</th>
                            <th className="text-right py-2">Giá</th>
                            <th className="text-right py-2">Thành tiền</th>
                          </tr>
                        </thead>
                        <tbody>
                          {order.items.map((item) => (
                            <tr key={item.id} className="border-b">
                              <td className="py-2">{item.name}</td>
                              <td className="text-right">{item.quantity}</td>
                              <td className="text-right">{formatPrice(item.price)}</td>
                              <td className="text-right font-medium">
                                {formatPrice(item.price * item.quantity)}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>

                    {/* Summary */}
                    <div className="bg-gray-50 p-4 rounded mb-6">
                      <div className="flex justify-between mb-2">
                        <span>Tạm tính:</span>
                        <span>{formatPrice(order.totalPrice - order.shippingFee)}</span>
                      </div>
                      <div className="flex justify-between mb-2">
                        <span>Phí vận chuyển:</span>
                        <span>{formatPrice(order.shippingFee)}</span>
                      </div>
                      <div className="flex justify-between text-lg font-bold border-t pt-2">
                        <span>Tổng cộng:</span>
                        <span>{formatPrice(order.totalPrice)}</span>
                      </div>
                    </div>

                    {/* Status Update */}
                    <div className="flex gap-2">
                      {getNextStatuses(order.status).length > 0 ? (
                        <>
                          {getNextStatuses(order.status).map((nextStatus) => (
                            <button
                              key={nextStatus}
                              onClick={() => handleUpdateStatus(order.id, nextStatus)}
                              disabled={updatingOrderId === order.id}
                              className={`flex items-center gap-2 px-4 py-2 rounded font-medium transition ${
                                getStatusInfo(nextStatus).color
                              } hover:opacity-80 disabled:opacity-50`}
                            >
                              <FaCheck size={14} />
                              {getStatusInfo(nextStatus).label}
                            </button>
                          ))}

                          {/* Cancel Order Button */}
                          {order.status !== "CANCELLED" && (
                            <button
                              onClick={() => handleUpdateStatus(order.id, "CANCELLED")}
                              disabled={updatingOrderId === order.id}
                              className="flex items-center gap-2 px-4 py-2 bg-red-100 text-red-800 rounded font-medium hover:opacity-80 disabled:opacity-50 transition"
                            >
                              <FaTimes size={14} />
                              Hủy đơn
                            </button>
                          )}
                        </>
                      ) : (
                        <div className="text-gray-600 text-sm">
                          {order.status === "DELIVERED"
                            ? "✅ Đơn hàng đã giao thành công"
                            : "Không thể cập nhật trạng thái"}
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default OrderManagement;
