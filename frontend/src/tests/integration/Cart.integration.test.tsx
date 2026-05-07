import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom/vitest";
import Cart from "@components/Cart/Cart";
import { useCart } from "@hooks/useCart";
import { orderService } from "@services/api/orderService";
import { couponService } from "@services/api/couponService";
import { productService } from "@services/api/productService";
import { toast } from "react-toastify";
import type { CartContextType } from "../../context/CartContext";
import type { CartItemRequest } from "../../types/cart";

vi.mock("@hooks/useCart", () => ({
  useCart: vi.fn(),
}));

vi.mock("@services/api/orderService", () => ({
  orderService: {
    createOrder: vi.fn(),
  },
}));

vi.mock("@services/api/couponService", () => ({
  couponService: {
    validateCoupon: vi.fn(),
    calculateDiscount: vi.fn(),
  },
}));

vi.mock("@services/api/productService", () => ({
  productService: {
    getProductById: vi.fn(),
    getAvailableStock: vi.fn(),
  },
}));

vi.mock("react-toastify", () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockFetchCart = () => vi.fn<[], Promise<void>>().mockResolvedValue(undefined);
const mockAddItem = () =>
  vi.fn<[CartItemRequest], Promise<void>>().mockResolvedValue(undefined);
const mockRemoveItem = () => vi.fn<[number], Promise<void>>().mockResolvedValue(undefined);
const mockUpdateItem = () =>
  vi.fn<[number, number], Promise<void>>().mockResolvedValue(undefined);
const mockClear = () => vi.fn<[], Promise<void>>().mockResolvedValue(undefined);

const mockedUseCart = vi.mocked(useCart);
const mockedOrderService = vi.mocked(orderService);
const mockedCouponService = vi.mocked(couponService);
const mockedProductService = vi.mocked(productService);
const mockedToast = vi.mocked(toast);

const baseCartContext = (): CartContextType => ({
  cart: {
    userId: "user-1",
    items: [{ id: 11, productId: "P001", quantity: 2, price: 100000 }],
    totalItems: 1,
    totalPrice: 200000,
  },
  isLoading: false,
  error: null,
  fetchCart: mockFetchCart(),
  addItem: mockAddItem(),
  removeItem: mockRemoveItem(),
  updateItem: mockUpdateItem(),
  clear: mockClear(),
});

const fillShippingAddress = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.type(screen.getByLabelText(/địa chỉ giao hàng/i), "123 Test Street");
  await user.type(screen.getByLabelText(/thành phố\/tỉnh/i), "Ho Chi Minh");
  await user.type(screen.getByLabelText(/quận\/huyện/i), "District 1");
  await user.type(screen.getByLabelText(/phường\/xã/i), "Ben Nghe");
  await user.type(screen.getByLabelText(/mã bưu điện/i), "700000");
  await user.type(screen.getByLabelText(/số điện thoại/i), "0912345678");
};

const fillCoupon = async (user: ReturnType<typeof userEvent.setup>, coupon: string) => {
  await user.type(screen.getByPlaceholderText(/nhập mã giảm giá/i), coupon);
  await user.click(screen.getByRole("button", { name: /áp dụng/i }));
};

const clickCheckout = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.click(screen.getByRole("button", { name: /thanh toán/i }));
};

describe("Cart Integration Testing", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem("userId", "user-1");

    Object.defineProperty(window, "location", {
      writable: true,
      value: { href: "http://localhost/" },
    });

    mockedProductService.getProductById.mockResolvedValue({
      id: "P001",
      name: "Test Product",
      price: 100000,
      description: "desc",
      status: "ACTIVE",
    });
    mockedProductService.getAvailableStock.mockResolvedValue(10);

    mockedCouponService.validateCoupon.mockResolvedValue(false);
    mockedCouponService.calculateDiscount.mockResolvedValue(0);
  });

  // =========================================================
  // NHOM 1: Test rendering va user interactions
  // =========================================================
  // Case 1.1: Hien thi trang thai loading khi gio hang dang duoc tai
  it("renders loading state", () => {
    mockedUseCart.mockReturnValue({ ...baseCartContext(), isLoading: true });
    render(<Cart />);
    expect(screen.getByText(/đang tải giỏ hàng/i)).toBeInTheDocument();
  });

  // Case 1.2: Hien thi thong bao loi khi useCart tra ve error
  it("renders error state", () => {
    mockedUseCart.mockReturnValue({ ...baseCartContext(), error: "Fetch failed" });
    render(<Cart />);
    expect(screen.getByText(/fetch failed/i)).toBeInTheDocument();
  });

  // Case 1.3: Hien thi giao dien gio hang trong va link tiep tuc mua sam
  it("renders empty cart state", () => {
    mockedUseCart.mockReturnValue({
      ...baseCartContext(),
      cart: { userId: "user-1", items: [], totalItems: 0, totalPrice: 0 },
    });

    render(<Cart />);
    expect(screen.getByRole("link", { name: /tiếp tục mua sắm/i })).toHaveAttribute(
      "href",
      "/authenticated/products"
    );
  });

  // Case 1.4: Render du lieu cart item va goi fetchCart khi component mount
  it("renders cart details and calls fetchCart on mount", async () => {
    const context = baseCartContext();
    mockedUseCart.mockReturnValue(context);

    render(<Cart />);

    await waitFor(() => expect(context.fetchCart).toHaveBeenCalledTimes(1));
    expect(screen.getByText(/test product/i)).toBeInTheDocument();
    expect(screen.getAllByText(/200\.000/).length).toBeGreaterThan(0);
  });

  // Case 1.5: Tuong tac tang so luong va xoa item trong gio hang
  it("handles quantity update and remove item interactions", async () => {
    const user = userEvent.setup();
    const context = baseCartContext();
    mockedUseCart.mockReturnValue(context);

    render(<Cart />);

    await user.click(screen.getByRole("button", { name: "+" }));
    expect(context.updateItem).toHaveBeenCalledWith(11, 3);

    await user.click(screen.getAllByRole("button", { name: /^xóa$/i })[0]);
    expect(context.removeItem).toHaveBeenCalledWith(11);
  });

  // =========================================================
  // NHOM 3: Test error handling va success messages (phan coupon)
  // =========================================================
  // Case 3.1: Ap dung coupon thanh cong -> hien success message
  it("applies coupon successfully and shows success message", async () => {
    const user = userEvent.setup();
    mockedUseCart.mockReturnValue(baseCartContext());
    mockedCouponService.validateCoupon.mockResolvedValue(true);
    mockedCouponService.calculateDiscount.mockResolvedValue(20000);

    render(<Cart />);
    await fillCoupon(user, "save20");

    await waitFor(() => {
      expect(mockedCouponService.validateCoupon).toHaveBeenCalledWith("SAVE20");
      expect(mockedCouponService.calculateDiscount).toHaveBeenCalledWith("SAVE20", 200000);
    });

    expect(screen.getByText(/mã giảm giá đã được áp dụng thành công/i)).toBeInTheDocument();
  });

  // Case 3.2: Coupon khong hop le/het han -> hien error message
  it("shows coupon invalid error", async () => {
    const user = userEvent.setup();
    mockedUseCart.mockReturnValue(baseCartContext());
    mockedCouponService.validateCoupon.mockResolvedValue(false);

    render(<Cart />);
    await fillCoupon(user, "invalid");

    expect(
      await screen.findByText(/mã giảm giá không hợp lệ hoặc đã hết hạn/i)
    ).toBeInTheDocument();
  });

  // Case 3.3: Coupon hop le nhung khong du dieu kien don hang -> hien error
  it("shows coupon minimum amount error when discount is zero", async () => {
    const user = userEvent.setup();
    mockedUseCart.mockReturnValue(baseCartContext());
    mockedCouponService.validateCoupon.mockResolvedValue(true);
    mockedCouponService.calculateDiscount.mockResolvedValue(0);

    render(<Cart />);
    await fillCoupon(user, "save0");

    expect(
      await screen.findByText(/đơn hàng không đủ điều kiện để áp dụng mã giảm giá này/i)
    ).toBeInTheDocument();
  });

  // Case 3.4: Loi API khi validate coupon -> hien noi dung loi tu API
  it("shows coupon API error message", async () => {
    const user = userEvent.setup();
    mockedUseCart.mockReturnValue(baseCartContext());
    mockedCouponService.validateCoupon.mockRejectedValue(new Error("Coupon API failed"));

    render(<Cart />);
    await fillCoupon(user, "saveerr");

    expect(await screen.findByText(/coupon api failed/i)).toBeInTheDocument();
  });

  // =========================================================
  // NHOM 2: Test form submission va API calls
  // NHOM 3: Error handling/success messages (phan checkout)
  // =========================================================
  // Case 2.1 + 3.5: Submit khi thieu dia chi bat buoc -> khong goi API, hien toast loi
  it("shows validation error when checkout without required address", async () => {
    const user = userEvent.setup();
    mockedUseCart.mockReturnValue(baseCartContext());

    render(<Cart />);
    await clickCheckout(user);

    expect(mockedOrderService.createOrder).not.toHaveBeenCalled();
    expect(mockedToast.error).toHaveBeenCalledWith(
      expect.stringMatching(/vui lòng điền đầy đủ thông tin giao hàng/i)
    );
  });

  // Case 2.2 + 3.6: Submit thanh cong (co coupon + dia chi) -> goi createOrder, clear cart, redirect
  it("submits order successfully with address and coupon, then clears cart and redirects", async () => {
    const user = userEvent.setup();
    const context = baseCartContext();
    mockedUseCart.mockReturnValue(context);
    mockedCouponService.validateCoupon.mockResolvedValue(true);
    mockedCouponService.calculateDiscount.mockResolvedValue(5000);
    mockedOrderService.createOrder.mockResolvedValue({ id: "order-1" } as never);

    render(<Cart />);

    await fillCoupon(user, "save5");
    await fillShippingAddress(user);
    await clickCheckout(user);

    await waitFor(() => expect(mockedOrderService.createOrder).toHaveBeenCalledTimes(1));

    expect(mockedOrderService.createOrder).toHaveBeenCalledWith(
      expect.objectContaining({
        userId: "user-1",
        couponCode: "SAVE5",
        shippingAddress: "123 Test Street",
        city: "Ho Chi Minh",
        district: "District 1",
        ward: "Ben Nghe",
        postalCode: "700000",
        phoneNumber: "0912345678",
      })
    );
    expect(context.clear).toHaveBeenCalledTimes(1);
    expect(window.location.href).toBe("/authenticated/orders");
  });

  // Case 2.3 + 3.7: API createOrder that bai -> hien toast loi, khong clear cart
  it("handles checkout API error and shows toast", async () => {
    const user = userEvent.setup();
    const context = baseCartContext();
    mockedUseCart.mockReturnValue(context);
    mockedOrderService.createOrder.mockRejectedValue(new Error("Create order failed"));

    render(<Cart />);
    await fillShippingAddress(user);
    await clickCheckout(user);

    await waitFor(() => {
      expect(mockedToast.error).toHaveBeenCalledWith(
        expect.stringMatching(/đã xảy ra lỗi khi tạo đơn hàng: create order failed/i)
      );
    });

    expect(context.clear).not.toHaveBeenCalled();
  });

  // Case 1.6: Tuong tac nut xoa toan bo gio hang -> goi clear
  it("clears cart when clicking clear cart button", async () => {
    const user = userEvent.setup();
    const context = baseCartContext();
    mockedUseCart.mockReturnValue(context);

    render(<Cart />);
    await user.click(screen.getByRole("button", { name: /xóa giỏ hàng/i }));

    expect(context.clear).toHaveBeenCalledTimes(1);
  });
});
