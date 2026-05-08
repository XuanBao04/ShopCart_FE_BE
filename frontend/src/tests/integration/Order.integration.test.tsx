import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom/vitest";
import Order from "@components/Order/Order";
import PriceBreakdown from "@components/Cart/PriceBreakdown";
import ProductCard from "@components/ProductList/ProductCard";
import { orderService } from "@services/api/orderService";
import { productService } from "@services/api/productService";
import { useCart } from "@hooks/useCart";
import { toast } from "react-toastify";
import type { OrderResponse } from "../../types/order";
import type { Product } from "../../types/product";

const mockNavigate = vi.fn();

vi.mock("@services/api/orderService", () => ({
  orderService: {
    getUserOrders: vi.fn(),
    cancelOrder: vi.fn(),
  },
}));

vi.mock("@services/api/productService", () => ({
  productService: {
    getAvailableStock: vi.fn(),
  },
}));

vi.mock("@hooks/useCart", () => ({
  useCart: vi.fn(),
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock("react-toastify", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const mockedOrderService = vi.mocked(orderService);
const mockedProductService = vi.mocked(productService);
const mockedUseCart = vi.mocked(useCart);
const mockedToast = vi.mocked(toast);

const buildOrder = (overrides: Partial<OrderResponse> = {}): OrderResponse => ({
  id: "order-1",
  userId: "user-1",
  items: [{ id: 1, productId: "P001", quantity: 2, price: 100000, name: "Product 1" }],
  status: "PENDING",
  createdAt: "2026-05-01T10:00:00.000Z",
  lastModifiedDate: "2026-05-01T10:00:00.000Z",
  subtotal: 200000,
  discountAmount: 10000,
  couponCode: "SAVE10",
  totalPrice: 219900,
  shippingFee: 29900,
  shippingAddress: "123 Test",
  city: "HCM",
  district: "D1",
  ward: "W1",
  postalCode: "700000",
  phoneNumber: "0912345678",
  ...overrides,
});

const baseProduct: Product = {
  id: "P001",
  name: "Test Product",
  description: "Test description",
  price: 100000,
  status: "ACTIVE",
};

describe("Order Integration Testing", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem("userId", "user-1");
  });

  // =========================================================
  // NHOM 1: Test rendering, data loading va cac trang thai co ban
  // =========================================================
  // Case 1.1: Hien thi loading khi dang goi API lay danh sach don hang
  it("renders loading state while fetching orders", () => {
    mockedOrderService.getUserOrders.mockImplementation(() => new Promise(() => undefined));
    render(<Order />);
    expect(screen.getByText(/Đang tải đơn hàng/i)).toBeInTheDocument();
  });

  // Case 1.2: Hien thi thong bao loi khi lay danh sach don hang that bai
  it("renders error state when fetching orders fails", async () => {
    mockedOrderService.getUserOrders.mockRejectedValue(new Error("Load failed"));
    render(<Order />);
    expect(await screen.findByText(/Load failed/i)).toBeInTheDocument();
  });

  // Case 1.3: Hien thi empty state khi user chua co don hang nao
  it("renders empty state when user has no orders", async () => {
    mockedOrderService.getUserOrders.mockResolvedValue([]);
    render(<Order />);
    expect(await screen.findByText(/Bạn chưa có đơn hàng nào/i)).toBeInTheDocument();
  });

  // Case 1.4: Hien thi danh sach don hang va trang thai don
  it("renders order list with status", async () => {
    mockedOrderService.getUserOrders.mockResolvedValue([
      buildOrder({ id: "order-abc-123", status: "SHIPPED", couponCode: undefined, discountAmount: 0 }),
    ]);
    render(<Order />);
    expect(await screen.findByText(/order-ab/i)).toBeInTheDocument();
    expect(screen.getByText("SHIPPED")).toBeInTheDocument();
  });

  // =========================================================
  // NHOM 2: Test user interactions tren Order details
  // =========================================================
  // Case 2.1: Mo chi tiet don hang va hien thi coupon + line items
  it("expands order details and shows coupon + line items", async () => {
    mockedOrderService.getUserOrders.mockResolvedValue([buildOrder()]);
    const user = userEvent.setup();
    render(<Order />);
    await user.click(await screen.findByText(/order-1/i));
    expect(screen.getByText(/Mã giảm/i)).toBeInTheDocument();
    expect(screen.getByText(/P001 - 2 x/i)).toBeInTheDocument();
  });

  // Case 2.2: Khong hien coupon badge khi don hang khong co couponCode
  it("does not render coupon badge when couponCode is missing", async () => {
    mockedOrderService.getUserOrders.mockResolvedValue([buildOrder({ couponCode: undefined, discountAmount: 0 })]);
    const user = userEvent.setup();
    render(<Order />);
    await user.click(await screen.findByText(/order-1/i));
    expect(screen.queryByText(/Mã giảm/i)).not.toBeInTheDocument();
  });

  // Case 2.3: Hien fallback text khi createdAt khong co gia tri
  it("shows fallback date text when createdAt is empty", async () => {
    mockedOrderService.getUserOrders.mockResolvedValue([buildOrder({ createdAt: "" })]);
    render(<Order />);
    expect(await screen.findByText(/Không xác định/i)).toBeInTheDocument();
  });

  // Case 2.4: Nut huy bi disable voi don khong o trang thai PENDING
  it("disables cancel button for non-pending orders", async () => {
    mockedOrderService.getUserOrders.mockResolvedValue([buildOrder({ status: "DELIVERED" })]);
    const user = userEvent.setup();
    render(<Order />);
    await user.click(await screen.findByText(/order-1/i));
    expect(screen.getByRole("button")).toBeDisabled();
  });

  // =========================================================
  // NHOM 3: Test cancel order flow (success + error)
  // =========================================================
  // Case 3.1: Huy don thanh cong -> goi API va cap nhat status tren UI
  it("cancels pending order successfully", async () => {
    mockedOrderService.getUserOrders.mockResolvedValue([buildOrder()]);
    mockedOrderService.cancelOrder.mockResolvedValue(buildOrder({ status: "CANCELLED" }));
    const user = userEvent.setup();
    render(<Order />);
    await user.click(await screen.findByText(/order-1/i));
    await user.click(screen.getByRole("button"));
    await waitFor(() => expect(mockedOrderService.cancelOrder).toHaveBeenCalledWith("order-1"));
    expect(screen.getByText("CANCELLED")).toBeInTheDocument();
  });

  // Case 3.2: Huy don that bai -> hien thi thong bao loi
  it("shows error when cancel order fails", async () => {
    mockedOrderService.getUserOrders.mockResolvedValue([buildOrder()]);
    mockedOrderService.cancelOrder.mockRejectedValue(new Error("Cancel failed"));
    const user = userEvent.setup();
    render(<Order />);
    await user.click(await screen.findByText(/order-1/i));
    await user.click(screen.getByRole("button"));
    expect(await screen.findByText(/Cancel failed/i)).toBeInTheDocument();
  });
});

describe("PriceCalculator Integration Testing (PriceBreakdown)", () => {
  // =========================================================
  // NHOM 4: Test PriceCalculator (mapping voi PriceBreakdown)
  // =========================================================
  // Case 4.1: Full mode khong co discount -> an dong giam gia
  it("renders full breakdown without discount row when discount is zero", () => {
    render(<PriceBreakdown subtotal={200000} discountAmount={0} shippingFee={29900} totalPrice={229900} />);
    expect(screen.getByText(/Chi tiết giá/i)).toBeInTheDocument();
    expect(screen.queryByText(/Giảm giá/i)).not.toBeInTheDocument();
  });

  // Case 4.2: Full mode co coupon -> hien dong giam gia va ma coupon
  it("renders discount row with coupon code in full mode", () => {
    render(
      <PriceBreakdown
        subtotal={200000}
        discountAmount={15000}
        couponCode="SAVE15"
        shippingFee={29900}
        totalPrice={214900}
      />,
    );
    expect(screen.getByText(/SAVE15/i)).toBeInTheDocument();
    expect(screen.getByText(/-15\.000/i)).toBeInTheDocument();
  });

  // Case 4.3: Compact mode -> khong hien heading full mode, van hien tong cong
  it("renders compact mode correctly", () => {
    render(
      <PriceBreakdown
        subtotal={200000}
        discountAmount={10000}
        couponCode="SAVE10"
        shippingFee={29900}
        totalPrice={219900}
        compact
      />,
    );
    expect(screen.queryByText(/Chi tiết giá/i)).not.toBeInTheDocument();
    expect(screen.getByText(/Tổng cộng/i)).toBeInTheDocument();
  });
});

describe("InventoryWarning Integration Testing (ProductCard)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedUseCart.mockReturnValue({
      cart: { userId: "user-1", items: [], totalItems: 0, totalPrice: 0 },
      isLoading: false,
      error: null,
      fetchCart: vi.fn(),
      addItem: vi.fn().mockResolvedValue(undefined),
      removeItem: vi.fn(),
      updateItem: vi.fn(),
      clear: vi.fn(),
    });
  });

  // =========================================================
  // NHOM 5: Test InventoryWarning (mapping voi ProductCard stock badge)
  // =========================================================
  // Case 5.1: San pham ACTIVE con ton kho -> hien thi Co san va cho phep add
  it("shows available stock badge when product is active and stock remains", async () => {
    mockedProductService.getAvailableStock.mockResolvedValue(5);
    render(<ProductCard product={baseProduct} />);
    expect(await screen.findByText(/Có sẵn \(5\)/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Thêm vào giỏ hàng/i })).toBeEnabled();
  });

  // Case 5.2: Het hang do stock bi tru het boi gio hang -> warning + disable action
  it("shows out-of-stock warning and disables actions when display stock is zero", async () => {
    mockedProductService.getAvailableStock.mockResolvedValue(3);
    mockedUseCart.mockReturnValue({
      cart: { userId: "user-1", items: [{ id: 1, productId: "P001", quantity: 3, price: 100000 }], totalItems: 1, totalPrice: 300000 },
      isLoading: false,
      error: null,
      fetchCart: vi.fn(),
      addItem: vi.fn().mockResolvedValue(undefined),
      removeItem: vi.fn(),
      updateItem: vi.fn(),
      clear: vi.fn(),
    });
    render(<ProductCard product={baseProduct} />);
    expect((await screen.findAllByText(/Hết hàng/i)).length).toBeGreaterThan(0);
    expect(screen.getByRole("button", { name: /Thêm vào giỏ hàng/i })).toBeDisabled();
    expect(screen.getByRole("spinbutton")).toBeDisabled();
  });

  // Case 5.3: San pham INACTIVE -> hien Het hang va khong goi API lay stock
  it("shows out-of-stock warning for inactive product and does not call stock API", () => {
    render(<ProductCard product={{ ...baseProduct, status: "INACTIVE" }} />);
    expect(screen.getAllByText(/Hết hàng/i).length).toBeGreaterThan(0);
    expect(mockedProductService.getAvailableStock).not.toHaveBeenCalled();
  });

  // Case 5.4: Chua dang nhap -> confirm va chuyen huong den login
  it("navigates to login when adding without userId", async () => {
    localStorage.removeItem("userId");
    vi.stubGlobal("confirm", vi.fn(() => true));
    mockedProductService.getAvailableStock.mockResolvedValue(5);
    render(<ProductCard product={baseProduct} />);
    await userEvent.click(await screen.findByRole("button", { name: /Thêm vào giỏ hàng/i }));
    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });

  // Case 5.5: Add to cart thanh cong -> goi addItem va hien success toast
  it("adds product successfully and shows success message", async () => {
    localStorage.setItem("userId", "user-1");
    mockedProductService.getAvailableStock.mockResolvedValue(5);
    const addItem = vi.fn().mockResolvedValue(undefined);
    mockedUseCart.mockReturnValue({
      cart: { userId: "user-1", items: [], totalItems: 0, totalPrice: 0 },
      isLoading: false,
      error: null,
      fetchCart: vi.fn(),
      addItem,
      removeItem: vi.fn(),
      updateItem: vi.fn(),
      clear: vi.fn(),
    });
    render(<ProductCard product={baseProduct} />);
    await userEvent.click(await screen.findByRole("button", { name: /Thêm vào giỏ hàng/i }));
    await waitFor(() => expect(addItem).toHaveBeenCalledWith({ productId: "P001", quantity: 1 }));
    expect(mockedToast.success).toHaveBeenCalled();
  });

  // Case 5.6: Add to cart that bai -> hien error toast tu API
  it("shows error message when add-to-cart fails", async () => {
    localStorage.setItem("userId", "user-1");
    mockedProductService.getAvailableStock.mockResolvedValue(5);
    const addItem = vi.fn().mockRejectedValue(new Error("Add failed"));
    mockedUseCart.mockReturnValue({
      cart: { userId: "user-1", items: [], totalItems: 0, totalPrice: 0 },
      isLoading: false,
      error: null,
      fetchCart: vi.fn(),
      addItem,
      removeItem: vi.fn(),
      updateItem: vi.fn(),
      clear: vi.fn(),
    });
    render(<ProductCard product={baseProduct} />);
    await userEvent.click(await screen.findByRole("button", { name: /Thêm vào giỏ hàng/i }));
    await waitFor(() => expect(mockedToast.error).toHaveBeenCalledWith("Add failed"));
  });
});
