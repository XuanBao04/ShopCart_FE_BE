package com.shopcart.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCouponRequest {
    @Min(value = 1, message = "Discount percent must be between 1 and 100")
    @Max(value = 100, message = "Discount percent must be between 1 and 100")
    private Integer discountPercent;

    private Boolean active;

    private Long minimumOrderAmount;  // VND

    private LocalDateTime expiryDate;  // null = no expiry
}
