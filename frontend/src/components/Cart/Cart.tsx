import { useEffect, useState } from "react";
import { useCart } from "../../hooks/useCart";
import { formatPrice } from "../../utils/priceCalculation";
import CartItem from "./CartItem";
import CouponInput from "./CouponInput";
import PriceBreakdown from "./PriceBreakdown";
import { orderService } from "../../services/api/orderService";
import { Navigate } from "react-router-dom";

const SHIPPING_FEE = 29900;

const Cart = () => {
  const userId = localStorage.getItem("userId") || "user1";
  const { cart, isLoading, error, fetchCart, removeItem, updateItem, clear } =
    useCart(userId);

  const [couponCode, setCouponCode] = useState<string | null>(null);
  const [discountAmount, setDiscountAmount] = useState(0);
  const [orderPreview, setOrderPreview] = useState<any>(null);

  // Recalculate when cart or coupon changes
  useEffect(() => {
    if (cart && cart.items.length > 0) {
      const subtotal = cart.items.reduce(
        (sum, item) => sum + item.price * item.quantity,
        0
      );

      setOrderPreview({
        subtotal,
        discountAmount,
        shippingFee: SHIPPING_FEE,
        totalPrice: subtotal - discountAmount + SHIPPING_FEE,
        couponCode: couponCode,
      });
    }
  }, [cart, discountAmount, couponCode]);

  const handleRedirectToOrders = async () => {
    try {
      if (!cart || cart.items.length === 0) {
        alert("Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng.");
        return <Navigate to="/authenticated/products" />;
      }

      // Create order with coupon if applied
      const orderRequest = {
        userId,
        orderItems: cart.items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price,
        })),
        couponCode: couponCode || undefined,
      };

      await orderService.createOrder(orderRequest);

      // Clear cart and redirect
      await clear();
      window.location.href = "/authenticated/orders";
    } catch (err: any) {
      alert(`Đã xảy ra lỗi khi tạo đơn hàng: ${err.message}`);
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
        <div className="space-y-4">
          {/* Coupon Input */}
          <CouponInput
            onCouponApply={setCouponCode}
            onDiscountChange={setDiscountAmount}
            orderAmount={
              cart?.items.reduce(
                (sum, item) => sum + item.price * item.quantity,
                0
              ) || 0
            }
          />

          {/* Price Breakdown */}
          {orderPreview && (
            <PriceBreakdown
              subtotal={orderPreview.subtotal}
              discountAmount={orderPreview.discountAmount}
              couponCode={orderPreview.couponCode}
              shippingFee={orderPreview.shippingFee}
              totalPrice={orderPreview.totalPrice}
            />
          )}

          {/* Action Buttons */}
          <div className="space-y-2">
            <button
              className="w-full bg-blue-600 text-white py-3 rounded hover:bg-blue-700 font-semibold transition-colors"
              onClick={handleRedirectToOrders}
            >
              Thanh toán
            </button>
            <button
              onClick={clear}
              className="w-full bg-red-600 text-white py-3 rounded hover:bg-red-700 font-semibold transition-colors"
            >
              Xóa giỏ hàng
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;

