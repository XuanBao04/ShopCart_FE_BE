import { useState } from "react";
import { Outlet, Navigate } from "react-router-dom";

export default function HeaderLayout() {
  const [user, setUser] = useState(null);
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <h1 className="text-2xl font-bold text-gray-900">ShopCart</h1>
        </nav>
      </header>
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <p className="text-gray-600">Welcome to ShopCart Frontend</p>
      </main>
      <Outlet />
    </div>
  );
}
