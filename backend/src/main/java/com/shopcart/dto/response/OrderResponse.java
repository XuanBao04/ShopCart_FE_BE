package com.shopcart.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private List<OrderItemResponse> items;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
