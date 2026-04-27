import { useEffect, useState } from "react";
import { orderService } from "../../services/api/orderService";
import { OrderResponse } from "../../types/order";

export default function Order() {
  const userId = localStorage.getItem("userId") || "user1";
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchOrders = async () => {
      setIsLoading(true);
      try {
        const response = await orderService.getUserOrders(userId);
        console.log("Order của tôi: ", response); // Corrected function name
        setOrders(response);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrders();
  }, [userId]);

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
        {orders.map((order) => (
          <li key={order.id} className="border p-4 rounded">
            <h2 className="text-xl font-bold">Đơn hàng #{order.id}</h2>
            <p>Ngày đặt: {new Date(order.date).toLocaleDateString()}</p>
            <p>Tổng tiền: {order.totalPrice} VND</p>
            <ul className="mt-2 space-y-1">
              {order.items.map((item) => (
                <li key={item.id}>
                  {item.name} - {item.quantity} x {item.price} VND
                </li>
              ))}
            </ul>
          </li>
        ))}
      </ul>
    </div>
  );
}
