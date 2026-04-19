import { useState } from 'react';
import { orderService } from '../../services/api/orderService';
import { cartService } from '../../services/api/cartService';
import { useCart } from '../../hooks/useCart';
import { OrderRequest } from '../../types/order';
import { formatPrice } from '../../utils/priceCalculation';

const Checkout = () => {
  const userId = localStorage.getItem('userId') || 'user1';
  const { cart, fetchCart } = useCart(userId);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleCheckout = async () => {
    if (!cart || cart.items.length === 0) {
      setError('Giỏ hàng trống');
      return;
    }

    setIsSubmitting(true);
    try {
      const orderRequest: OrderRequest = {
        userId,
        orderItems: cart.items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: 0, // Will be set by backend
        })),
      };

      const response = await orderService.createOrder(orderRequest);
      
      // Clear cart after successful order
      await cartService.clearCart(userId);
      
      setSuccess(true);
      setError(null);
      
      // Redirect to order confirmation
      setTimeout(() => {
        window.location.href = `/order/${response.id}`;
      }, 2000);
    } catch (err) {
      setError((err as Error).message);
      setSuccess(false);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!cart) {
    return <div className="p-8">Đang tải...</div>;
  }

  if (cart.items.length === 0) {
    return (
      <div className="container mx-auto p-8 text-center">
        <h1 className="text-2xl font-bold mb-4">Giỏ hàng trống</h1>
        <a href="/" className="text-blue-600 hover:underline">
          Tiếp tục mua sắm
        </a>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-8">Thanh toán</h1>

      {error && <div className="bg-red-100 text-red-700 p-4 rounded mb-4">{error}</div>}
      {success && (
        <div className="bg-green-100 text-green-700 p-4 rounded mb-4">
          Đặt hàng thành công! Đang chuyển hướng...
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Order Summary */}
        <div className="border rounded-lg p-6">
          <h2 className="text-xl font-bold mb-4">Thông tin đơn hàng</h2>
          <div className="space-y-3 mb-6">
            {cart.items.map((item) => (
              <div key={item.id} className="flex justify-between pb-3 border-b">
                <span>{item.productId} x {item.quantity}</span>
              </div>
            ))}
          </div>
          <div className="border-t pt-4">
            <div className="flex justify-between text-lg font-bold">
              <span>Tổng cộng:</span>
              <span>{formatPrice(cart.totalPrice)}</span>
            </div>
          </div>
        </div>

        {/* Checkout Form */}
        <div className="border rounded-lg p-6">
          <h2 className="text-xl font-bold mb-4">Thông tin giao hàng</h2>
          <form className="space-y-4">
            <div>
              <label className="block text-sm font-bold mb-1">Tên người nhận</label>
              <input type="text" className="w-full border rounded px-3 py-2" />
            </div>
            <div>
              <label className="block text-sm font-bold mb-1">Số điện thoại</label>
              <input type="tel" className="w-full border rounded px-3 py-2" />
            </div>
            <div>
              <label className="block text-sm font-bold mb-1">Địa chỉ</label>
              <textarea className="w-full border rounded px-3 py-2 h-20" />
            </div>
            <button
              type="button"
              onClick={handleCheckout}
              disabled={isSubmitting}
              className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:bg-gray-400"
            >
              {isSubmitting ? 'Đang xử lý...' : 'Xác nhận đặt hàng'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
