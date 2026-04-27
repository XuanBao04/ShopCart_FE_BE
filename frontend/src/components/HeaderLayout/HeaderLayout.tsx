import { useState, useEffect } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import { FaShoppingCart } from "react-icons/fa";
import apiClient from "../../services/api/apiClient";

export default function HeaderLayout() {
  const [userId, setUserId] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const storedUserId = localStorage.getItem("userId");
    if (storedUserId) {
      setUserId(storedUserId);
    } else {
      navigate("/login");
    }
  }, []);

  const handleLogout = async () => {
    try {
      await apiClient.post("/auth/logout");
    } catch (error) {
      // Continue with client-side logout even if server logout fails
    }
    localStorage.removeItem("userId");
    setUserId(null);
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
            <button
              onClick={() => navigate("/authenticated/cart")}
              className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 flex items-center gap-2"
            >
              <FaShoppingCart />
              Giỏ hàng
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
        <p className="text-gray-600">Welcome to ShopCart Frontend</p>
      </main>

      <Outlet />
    </div>
  );
}
