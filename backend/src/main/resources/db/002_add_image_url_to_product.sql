-- Migration: Add imageUrl to products
-- Created: 2026-05-11

ALTER TABLE products ADD COLUMN image_url VARCHAR(255);
