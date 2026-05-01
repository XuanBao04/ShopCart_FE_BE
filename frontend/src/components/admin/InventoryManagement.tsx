import React, { useEffect, useState } from "react";
import { productService } from "../../services/api/productService";
import { inventoryService } from "../../services/api/inventoryService";
import { Product } from "../../types/product";
import { InventoryItem } from "../../types/inventory";
import { formatPrice } from "../../utils/priceCalculation";
import { FaSync, FaEdit, FaCheck, FaTimes } from "react-icons/fa";

interface InventoryWithProduct extends Product {
  inventory?: InventoryItem;
  stock?: number;
}

const InventoryManagement = () => {
  const [products, setProducts] = useState<InventoryWithProduct[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editQuantity, setEditQuantity] = useState<number>(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<"ALL" | "ACTIVE" | "INACTIVE">("ALL");

  const fetchInventory = async () => {
    setIsLoading(true);
    setError(null);
    try {
      // Fetch all products
      const allProducts = await productService.getAllProducts();

      // Fetch inventory for each product
      const productsWithInventory = await Promise.all(
        allProducts.map(async (product) => {
          try {
            const stock = await productService.getAvailableStock(product.id);
            return { ...product, stock };
          } catch {
            // If inventory not found, set stock to 0
            return { ...product, stock: 0 };
          }
        })
      );

      setProducts(productsWithInventory);
    } catch (err) {
      setError((err as Error).message || "Không thể tải dữ liệu tồn kho");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchInventory();
  }, []);

  const handleEditStart = (productId: string, currentQuantity: number) => {
    setEditingId(productId);
    setEditQuantity(currentQuantity);
  };

  const handleSaveInventory = async (productId: string) => {
    try {
      await inventoryService.updateInventoryItem(productId, editQuantity);
      
      // Update local state
      setProducts(products.map(p => 
        p.id === productId ? { ...p, stock: editQuantity } : p
      ));
      
      setEditingId(null);
      // Show success message (optional - implement toast if needed)
    } catch (err) {
      alert("Lỗi cập nhật tồn kho: " + (err as Error).message);
    }
  };

  const handleCancel = () => {
    setEditingId(null);
    setEditQuantity(0);
  };

  // Filter products
  let filteredProducts = products;

  if (statusFilter !== "ALL") {
    filteredProducts = filteredProducts.filter(p => p.status === statusFilter);
  }

  if (searchTerm) {
    filteredProducts = filteredProducts.filter(p =>
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.id.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }

  return (
    <div className="p-6">
      <div className="mb-6">
        <h2 className="text-2xl font-bold mb-4">Quản lý Tồn kho Sản phẩm</h2>

        {error && (
          <div className="bg-red-100 text-red-700 p-4 rounded mb-4">
            {error}
          </div>
        )}

        {/* Controls */}
        <div className="flex flex-col md:flex-row gap-4 mb-6">
          {/* Search */}
          <input
            type="text"
            placeholder="Tìm kiếm sản phẩm..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-1 px-4 py-2 border rounded"
          />

          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as "ALL" | "ACTIVE" | "INACTIVE")}
            className="px-4 py-2 border rounded"
          >
            <option value="ALL">Tất cả</option>
            <option value="ACTIVE">Đang hoạt động</option>
            <option value="INACTIVE">Không hoạt động</option>
          </select>

          {/* Refresh Button */}
          <button
            onClick={fetchInventory}
            disabled={isLoading}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400"
          >
            <FaSync className={isLoading ? "animate-spin" : ""} />
            {isLoading ? "Đang tải..." : "Làm mới"}
          </button>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-blue-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Tổng sản phẩm</p>
            <p className="text-2xl font-bold text-blue-600">{products.length}</p>
          </div>
          <div className="bg-green-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Đang hoạt động</p>
            <p className="text-2xl font-bold text-green-600">
              {products.filter(p => p.status === "ACTIVE").length}
            </p>
          </div>
          <div className="bg-orange-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Tổng tồn kho</p>
            <p className="text-2xl font-bold text-orange-600">
              {products.reduce((sum, p) => sum + (p.stock || 0), 0)}
            </p>
          </div>
        </div>

        {/* Table */}
        {isLoading ? (
          <div className="text-center py-8">
            <p className="text-gray-600">Đang tải dữ liệu...</p>
          </div>
        ) : filteredProducts.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-600">Không có sản phẩm nào</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b-2 border-gray-200">
                  <th className="text-left py-3 px-4">ID Sản phẩm</th>
                  <th className="text-left py-3 px-4">Tên sản phẩm</th>
                  <th className="text-right py-3 px-4">Giá</th>
                  <th className="text-center py-3 px-4">Tồn kho</th>
                  <th className="text-center py-3 px-4">Trạng thái</th>
                  <th className="text-center py-3 px-4">Hành động</th>
                </tr>
              </thead>
              <tbody>
                {filteredProducts.map((product) => (
                  <tr key={product.id} className="border-b hover:bg-gray-50">
                    <td className="py-3 px-4 font-mono text-sm">{product.id}</td>
                    <td className="py-3 px-4">{product.name}</td>
                    <td className="py-3 px-4 text-right">{formatPrice(product.price)}</td>
                    <td className="py-3 px-4 text-center">
                      {editingId === product.id ? (
                        <input
                          type="number"
                          min="0"
                          value={editQuantity}
                          onChange={(e) => setEditQuantity(parseInt(e.target.value) || 0)}
                          className="w-20 px-2 py-1 border rounded text-center"
                        />
                      ) : (
                        <span className={`font-semibold ${
                          (product.stock || 0) <= 10 ? "text-red-600" : "text-green-600"
                        }`}>
                          {product.stock || 0}
                        </span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span className={`inline-block px-3 py-1 rounded text-sm font-medium ${
                        product.status === "ACTIVE"
                          ? "bg-green-100 text-green-800"
                          : "bg-red-100 text-red-800"
                      }`}>
                        {product.status === "ACTIVE" ? "Hoạt động" : "Ngừng hoạt động"}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-center">
                      {editingId === product.id ? (
                        <div className="flex justify-center gap-2">
                          <button
                            onClick={() => handleSaveInventory(product.id)}
                            className="text-green-600 hover:text-green-800 p-2"
                            title="Lưu"
                          >
                            <FaCheck />
                          </button>
                          <button
                            onClick={handleCancel}
                            className="text-red-600 hover:text-red-800 p-2"
                            title="Hủy"
                          >
                            <FaTimes />
                          </button>
                        </div>
                      ) : (
                        <button
                          onClick={() => handleEditStart(product.id, product.stock || 0)}
                          className="text-blue-600 hover:text-blue-800 p-2"
                          title="Chỉnh sửa"
                        >
                          <FaEdit />
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default InventoryManagement;
