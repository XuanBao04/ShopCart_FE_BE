import { useEffect, useState } from "react";
import { orderService } from "../../services/api/orderService";
import { OrderResponse } from "../../types/order";
import PriceBreakdown from "../Cart/PriceBreakdown";
import { formatPrice } from "../../utils/priceCalculation";

const getStatusBadgeClass = (status: OrderResponse["status"]) => {
  switch (status) {
    case "PENDING":
      return "bg-yellow-100 text-yellow-800";
    case "PROCESSING":
      return "bg-blue-100 text-blue-800";
    case "SHIPPED":
      return "bg-indigo-100 text-indigo-800";
    case "DELIVERED":
      return "bg-green-100 text-green-800";
    case "CANCELLED":
      return "bg-red-100 text-red-800";
    default:
      return "bg-gray-100 text-gray-800";
  }
};

export default function Order() {
  const userId = localStorage.getItem("userId") || "user1";
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [cancelingOrderId, setCancelingOrderId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [expandedOrderId, setExpandedOrderId] = useState<string | null>(null);

  useEffect(() => {
    const fetchOrders = async () => {
      setIsLoading(true);
      try {
        const response = await orderService.getUserOrders(userId);
        setOrders(response);
      } catch (err: Error | unknown) {
        const errorMessage = err instanceof Error ? err.message : "Không thể tải đơn hàng.";
        setError(errorMessage);
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrders();
  }, [userId]);

  const handleCancelOrder = async (orderId: string) => {
    setCancelingOrderId(orderId);
    setError(null);

    try {
      const updatedOrder = await orderService.cancelOrder(orderId);
      setOrders((prev) =>
        prev.map((order) => (order.id === orderId ? updatedOrder : order)),
      );
    } catch (err: Error | unknown) {
      const errorMessage = err instanceof Error ? err.message : "Hủy đơn hàng thất bại.";
      setError(errorMessage);
    } finally {
      setCancelingOrderId(null);
    }
  };

  if (isLoading) {
    return <div className="text-center p-8">Đang tải đơn hàng...</div>;
  }

  if (error) {
    return <div className="text-red-500 p-8">Lỗi: {error}</div>;
  }

  if (orders.length === 0) {
    return <div className="text-center p-8">Bạn chưa có đơn hàng nào.</div>;
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-8">Lịch sử đơn hàng</h1>
      <div className="space-y-4">
        {orders.map((order) => {
          const isCancelled = order.status === "CANCELLED";
          const isCancelling = cancelingOrderId === order.id;
          const isExpanded = expandedOrderId === order.id;

          return (
            <div
              key={order.id}
              className="border rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow"
            >
              {/* Order Header */}
              <div
                className="bg-gradient-to-r from-blue-50 to-blue-100 p-4 cursor-pointer hover:bg-blue-100 transition-colors"
                onClick={() =>
                  setExpandedOrderId(isExpanded ? null : order.id)
                }
              >
                <div className="flex items-center justify-between gap-4">
                  <div className="flex-1">
                    <h2 className="text-lg font-bold">
                      Đơn hàng #{order.id.substring(0, 8)}...
                    </h2>
                    <p className="text-sm text-gray-600 mt-1">
                      {order.createdAt
                        ? new Date(order.createdAt).toLocaleDateString(
                            "vi-VN",
                            {
                              year: "numeric",
                              month: "2-digit",
                              day: "2-digit",
                              hour: "2-digit",
                              minute: "2-digit",
                            }
                          )
                        : "Không xác định"}
                    </p>
                  </div>

                  <div className="text-right">
                    <p className="text-2xl font-bold text-blue-600">
                      {formatPrice(order.totalPrice)} VND
                    </p>
                    <span
                      className={`inline-block px-3 py-1 rounded-full text-xs font-semibold uppercase mt-2 ${getStatusBadgeClass(
                        order.status
                      )}`}
                    >
                      {order.status}
                    </span>
                  </div>

                  <div className="text-gray-400">
                    {isExpanded ? "▼" : "▶"}
                  </div>
                </div>
              </div>

              {/* Order Details (Expandable) */}
              {isExpanded && (
                <div className="p-4 border-t bg-white">
                  {/* Coupon Badge */}
                  {order.couponCode && (
                    <div className="mb-4 inline-block bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-semibold">
                      ✓ Mã giảm: {order.couponCode}
                    </div>
                  )}

                  {/* Items */}
                  <div className="mb-4 border-b pb-4">
                    <h3 className="font-semibold mb-2">Sản phẩm:</h3>
                    <ul className="space-y-2">
                      {order.items.map((item) => (
                        <li
                          key={item.id}
                          className="flex justify-between text-sm text-gray-700"
                        >
                          <span>
                            {item.productId} - {item.quantity} x{" "}
                            {formatPrice(item.price)} VND
                          </span>
                          <span className="font-semibold">
                            {formatPrice(item.price * item.quantity)} VND
                          </span>
                        </li>
                      ))}
                    </ul>
                  </div>

                  {/* Price Breakdown */}
                  <div className="mb-4">
                    <PriceBreakdown
                      subtotal={order.subtotal}
                      discountAmount={order.discountAmount}
                      couponCode={order.couponCode}
                      shippingFee={order.shippingFee}
                      totalPrice={order.totalPrice}
                      compact={true}
                    />
                  </div>

                  {/* Action Buttons */}
                  <div className="flex gap-2 justify-end">
                    <button
                      onClick={() => handleCancelOrder(order.id)}
                      disabled={isCancelled || isCancelling}
                      className="bg-red-500 text-white px-6 py-2 rounded hover:bg-red-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold"
                    >
                      {isCancelling
                        ? "Đang hủy..."
                        : isCancelled
                          ? "Đã hủy"
                          : "Hủy đơn hàng"}
                    </button>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
