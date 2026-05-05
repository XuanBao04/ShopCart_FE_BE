package com.shopcart.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class CreateCouponRequest {
    @NotBlank(message = "Coupon code cannot be empty")
    private String code;

    @Min(value = 1, message = "Discount percent must be between 1 and 100")
    @Max(value = 100, message = "Discount percent must be between 1 and 100")
    private Integer discountPercent;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Long minimumOrderAmount = 0L;  // VND

    private LocalDateTime expiryDate;  // null = no expiry
}
