import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom/vitest";
import Cart from "@components/Cart/Cart";
import { useCart } from "@hooks/useCart";
import { orderService } from "@services/api/orderService";
import { inventoryService } from "@services/api/inventoryService";
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

vi.mock("@services/api/inventoryService", () => ({
  inventoryService: {
    checkStock: vi.fn(),
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
const mockedInventoryService = vi.mocked(inventoryService);
const mockedProductService = vi.mocked(productService);
const mockedToast = vi.mocked(toast);

/**
 * Shared cart context for order-mock test cases.
 * We keep 2 items to verify inventory check is called for every item in cart.
 */
const baseCartContext = (): CartContextType => ({
  cart: {
    userId: "user-1",
    items: [
      { id: 11, productId: "P001", quantity: 2, price: 100000 },
      { id: 12, productId: "P002", quantity: 1, price: 150000 },
    ],
    totalItems: 2,
    totalPrice: 350000,
  },
  isLoading: false,
  error: null,
  fetchCart: mockFetchCart(),
  addItem: mockAddItem(),
  removeItem: mockRemoveItem(),
  updateItem: mockUpdateItem(),
  clear: mockClear(),
});

/**
 * Fill all required address fields so checkout can proceed to stock check.
 */
const fillRequiredAddress = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.type(screen.getByLabelText(/địa chỉ giao hàng/i), "123 Test Street");
  await user.type(screen.getByLabelText(/thành phố\/tỉnh/i), "Ho Chi Minh");
  await user.type(screen.getByLabelText(/quận\/huyện/i), "District 1");
  await user.type(screen.getByLabelText(/phường\/xã/i), "Ben Nghe");
  await user.type(screen.getByLabelText(/số điện thoại/i), "0912345678");
};

const clickCheckout = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.click(screen.getByRole("button", { name: /thanh toán/i }));
};

describe("Order - Mock Testing (createOrder + checkStock)", () => {
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
  });

  it("does not call stock check or createOrder when required address is missing", async () => {
    // Case 1:
    // Thiếu thông tin địa chỉ bắt buộc khi checkout:
    // - Không được gọi checkStock.
    // - Không được gọi createOrder.
    // - Phải hiển thị toast lỗi validate địa chỉ.
    const user = userEvent.setup();
    mockedUseCart.mockReturnValue(baseCartContext());

    render(<Cart />);
    await clickCheckout(user);

    expect(mockedInventoryService.checkStock).not.toHaveBeenCalled();
    expect(mockedOrderService.createOrder).not.toHaveBeenCalled();
    expect(mockedToast.error).toHaveBeenCalledWith(
      expect.stringMatching(/vui lòng điền đầy đủ thông tin giao hàng/i),
    );
  });

  it("calls checkStock for every cart item, then creates order and clears cart when all stock checks pass", async () => {
    // Case 2:
    // Đủ địa chỉ + tất cả sản phẩm còn hàng:
    // - checkStock phải được gọi cho từng item trong giỏ.
    // - createOrder được gọi sau khi checkStock thành công.
    // - clear cart và redirect sang trang orders.
    const user = userEvent.setup();
    const context = baseCartContext();
    mockedUseCart.mockReturnValue(context);

    // First item in stock, second item also in stock.
    mockedInventoryService.checkStock
      .mockResolvedValueOnce(true)
      .mockResolvedValueOnce(true);
    mockedOrderService.createOrder.mockResolvedValue({ id: "order-1" } as never);

    render(<Cart />);

    await fillRequiredAddress(user);
    await clickCheckout(user);

    await waitFor(() => {
      expect(mockedInventoryService.checkStock).toHaveBeenCalledTimes(2);
      expect(mockedInventoryService.checkStock).toHaveBeenNthCalledWith(1, "P001", 2);
      expect(mockedInventoryService.checkStock).toHaveBeenNthCalledWith(2, "P002", 1);
    });

    expect(mockedOrderService.createOrder).toHaveBeenCalledTimes(1);
    expect(mockedOrderService.createOrder).toHaveBeenCalledWith(
      expect.objectContaining({
        userId: "user-1",
        shippingAddress: "123 Test Street",
        city: "Ho Chi Minh",
        district: "District 1",
        ward: "Ben Nghe",
        phoneNumber: "0912345678",
      }),
    );

    // Verify flow order: stock check must happen before order creation.
    expect(mockedInventoryService.checkStock.mock.invocationCallOrder[0]).toBeLessThan(
      mockedOrderService.createOrder.mock.invocationCallOrder[0],
    );

    expect(context.clear).toHaveBeenCalledTimes(1);
    expect(window.location.href).toBe("/authenticated/orders");
  });

  it("does not create order when at least one checkStock result is false", async () => {
    // Case 3:
    // Có ít nhất 1 sản phẩm hết hàng (checkStock = false):
    // - Không được tạo đơn hàng.
    // - Không được clear cart.
    // - Phải hiển thị toast báo thiếu tồn kho.
    const user = userEvent.setup();
    const context = baseCartContext();
    mockedUseCart.mockReturnValue(context);

    // One product is out of stock.
    mockedInventoryService.checkStock
      .mockResolvedValueOnce(true)
      .mockResolvedValueOnce(false);

    render(<Cart />);

    await fillRequiredAddress(user);
    await clickCheckout(user);

    await waitFor(() => {
      expect(mockedInventoryService.checkStock).toHaveBeenCalledTimes(2);
    });

    expect(mockedOrderService.createOrder).not.toHaveBeenCalled();
    expect(context.clear).not.toHaveBeenCalled();
    expect(mockedToast.error).toHaveBeenCalledWith(
      expect.stringMatching(/không còn đủ tồn kho/i),
    );
  });

  it("handles checkStock API failure and does not call createOrder", async () => {
    // Case 4:
    // API checkStock bị lỗi:
    // - Luồng checkout phải dừng lại.
    // - Không được gọi createOrder.
    // - Không clear cart và hiển thị toast lỗi phù hợp.
    const user = userEvent.setup();
    const context = baseCartContext();
    mockedUseCart.mockReturnValue(context);

    mockedInventoryService.checkStock.mockRejectedValue(
      new Error("Inventory service failed"),
    );

    render(<Cart />);

    await fillRequiredAddress(user);
    await clickCheckout(user);

    await waitFor(() => {
      expect(mockedInventoryService.checkStock).toHaveBeenCalled();
      expect(mockedToast.error).toHaveBeenCalledWith(
        expect.stringMatching(/đã xảy ra lỗi khi tạo đơn hàng: inventory service failed/i),
      );
    });

    expect(mockedOrderService.createOrder).not.toHaveBeenCalled();
    expect(context.clear).not.toHaveBeenCalled();
  });

  it("handles createOrder failure after successful stock checks", async () => {
    // Case 5:
    // checkStock thành công nhưng createOrder thất bại:
    // - Vẫn phải verify checkStock đã chạy đầy đủ trước đó.
    // - Không clear cart.
    // - Hiển thị toast lỗi từ createOrder.
    const user = userEvent.setup();
    const context = baseCartContext();
    mockedUseCart.mockReturnValue(context);

    mockedInventoryService.checkStock
      .mockResolvedValueOnce(true)
      .mockResolvedValueOnce(true);
    mockedOrderService.createOrder.mockRejectedValue(new Error("Create order failed"));

    render(<Cart />);

    await fillRequiredAddress(user);
    await clickCheckout(user);

    await waitFor(() => {
      expect(mockedInventoryService.checkStock).toHaveBeenCalledTimes(2);
      expect(mockedOrderService.createOrder).toHaveBeenCalledTimes(1);
      expect(mockedToast.error).toHaveBeenCalledWith(
        expect.stringMatching(/đã xảy ra lỗi khi tạo đơn hàng: create order failed/i),
      );
    });

    expect(context.clear).not.toHaveBeenCalled();
  });
});
