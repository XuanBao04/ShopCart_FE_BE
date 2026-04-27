import { useEffect, useState } from "react";
import { orderService } from "../../services/api/orderService";
import { OrderResponse } from "../../types/order";

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

  useEffect(() => {
    const fetchOrders = async () => {
      setIsLoading(true);
      try {
        const response = await orderService.getUserOrders(userId);
        setOrders(response);
      } catch (err: any) {
        setError(err.message || "Không thể tải đơn hàng.");
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
    } catch (err: any) {
      setError(err.message || "Hủy đơn hàng thất bại.");
    } finally {
      setCancelingOrderId(null);
    }
  };

  if (isLoading) {
    return <div className="text-center">Đang tải đơn hàng...</div>;
  }

  if (error) {
    return <div className="text-red-500">Lỗi: {error}</div>;
  }

  if (orders.length === 0) {
    return <div className="text-center">Bạn chưa có đơn hàng nào.</div>;
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-8">Lịch sử đơn hàng</h1>
      <ul className="space-y-4">
        {orders.map((order) => {
          const isCancelled = order.status === "CANCELLED";
          const isCancelling = cancelingOrderId === order.id;

          return (
            <li
              key={order.id}
              className="border p-4 rounded flex items-center justify-between gap-4"
            >
              <div>
                <h2 className="text-xl font-bold">Đơn hàng #{order.id}</h2>
                <p>
                  Ngày đặt:{" "}
                  {order.date
                    ? new Date(order.date).toLocaleDateString()
                    : "Không xác định"}
                </p>
                <p>Tổng tiền: {order.totalPrice} VND</p>
                <ul className="mt-2 space-y-1">
                  {order.items.map((item) => (
                    <li key={item.id}>
                      {item.name} - {item.quantity} x {item.price} VND
                    </li>
                  ))}
                </ul>
              </div>

              <div className="flex items-center gap-3 shrink-0">
                <p className="flex items-center gap-2">
                  <span className="font-medium">Trạng thái:</span>
                  <span
                    className={`px-2.5 py-1 rounded-full text-xs font-semibold uppercase ${getStatusBadgeClass(order.status)}`}
                  >
                    {order.status}
                  </span>
                </p>

                <button
                  onClick={() => handleCancelOrder(order.id)}
                  disabled={isCancelled || isCancelling}
                  className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  {isCancelling
                    ? "Đang hủy..."
                    : isCancelled
                      ? "Đã hủy"
                      : "Hủy đơn hàng"}
                </button>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
