// import "./App.css";
import { ReactElement, useEffect } from "react";
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from "react-router-dom";
import HeaderLayout from "./components/HeaderLayout/HeaderLayout";
import ProductList from "./components/ProductList/ProductList";
import Cart from "./components/Cart/Cart";
import LoginPage from "./pages/login/LoginPage";
import Order from "./components/Order/Order";
import AdminDashboard from "./pages/admin/AdminDashboard";
import { CartProvider } from "./context/CartContext";

function RequireAuth({ children }: { children: ReactElement }) {
  const navigate = useNavigate();
  const userId = localStorage.getItem("userId");

  useEffect(() => {
    if (!userId) {
      const shouldGoToLogin = confirm("Vui l�ng dang nh?p d? ti?p t?c.");
      if (shouldGoToLogin) {
        navigate("/login", { replace: true });
      } else {
        navigate("/authenticated/products", { replace: true });
      }
    }
  }, [userId, navigate]);

  if (!userId) return null;
  return children;
}

function App() {
  return (
    <BrowserRouter>
      <CartProvider>
        <Routes>
          <Route path="/" element={<Navigate to="/authenticated/products" />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/authenticated" element={<HeaderLayout />}>
            <Route path="products" element={<ProductList />} />
            <Route
              path="cart"
              element={
                <RequireAuth>
                  <Cart />
                </RequireAuth>
              }
            />
            <Route
              path="orders"
              element={
                <RequireAuth>
                  <Order />
                </RequireAuth>
              }
            />
          </Route>
        </Routes>
      </CartProvider>
    </BrowserRouter>
  );
}

export default App;
