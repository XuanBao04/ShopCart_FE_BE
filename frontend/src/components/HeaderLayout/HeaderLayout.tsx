import { useState, useEffect } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import { FaShoppingCart, FaClipboardList, FaShieldAlt } from "react-icons/fa";
import apiClient from "../../services/api/apiClient";
import { useCart } from "../../hooks/useCart";

export default function HeaderLayout() {
  const [userId, setUserId] = useState<string | null>(null);
  const [role, setRole] = useState<string | null>(null);
  const navigate = useNavigate();
  const { cart } = useCart();

  useEffect(() => {
    const storedUserId = localStorage.getItem("userId");
    const storedRole = localStorage.getItem("role");
    if (storedUserId) {
      setUserId(storedUserId);
      setRole(storedRole);
    } else {
      navigate("/login");
    }
  }, []);

  const handleLogout = async () => {
    try {
      await apiClient.post("/auth/logout");
    } catch {
      // Continue with client-side logout even if server logout fails
    }
    localStorage.removeItem("userId");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    setUserId(null);
    setRole(null);
    navigate("/login"); // chuyển về login
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1
            className="text-2xl font-bold text-gray-900 cursor-pointer"
            onClick={() => navigate("/authenticated/products")}
          >
            ShopCart
          </h1>

          <div className="flex items-center gap-4">
            {role === "ADMIN" && (
              <button
                onClick={() => navigate("/admin/dashboard")}
                className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 flex items-center gap-2"
              >
                <FaShieldAlt />
                Quản lý
              </button>
            )}

            <button
              onClick={() => navigate("/authenticated/cart")}
              className="relative bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 flex items-center gap-2"
            >
              <FaShoppingCart />
              Giỏ hàng
              {cart && cart.totalItems > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded-full">
                  {cart.totalItems}
                </span>
              )}
            </button>

            <button
              onClick={() => navigate("/authenticated/orders")}
              className="bg-indigo-500 text-white px-4 py-2 rounded hover:bg-indigo-600 flex items-center gap-2"
            >
              <FaClipboardList />
              Đơn hàng
            </button>

            {userId ? (
              <button
                onClick={handleLogout}
                className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
              >
                Đăng xuất
              </button>
            ) : (
              <button
                onClick={() => navigate("/login")}
                className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
              >
                Đăng nhập
              </button>
            )}
          </div>
        </nav>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <p className="text-gray-600">Welcome to ShopCart</p>
      </main>

      <Outlet />
    </div>
  );
}

