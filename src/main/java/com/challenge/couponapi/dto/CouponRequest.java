package com.challenge.couponapi.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO representing the request payload to create a new coupon.
 */
public record CouponRequest(

		@NotBlank(message = "Code is required")
		String code,

		@NotBlank(message = "Description is required")
		@Size(max = 500, message = "Description must not exceed 500 characters")
		String description,

		@NotNull(message = "Discount value is required")
	    @DecimalMin(value = "0.5", message = "Discount value must be at least 0.5")
	    BigDecimal discountValue,

		@NotNull(message = "Expiration Date is required")
		OffsetDateTime expirationDate,

		boolean published

) {
}