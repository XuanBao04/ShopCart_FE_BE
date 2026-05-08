import { useEffect, useState } from "react";
import { useCart } from "../../hooks/useCart";
// import { formatPrice } from "../../utils/priceCalculation";
import CartItem from "./CartItem";
import CouponInput from "./CouponInput";
import PriceBreakdown from "./PriceBreakdown";
import AddressForm from "./AddressForm";
import { orderService } from "../../services/api/orderService";
import { inventoryService } from "../../services/api/inventoryService";
import { Navigate } from "react-router-dom";
import { ShippingAddress } from "../../types/order";
import { toast } from "react-toastify";

const SHIPPING_FEE = 29900;

type OrderPreview = {
  subtotal: number;
  discountAmount: number;
  shippingFee: number;
  totalPrice: number;
  couponCode: string | null;
};

const Cart = () => {
  const userId = localStorage.getItem("userId") || "";
  const { cart, isLoading, error, fetchCart, removeItem, updateItem, clear } =
    useCart();

  const [couponCode, setCouponCode] = useState<string | null>(null);
  const [discountAmount, setDiscountAmount] = useState(0);
  const [orderPreview, setOrderPreview] = useState<OrderPreview | null>(
    null
  );
  const [shippingAddress, setShippingAddress] = useState<ShippingAddress>({
    shippingAddress: "",
    city: "",
    district: "",
    ward: "",
    postalCode: "",
    phoneNumber: "",
  });

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
        toast.error("Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng.");
        return <Navigate to="/authenticated/products" />;
      }

      // Validate address fields
      if (
        !shippingAddress.shippingAddress ||
        !shippingAddress.city ||
        !shippingAddress.district ||
        !shippingAddress.ward ||
        !shippingAddress.phoneNumber
      ) {
        toast.error("Vui lòng điền đầy đủ thông tin giao hàng.");
        return;
      }

      // Create order with coupon and address if applied
      const orderRequest = {
        userId,
        orderItems: cart.items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price,
        })),
        couponCode: couponCode ?? undefined,
        ...shippingAddress,
      };

      // Validate stock for every cart item before creating order.
      const stockChecks = await Promise.all(
        cart.items.map((item) =>
          inventoryService.checkStock(item.productId, item.quantity),
        ),
      );

      if (stockChecks.some((isAvailable) => !isAvailable)) {
        toast.error(
          "Một hoặc nhiều sản phẩm không còn đủ tồn kho. Vui lòng cập nhật giỏ hàng.",
        );
        return;
      }

      await orderService.createOrder(orderRequest);

      // Clear cart and redirect
      await clear();
      window.location.href = "/authenticated/orders";
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : "Không xác định được lỗi.";
      toast.error(`Đã xảy ra lỗi khi tạo đơn hàng: ${message}`);
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
        {/* Left Column: Cart Items and Address */}
        <div className="lg:col-span-2 space-y-4">
          {/* Cart Items */}
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

          {/* Address Form */}
          <AddressForm onAddressChange={setShippingAddress} />
        </div>

        {/* Right Column: Cart Summary */}
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
              couponCode={orderPreview.couponCode ?? undefined}
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
