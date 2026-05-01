-- Migration: Add discount support to orders and update coupons table
-- Created: 2024-04-30

-- Add discount_amount column to orders table
ALTER TABLE orders ADD COLUMN discount_amount BIGINT DEFAULT 0;

-- Update coupons table with new columns
ALTER TABLE coupons ADD COLUMN minimum_order_amount BIGINT DEFAULT 0;
ALTER TABLE coupons ADD COLUMN expiry_date TIMESTAMP NULL;
ALTER TABLE coupons ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE coupons ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add index for coupon lookups
CREATE INDEX idx_coupon_active ON coupons(active);
CREATE INDEX idx_coupon_expiry ON coupons(expiry_date);
