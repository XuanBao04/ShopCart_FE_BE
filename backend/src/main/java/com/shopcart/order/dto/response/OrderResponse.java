package com.shopcart.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private List<OrderItemResponse> items;
    private Long subtotal;         
    private Long discountAmount;  
    private Long shippingFee;
    private Long totalPrice;       
    private String couponCode;     
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedDate;

    // Shipping address fields
    private String shippingAddress;
    private String city;
    private String district;
    private String ward;
    private String postalCode;
    private String phoneNumber;
}
