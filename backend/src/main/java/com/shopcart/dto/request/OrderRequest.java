package com.shopcart.dto.request;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private String userId;
    private List<OrderItemRequest> orderItems;
}
