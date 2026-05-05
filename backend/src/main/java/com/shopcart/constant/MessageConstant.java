package com.shopcart.constant;


public class MessageConstant {

    public static class Order {
        public static final String NOT_FOUND = "Order not found with id: ";
        public static final String CANNOT_CANCEL = "Cannot cancel order with status: ";
        public static final String INVALID_STATUS = "Invalid order status: ";
        public static final String EMPTY_ITEMS = "Order must contain at least one item";
    }

    public static class Product {
        public static final String NOT_FOUND = "Product not found with id: ";
        public static final String INVENTORY_NOT_FOUND = "Inventory not found for product: ";
        public static final String INSUFFICIENT_STOCK = "Insufficient stock for product: ";
        public static final String ID_REQUIRED = "Product ID cannot be null or empty";
    }

    public static class Cart {
        public static final String NOT_FOUND = "Cart item not found with id: ";
        public static final String WRONG_USER = "Cart item does not belong to user: ";
    }

    public static class Inventory {
        public static final String NOT_FOUND = "Inventory not found for product: ";
        public static final String INSUFFICIENT_STOCK = "Insufficient stock for product: ";
        public static final String INSUFFICIENT_STOCK_RESERVE = "Insufficient stock to reserve for product: ";
        public static final String POSITIVE_QUANTITY = "Quantity must be a positive integer";
        public static final String STOCK_NOT_RESERVED = "Stock not reserved for product: ";
    }

    public static class Coupon {
        public static final String NOT_FOUND = "Coupon not found: ";
        public static final String CODE_REQUIRED = "Coupon code cannot be null or empty";
        public static final String INVALID_ORDER_AMOUNT = "Order amount must be positive";
        public static final String MIN_ORDER_AMOUNT_NEGATIVE = "Minimum order amount cannot be negative";
        public static final String INACTIVE = "Coupon is no longer active: ";
        public static final String EXPIRED = "Coupon has expired: ";
        public static final String FUTURE_EXPIRY = "Expiry date cannot be in the past";
        public static final String DISCOUNT_RANGE = "Discount percent must be between %d and %d";
        public static final String MIN_ORDER_REQUIRED = "Minimum order amount %d required for this coupon. Current: %d";
    }

    public static class Auth {
        public static final String INVALID_PASSWORD = "Invalid password";
        public static final String USER_NOT_FOUND = "User not found with username: ";
        public static final String USERNAME_EXISTS = "Username already exists: ";
        public static final String LOGIN_SUCCESS = "Login successful";
        public static final String REGISTER_SUCCESS = "Registration successful";
        public static final String USER_INFO_RETRIEVED = "User info retrieved";
    }
}
