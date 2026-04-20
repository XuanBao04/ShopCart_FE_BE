package com.shopcart.dto.response;

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
    private Long shippingFee;
    private Long totalPrice;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
