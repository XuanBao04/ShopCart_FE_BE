import { useEffect } from 'react';
import { useCart } from '../../hooks/useCart';
import { formatPrice } from '../../utils/priceCalculation';
import CartItem from './CartItem';

const Cart = () => {
  const userId = localStorage.getItem('userId') || 'user1';
  const { cart, isLoading, error, fetchCart, removeItem, updateItem, clear } = useCart(userId);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  if (isLoading) {
    return <div className="flex justify-center items-center p-8">Đang tải giỏ hàng...</div>;
  }

  if (error) {
    return <div className="text-red-500 p-8">Lỗi: {error}</div>;
  }

  if (!cart || cart.items.length === 0) {
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
          <button className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 mb-2">
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
