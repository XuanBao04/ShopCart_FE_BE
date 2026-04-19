/**
 * API Configuration
 */
export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  TIMEOUT: 30000,
};

/**
 * Order Status
 */
export const ORDER_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  SHIPPED: 'SHIPPED',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED',
} as const;

/**
 * Product Status
 */
export const PRODUCT_STATUS = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
} as const;

/**
 * Shipping Costs
 */
export const SHIPPING_COSTS = {
  STANDARD: 29900, // VND
  EXPRESS: 49900,
  OVERNIGHT: 99900,
} as const;

/**
 * Tax Rate
 */
export const TAX_RATE = 0.1; // 10%

/**
 * Pagination
 */
export const PAGINATION = {
  PAGE_SIZE: 20,
  DEFAULT_PAGE: 1,
} as const;

/**
 * Error Messages
 */
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Lỗi kết nối. Vui lòng thử lại.',
  INVALID_EMAIL: 'Email không hợp lệ.',
  INVALID_PHONE: 'Số điện thoại không hợp lệ.',
  INVALID_QUANTITY: 'Số lượng phải là số nguyên dương.',
  INVALID_PRICE: 'Giá tiền phải là số dương.',
  PRODUCT_NOT_FOUND: 'Sản phẩm không tìm thấy.',
  INSUFFICIENT_STOCK: 'Hết hàng hoặc không đủ số lượng.',
  ORDER_NOT_FOUND: 'Đơn hàng không tìm thấy.',
} as const;
