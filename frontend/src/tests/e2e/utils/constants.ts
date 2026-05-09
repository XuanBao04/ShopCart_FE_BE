/**
 * Constants: Chứa tất cả các hằng số sử dụng trong E2E tests
 * Bao gồm cấu hình API, phí vận chuyển, và dữ liệu test mẫu
 */

export const SHIPPING_FEE = 29900;

export const API_URL =
  process.env.VITE_API_URL || 'http://localhost:8080/api';

export const TEST_ADDRESS = {
  shippingAddress: '123 Đường Nguyễn Hữu Cảnh, Tòa nhà A',
  city: 'Thành phố Hồ Chí Minh',
  district: 'Quận 1',
  ward: 'Phường Bến Nghé',
  postalCode: '700000',
  phoneNumber: '0912345678',
};
