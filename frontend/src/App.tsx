// import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import HeaderLayout from "./components/HeaderLayout/HeaderLayout";
import ProductList from "./components/ProductList/ProductList";
import Cart from "./components/Cart/Cart";
import LoginPage from "./pages/login/LoginPage";
import Order from "./components/Order/Order";
import Inventory from "./components/Inventory/Inventory";
import AdminDashboard from "./pages/admin/AdminDashboard";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/authenticated" element={<HeaderLayout />}>
          <Route path="products" element={<ProductList />} />
          <Route path="cart" element={<Cart />} />
          <Route path="orders" element={<Order />} />
          <Route path="inventory" element={<Inventory />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
