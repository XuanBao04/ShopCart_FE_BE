import React, { useEffect, useState } from "react";
import { couponService } from "../../services/api/couponService";
import { CouponResponse, CouponRequest, UpdateCouponRequest } from "../../types/coupon";
import { formatPrice } from "../../utils/priceCalculation";
import { FaSync, FaEdit, FaTrash, FaCheck, FaTimes, FaPlus } from "react-icons/fa";
import { toast } from "react-toastify";

interface CreateFormData {
  code: string;
  discountPercent: string;
  active: boolean;
  minimumOrderAmount: string;
  expiryDate: string;
}

interface EditFormData {
  discountPercent: string;
  active: boolean;
  minimumOrderAmount: string;
  expiryDate: string;
}

const CouponManagement = () => {
  const [coupons, setCoupons] = useState<CouponResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingCode, setEditingCode] = useState<string | null>(null);

  // Create form state
  const [createForm, setCreateForm] = useState<CreateFormData>({
    code: "",
    discountPercent: "10",
    active: true,
    minimumOrderAmount: "0",
    expiryDate: "",
  });

  // Edit form state
  const [editForm, setEditForm] = useState<EditFormData>({
    discountPercent: "10",
    active: true,
    minimumOrderAmount: "0",
    expiryDate: "",
  });

  const fetchCoupons = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await couponService.getAllCoupons();
      setCoupons(data);
    } catch (err) {
      setError((err as Error).message || "Không thể tải dữ liệu mã giảm giá");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCoupons();
  }, []);

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!createForm.code.trim()) {
      toast.error("Mã giảm giá không được để trống");
      return;
    }
    if (parseInt(createForm.discountPercent) <= 0 || parseInt(createForm.discountPercent) > 100) {
      toast.error("Phần trăm giảm giá phải từ 1-100");
      return;
    }

    try {
      const request: CouponRequest = {
        code: createForm.code.toUpperCase(),
        discountPercent: parseInt(createForm.discountPercent),
        active: createForm.active,
        minimumOrderAmount: parseInt(createForm.minimumOrderAmount) || 0,
        expiryDate: createForm.expiryDate || undefined,
      };

      await couponService.createCoupon(request);
      await fetchCoupons();
      setShowCreateForm(false);
      setCreateForm({
        code: "",
        discountPercent: "10",
        active: true,
        minimumOrderAmount: "0",
        expiryDate: "",
      });
    } catch (err) {
      toast.error("Lỗi tạo mã giảm giá: " + (err as Error).message);
    }
  };

  const handleEditStart = (coupon: CouponResponse) => {
    setEditingCode(coupon.code);
    setEditForm({
      discountPercent: coupon.discountPercent.toString(),
      active: coupon.active,
      minimumOrderAmount: coupon.minimumOrderAmount.toString(),
      expiryDate: coupon.expiryDate ? new Date(coupon.expiryDate).toISOString().split("T")[0] : "",
    });
  };

  const handleEditSubmit = async (code: string) => {
    if (parseInt(editForm.discountPercent) <= 0 || parseInt(editForm.discountPercent) > 100) {
      toast.error("Phần trăm giảm giá phải từ 1-100");
      return;
    }

    try {
      const request: UpdateCouponRequest = {
        discountPercent: parseInt(editForm.discountPercent),
        active: editForm.active,
        minimumOrderAmount: parseInt(editForm.minimumOrderAmount) || 0,
        expiryDate: editForm.expiryDate || undefined,
      };

      await couponService.updateCoupon(code, request);
      await fetchCoupons();
      setEditingCode(null);
    } catch (err) {
      toast.error("Lỗi cập nhật mã giảm giá: " + (err as Error).message);
    }
  };

  const handleDelete = async (code: string) => {
    if (confirm(`Bạn có chắc muốn xóa mã giảm giá ${code}?`)) {
      try {
        await couponService.deleteCoupon(code);
        await fetchCoupons();
      } catch (err) {
        toast.error("Lỗi xóa mã giảm giá: " + (err as Error).message);
      }
    }
  };

  const handleCancel = () => {
    setEditingCode(null);
    setShowCreateForm(false);
  };

  // Filter coupons
  let filteredCoupons = coupons;
  if (searchTerm) {
    filteredCoupons = filteredCoupons.filter((c) =>
      c.code.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }

  const isExpired = (expiryDate: string | null) => {
    if (!expiryDate) return false;
    return new Date(expiryDate) < new Date();
  };

  const formatDate = (dateString: string | null) => {
    if (!dateString) return "Không có hạn";
    return new Date(dateString).toLocaleDateString("vi-VN");
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h2 className="text-2xl font-bold mb-4">Quản lý Mã giảm giá</h2>

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
            placeholder="Tìm kiếm mã giảm giá..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-1 px-4 py-2 border rounded"
          />

          {/* Refresh Button */}
          <button
            onClick={fetchCoupons}
            disabled={isLoading}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400"
          >
            <FaSync className={isLoading ? "animate-spin" : ""} />
            {isLoading ? "Đang tải..." : "Làm mới"}
          </button>

          {/* Create Button */}
          <button
            onClick={() => setShowCreateForm(!showCreateForm)}
            className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
          >
            <FaPlus />
            Tạo mới
          </button>
        </div>

        {/* Create Form */}
        {showCreateForm && (
          <div className="bg-gray-50 p-6 rounded mb-6 border border-gray-200">
            <h3 className="text-lg font-bold mb-4">Tạo mã giảm giá mới</h3>
            <form onSubmit={handleCreateSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-2">Mã giảm giá</label>
                <input
                  type="text"
                  value={createForm.code}
                  onChange={(e) => setCreateForm({ ...createForm, code: e.target.value.toUpperCase() })}
                  placeholder="VD: WELCOME10"
                  className="w-full px-3 py-2 border rounded"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">Phần trăm giảm (%)</label>
                <input
                  type="number"
                  min="1"
                  max="100"
                  value={createForm.discountPercent}
                  onChange={(e) => setCreateForm({ ...createForm, discountPercent: e.target.value })}
                  className="w-full px-3 py-2 border rounded"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">Số tiền tối thiểu (VND)</label>
                <input
                  type="number"
                  min="0"
                  value={createForm.minimumOrderAmount}
                  onChange={(e) => setCreateForm({ ...createForm, minimumOrderAmount: e.target.value })}
                  className="w-full px-3 py-2 border rounded"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">Ngày hết hạn</label>
                <input
                  type="datetime-local"
                  value={createForm.expiryDate}
                  onChange={(e) => setCreateForm({ ...createForm, expiryDate: e.target.value })}
                  className="w-full px-3 py-2 border rounded"
                />
              </div>

              <div>
                <label className="flex items-center gap-2 text-sm font-medium mb-2">
                  <input
                    type="checkbox"
                    checked={createForm.active}
                    onChange={(e) => setCreateForm({ ...createForm, active: e.target.checked })}
                    className="rounded"
                  />
                  Hoạt động
                </label>
              </div>

              <div className="flex gap-2 items-end">
                <button
                  type="submit"
                  className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                >
                  <FaCheck />
                  Tạo
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreateForm(false)}
                  className="flex items-center gap-2 px-4 py-2 bg-gray-400 text-white rounded hover:bg-gray-500"
                >
                  <FaTimes />
                  Hủy
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-blue-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Tổng mã</p>
            <p className="text-2xl font-bold text-blue-600">{coupons.length}</p>
          </div>
          <div className="bg-green-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Đang hoạt động</p>
            <p className="text-2xl font-bold text-green-600">
              {coupons.filter((c) => c.active && !isExpired(c.expiryDate)).length}
            </p>
          </div>
          <div className="bg-orange-50 p-4 rounded">
            <p className="text-gray-600 text-sm">Đã hết hạn</p>
            <p className="text-2xl font-bold text-orange-600">
              {coupons.filter((c) => isExpired(c.expiryDate)).length}
            </p>
          </div>
        </div>

        {/* Table */}
        {isLoading ? (
          <div className="text-center py-8">
            <p className="text-gray-600">Đang tải dữ liệu...</p>
          </div>
        ) : filteredCoupons.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-600">Không có mã giảm giá nào</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b-2 border-gray-200">
                  <th className="text-left py-3 px-4">Mã</th>
                  <th className="text-center py-3 px-4">Giảm (%)</th>
                  <th className="text-right py-3 px-4">Tối thiểu (VND)</th>
                  <th className="text-center py-3 px-4">Hết hạn</th>
                  <th className="text-center py-3 px-4">Trạng thái</th>
                  <th className="text-center py-3 px-4">Hành động</th>
                </tr>
              </thead>
              <tbody>
                {filteredCoupons.map((coupon) => (
                  <tr key={coupon.code} className="border-b hover:bg-gray-50">
                    <td className="py-3 px-4 font-mono font-bold text-blue-600">{coupon.code}</td>
                    <td className="py-3 px-4 text-center font-semibold">{coupon.discountPercent}%</td>
                    <td className="py-3 px-4 text-right">{formatPrice(coupon.minimumOrderAmount)}</td>
                    <td className="py-3 px-4 text-center text-sm">
                      <span
                        className={isExpired(coupon.expiryDate) ? "text-red-600 font-semibold" : ""}
                      >
                        {formatDate(coupon.expiryDate)}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span
                        className={`inline-block px-3 py-1 rounded text-sm font-medium ${
                          coupon.active && !isExpired(coupon.expiryDate)
                            ? "bg-green-100 text-green-800"
                            : "bg-red-100 text-red-800"
                        }`}
                      >
                        {coupon.active && !isExpired(coupon.expiryDate) ? "Hoạt động" : "Ngừng hoạt động"}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-center">
                      {editingCode === coupon.code ? (
                        <div className="space-y-2">
                          <div className="grid grid-cols-1 gap-2 mb-2">
                            <input
                              type="number"
                              min="1"
                              max="100"
                              value={editForm.discountPercent}
                              onChange={(e) => setEditForm({ ...editForm, discountPercent: e.target.value })}
                              className="px-2 py-1 border rounded text-sm"
                              placeholder="Giảm %"
                            />
                            <input
                              type="number"
                              min="0"
                              value={editForm.minimumOrderAmount}
                              onChange={(e) =>
                                setEditForm({ ...editForm, minimumOrderAmount: e.target.value })
                              }
                              className="px-2 py-1 border rounded text-sm"
                              placeholder="Tối thiểu"
                            />
                            <input
                              type="datetime-local"
                              value={editForm.expiryDate}
                              onChange={(e) => setEditForm({ ...editForm, expiryDate: e.target.value })}
                              className="px-2 py-1 border rounded text-sm"
                            />
                            <label className="flex items-center gap-2 text-sm">
                              <input
                                type="checkbox"
                                checked={editForm.active}
                                onChange={(e) =>
                                  setEditForm({ ...editForm, active: e.target.checked })
                                }
                                className="rounded"
                              />
                              Hoạt động
                            </label>
                          </div>
                          <div className="flex justify-center gap-2">
                            <button
                              onClick={() => handleEditSubmit(coupon.code)}
                              className="text-green-600 hover:text-green-800 p-2"
                              title="Lưu"
                            >
                              <FaCheck />
                            </button>
                            <button
                              onClick={handleCancel}
                              className="text-gray-600 hover:text-gray-800 p-2"
                              title="Hủy"
                            >
                              <FaTimes />
                            </button>
                          </div>
                        </div>
                      ) : (
                        <div className="flex justify-center gap-2">
                          <button
                            onClick={() => handleEditStart(coupon)}
                            className="text-blue-600 hover:text-blue-800 p-2"
                            title="Chỉnh sửa"
                          >
                            <FaEdit />
                          </button>
                          <button
                            onClick={() => handleDelete(coupon.code)}
                            className="text-red-600 hover:text-red-800 p-2"
                            title="Xóa"
                          >
                            <FaTrash />
                          </button>
                        </div>
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

export default CouponManagement;
