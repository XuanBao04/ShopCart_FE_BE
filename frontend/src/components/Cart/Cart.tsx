import { useEffect } from "react";
import { useCart } from "../../hooks/useCart";
import { formatPrice } from "../../utils/priceCalculation";
import CartItem from "./CartItem";
import { orderService } from "../../services/api/orderService";
import { Navigate } from "react-router-dom";

const Cart = () => {
  const userId = localStorage.getItem("userId") || "user1";
  const { cart, isLoading, error, fetchCart, removeItem, updateItem, clear } =
    useCart(userId);

  const handleRedirectToOrders = async () => {
    try {
      if (!cart || cart.items.length === 0) {
        alert("Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng.");
        return <Navigate to="/authenticated/products" />;
      }
      // Create order
      const orderRequest = {
        userId,
        orderItems: cart.items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price,
        })),
      };
      await orderService.createOrder(orderRequest);

      // Clear cart and redirect
      await clear();
      window.location.href = "/authenticated/orders";
    } catch (error) {
      alert("Đã xảy ra lỗi khi tạo đơn hàng. Vui lòng thử lại.");
    }
  };

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center p-8">
        Đang tải giỏ hàng...
      </div>
    );
  }

  if (error) {
    return <div className="text-red-500 p-8">Lỗi: {error}</div>;
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="container mx-auto p-8 text-center">
        <h1 className="text-2xl font-bold mb-4">Giỏ hàng trống</h1>
        <a
          href="/authenticated/products"
          className="text-blue-600 hover:underline"
        >
          Tiếp tục mua sắm
        </a>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-8">Giỏ hàng</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cart Items */}
        <div className="lg:col-span-2">
          <div className="border rounded-lg p-4">
            {cart.items.map((item) => (
              <CartItem
                key={item.id}
                item={item}
                onRemove={() => removeItem(item.id)}
                onUpdateQuantity={(quantity) => updateItem(item.id, quantity)}
              />
            ))}
          </div>
        </div>

        {/* Cart Summary */}
        <div className="bg-gray-100 rounded-lg p-6 h-fit">
          <h2 className="text-xl font-bold mb-4">Tóm tắt đơn hàng</h2>
          <div className="space-y-3 mb-4">
            <div className="flex justify-between">
              <span>Tổng sản phẩm:</span>
              <span>{cart.totalItems}</span>
            </div>
            <div className="flex justify-between text-lg font-bold border-t pt-3">
              <span>Tổng cộng:</span>
              <span>{formatPrice(cart.totalPrice)}</span>
            </div>
          </div>
          <button
            className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 mb-2"
            onClick={handleRedirectToOrders}
          >
            Thanh toán
          </button>
          <button
            onClick={clear}
            className="w-full bg-red-600 text-white py-2 rounded hover:bg-red-700"
          >
            Xóa giỏ hàng
          </button>
        </div>
      </div>
    </div>
  );
};

export default Cart;
