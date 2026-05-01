import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import InventoryManagement from "../../components/admin/InventoryManagement";
import OrderManagement from "../../components/admin/OrderManagement";
import CouponManagement from "../../components/admin/CouponManagement";
import { FaBox, FaClipboardList, FaSignOutAlt, FaTag } from "react-icons/fa";
import apiClient from "../../services/api/apiClient";

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"inventory" | "orders" | "coupons">("inventory");

  useEffect(() => {
    const role = localStorage.getItem("role");
    const storedUsername = localStorage.getItem("username");

    if (role !== "ADMIN") {
      navigate("/login", { replace: true });
      return;
    }

    setUsername(storedUsername);
  }, [navigate]);

  const handleLogout = async () => {
    try {
      await apiClient.post("/auth/logout");
    } catch {
      // Continue with client-side logout even if server logout fails
    }
    localStorage.removeItem("userId");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    navigate("/login", { replace: true });
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">
             Admin Dashboard
          </h1>
          <div className="flex items-center gap-4">
            <span className="text-gray-600">Xin chào, {username}</span>
            <button
              onClick={handleLogout}
              className="flex items-center gap-2 bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
            >
              <FaSignOutAlt />
              Đăng xuất
            </button>
          </div>
        </nav>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Tab Navigation */}
        <div className="flex gap-4 mb-8 flex-wrap">
          <button
            onClick={() => setActiveTab("inventory")}
            className={`flex items-center gap-2 px-6 py-3 rounded font-semibold transition ${
              activeTab === "inventory"
                ? "bg-blue-600 text-white"
                : "bg-white text-gray-700 hover:bg-gray-50"
            }`}
          >
            <FaBox />
            Quản lý Tồn kho
          </button>

          <button
            onClick={() => setActiveTab("orders")}
            className={`flex items-center gap-2 px-6 py-3 rounded font-semibold transition ${
              activeTab === "orders"
                ? "bg-blue-600 text-white"
                : "bg-white text-gray-700 hover:bg-gray-50"
            }`}
          >
            <FaClipboardList />
            Quản lý Đơn hàng
          </button>

          <button
            onClick={() => setActiveTab("coupons")}
            className={`flex items-center gap-2 px-6 py-3 rounded font-semibold transition ${
              activeTab === "coupons"
                ? "bg-blue-600 text-white"
                : "bg-white text-gray-700 hover:bg-gray-50"
            }`}
          >
            <FaTag />
            Quản lý Mã giảm
          </button>
        </div>

        {/* Tab Content */}
        <div className="bg-white rounded-lg shadow">
          {activeTab === "inventory" && <InventoryManagement />}
          {activeTab === "orders" && <OrderManagement />}
          {activeTab === "coupons" && <CouponManagement />}
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;
