import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import "@testing-library/jest-dom/vitest";
import { CartProvider } from "../../context/CartContext";
import { useCart } from "@hooks/useCart";
import { cartService } from "@services/api/cartService";

vi.mock("@services/api/cartService", () => ({
  cartService: {
    getCart: vi.fn(),
    addToCart: vi.fn(),
    removeFromCart: vi.fn(),
    updateCartItem: vi.fn(),
    clearCart: vi.fn(),
  },
}));

const mockedCartService = vi.mocked(cartService);

const DEFAULT_ITEM = {
  productId: "P001",
  quantity: 2,
};

const MOCK_CART_RESPONSE = {
  userId: "user-1",
  items: [{ id: 11, productId: "P001", quantity: 2, price: 100000 }],
  totalItems: 1,
  totalPrice: 200000,
};

/**
 * Component test harness:
 * - Dùng `useCart()` thật từ `CartProvider`.
 * - Cung cấp button để trigger `addItem`.
 * - Render trạng thái để assertion trong test rõ ràng và ổn định.
 */
const CartAddToCartTestHarness = () => {
  const { addItem, cart, error } = useCart();
  const [status, setStatus] = useState("idle");
  const [caughtError, setCaughtError] = useState("");

  const handleAddToCart = async () => {
    setStatus("loading");
    setCaughtError("");

    try {
      await addItem(DEFAULT_ITEM);
      setStatus("success");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Unknown error";
      setCaughtError(message);
      setStatus("failed");
    }
  };

  return (
    <div>
      <button onClick={handleAddToCart}>Add to cart</button>
      <p data-testid="status">{status}</p>
      <p data-testid="context-error">{error ?? ""}</p>
      <p data-testid="caught-error">{caughtError}</p>
      <p data-testid="total-items">{cart?.totalItems ?? 0}</p>
    </div>
  );
};

describe("Cart - Mock Testing for cartService.addToCart", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();

    // Mock mặc định cho luồng fetch cart khi CartProvider mount.
    mockedCartService.getCart.mockResolvedValue({
      userId: "user-1",
      items: [],
      totalItems: 0,
      totalPrice: 0,
    });
  });

  // Case 1:
  // Khi chưa đăng nhập (không có userId trong localStorage),
  // addItem phải throw lỗi yêu cầu đăng nhập và KHÔNG được gọi cartService.addToCart.
  it("throws login error and does not call addToCart when user is not logged in", async () => {
    const user = userEvent.setup();

    render(
      <CartProvider>
        <CartAddToCartTestHarness />
      </CartProvider>
    );

    await user.click(screen.getByRole("button", { name: /add to cart/i }));

    // Verify trạng thái thất bại được bắt từ error throw ra bởi addItem.
    await waitFor(() => {
      expect(screen.getByTestId("status")).toHaveTextContent("failed");
    });

    // Verify không gọi external dependency khi chưa có userId.
    expect(mockedCartService.addToCart).not.toHaveBeenCalled();

    // Verify đúng message throw ra cho client.
    expect(screen.getByTestId("caught-error")).toHaveTextContent(
      /vui l.+ng dang nh.+p/i
    );
  });

  // Case 2:
  // Khi API addToCart trả về thành công,
  // cần verify hàm mock được gọi đúng tham số (userId + payload item)
  // và state giỏ hàng được cập nhật tương ứng.
  it("calls addToCart with correct arguments and updates cart when response is successful", async () => {
    const user = userEvent.setup();
    localStorage.setItem("userId", "user-1");
    mockedCartService.addToCart.mockResolvedValue(MOCK_CART_RESPONSE);

    render(
      <CartProvider>
        <CartAddToCartTestHarness />
      </CartProvider>
    );

    await user.click(screen.getByRole("button", { name: /add to cart/i }));

    // Verify gọi API đúng 1 lần với đúng userId và payload item.
    await waitFor(() => {
      expect(mockedCartService.addToCart).toHaveBeenCalledTimes(1);
      expect(mockedCartService.addToCart).toHaveBeenCalledWith(
        "user-1",
        DEFAULT_ITEM
      );
    });

    // Verify trạng thái UI sau khi add thành công.
    expect(screen.getByTestId("status")).toHaveTextContent("success");
    expect(screen.getByTestId("context-error")).toHaveTextContent("");
    expect(screen.getByTestId("total-items")).toHaveTextContent("1");
  });

  // Case 3:
  // Khi API addToCart thất bại và có response.data.message,
  // CartProvider phải ưu tiên message từ API để set error context và throw lại đúng message đó.
  it("handles failed response with API message, sets context error, and rethrows same message", async () => {
    const user = userEvent.setup();
    localStorage.setItem("userId", "user-1");

    // Mô phỏng lỗi kiểu Axios có message trong response.data.message.
    mockedCartService.addToCart.mockRejectedValue({
      response: {
        data: {
          message: "Sản phẩm vượt quá tồn kho",
        },
      },
      message: "Network Error",
    });

    render(
      <CartProvider>
        <CartAddToCartTestHarness />
      </CartProvider>
    );

    await user.click(screen.getByRole("button", { name: /add to cart/i }));

    // Verify có gọi API với tham số đúng trước khi thất bại.
    await waitFor(() => {
      expect(mockedCartService.addToCart).toHaveBeenCalledTimes(1);
      expect(mockedCartService.addToCart).toHaveBeenCalledWith(
        "user-1",
        DEFAULT_ITEM
      );
    });

    // Verify error message ưu tiên lấy từ response.data.message.
    expect(screen.getByTestId("status")).toHaveTextContent("failed");
    expect(screen.getByTestId("context-error")).toHaveTextContent(
      "Sản phẩm vượt quá tồn kho"
    );
    expect(screen.getByTestId("caught-error")).toHaveTextContent(
      "Sản phẩm vượt quá tồn kho"
    );
  });

  // Case 4:
  // Khi API addToCart thất bại nhưng không có response.data.message,
  // CartProvider phải fallback về error.message mặc định của Error object.
  it("handles failed response without API message by falling back to generic error.message", async () => {
    const user = userEvent.setup();
    localStorage.setItem("userId", "user-1");
    mockedCartService.addToCart.mockRejectedValue(
      new Error("Không thể thêm vào giỏ hàng")
    );

    render(
      <CartProvider>
        <CartAddToCartTestHarness />
      </CartProvider>
    );

    await user.click(screen.getByRole("button", { name: /add to cart/i }));

    // Verify vẫn gọi API với tham số đúng trong nhánh thất bại.
    await waitFor(() => {
      expect(mockedCartService.addToCart).toHaveBeenCalledTimes(1);
      expect(mockedCartService.addToCart).toHaveBeenCalledWith(
        "user-1",
        DEFAULT_ITEM
      );
    });

    // Verify fallback message khi response.data.message không tồn tại.
    expect(screen.getByTestId("status")).toHaveTextContent("failed");
    expect(screen.getByTestId("context-error")).toHaveTextContent(
      "Không thể thêm vào giỏ hàng"
    );
    expect(screen.getByTestId("caught-error")).toHaveTextContent(
      "Không thể thêm vào giỏ hàng"
    );
  });
});
