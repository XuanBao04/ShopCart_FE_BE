import { useState } from "react";
import { ShippingAddress } from "../../types/order";

interface AddressFormProps {
  onAddressChange: (address: ShippingAddress) => void;
}

export default function AddressForm({ onAddressChange }: AddressFormProps) {
  const [address, setAddress] = useState<ShippingAddress>({
    shippingAddress: "",
    city: "",
    district: "",
    ward: "",
    postalCode: "",
    phoneNumber: "",
  });

  const [errors, setErrors] = useState<Partial<ShippingAddress>>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    let processedValue = value;

    // Phone number: only digits, max 10
    if (name === "phoneNumber") {
      processedValue = value.replace(/\D/g, "").slice(0, 10);
    }

    const updatedAddress = { ...address, [name]: processedValue };
    setAddress(updatedAddress);
    onAddressChange(updatedAddress);
    
    // Clear error for this field when user starts typing
    if (errors[name as keyof ShippingAddress]) {
      setErrors({
        ...errors,
        [name]: undefined,
      });
    }
  };


  return (
    <div className="border rounded-lg p-4 bg-white">
      <h2 className="text-xl font-bold mb-4">Thông tin giao hàng</h2>
      
      <div className="space-y-4">
        {/* Shipping Address */}
        <div>
          <label htmlFor="shippingAddress" className="block text-sm font-medium text-gray-700 mb-1">
            Địa chỉ giao hàng <span className="text-red-500">*</span>
          </label>
          <input
            id="shippingAddress"
            type="text"
            name="shippingAddress"
            value={address.shippingAddress}
            onChange={handleChange}
            placeholder="Ví dụ: 123 Đường ABC, Toà nhà XYZ"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
              errors.shippingAddress
                ? "border-red-500 focus:ring-red-500"
                : "border-gray-300 focus:ring-blue-500"
            }`}
          />
          {errors.shippingAddress && (
            <p className="text-red-500 text-sm mt-1">{errors.shippingAddress}</p>
          )}
        </div>

        {/* City/Province */}
        <div>
          <label htmlFor="city" className="block text-sm font-medium text-gray-700 mb-1">
            Thành phố/Tỉnh <span className="text-red-500">*</span>
          </label>
          <input
            id="city"
            type="text"
            name="city"
            value={address.city}
            onChange={handleChange}
            placeholder="Ví dụ: Thành phố Hồ Chí Minh"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
              errors.city
                ? "border-red-500 focus:ring-red-500"
                : "border-gray-300 focus:ring-blue-500"
            }`}
          />
          {errors.city && (
            <p className="text-red-500 text-sm mt-1">{errors.city}</p>
          )}
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* District */}
          <div>
            <label htmlFor="district" className="block text-sm font-medium text-gray-700 mb-1">
              Quận/Huyện <span className="text-red-500">*</span>
            </label>
            <input
              id="district"
              type="text"
              name="district"
              value={address.district}
              onChange={handleChange}
              placeholder="Ví dụ: Quận 1"
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                errors.district
                  ? "border-red-500 focus:ring-red-500"
                  : "border-gray-300 focus:ring-blue-500"
              }`}
            />
            {errors.district && (
              <p className="text-red-500 text-sm mt-1">{errors.district}</p>
            )}
          </div>

          {/* Ward */}
          <div>
            <label htmlFor="ward" className="block text-sm font-medium text-gray-700 mb-1">
              Phường/Xã <span className="text-red-500">*</span>
            </label>
            <input
              id="ward"
              type="text"
              name="ward"
              value={address.ward}
              onChange={handleChange}
              placeholder="Ví dụ: Phường Bến Nghé"
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                errors.ward
                  ? "border-red-500 focus:ring-red-500"
                  : "border-gray-300 focus:ring-blue-500"
              }`}
            />
            {errors.ward && (
              <p className="text-red-500 text-sm mt-1">{errors.ward}</p>
            )}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Postal Code */}
          <div>
            <label htmlFor="postalCode" className="block text-sm font-medium text-gray-700 mb-1">
              Mã bưu điện
            </label>
            <input
              id="postalCode"
              type="text"
              name="postalCode"
              value={address.postalCode}
              onChange={handleChange}
              placeholder="Ví dụ: 700000"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Phone Number */}
          <div>
            <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-700 mb-1">
              Số điện thoại <span className="text-red-500">*</span>
            </label>
            <input
              id="phoneNumber"
              type="tel"
              name="phoneNumber"
              value={address.phoneNumber}
              onChange={handleChange}
              placeholder="Ví dụ: 0123456789"
              maxLength={10}
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                errors.phoneNumber
                  ? "border-red-500 focus:ring-red-500"
                  : "border-gray-300 focus:ring-blue-500"
              }`}
            />
            {errors.phoneNumber && (
              <p className="text-red-500 text-sm mt-1">{errors.phoneNumber}</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
