package com.challenge.couponapi.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.challenge.couponapi.domain.model.Coupon;

/**
 * DTO representing a response payload for discount coupons.
 */
public record CouponResponse(
	    String id,
	    String code,
	    String description,
	    BigDecimal discountValue,
	    OffsetDateTime expirationDate,
	    String status,
	    boolean published,
	    boolean redeemed
	) {
	    /**
	     * Mapping Constructor (Static Factory Method).
	     */
	    public static CouponResponse fromEntity(Coupon coupon) {
	        return new CouponResponse(
	            coupon.getId(),
	            coupon.getCode(),
	            coupon.getDescription(),
	            coupon.getDiscountValue(),
	            coupon.getExpirationDate(),
	            coupon.getStatus().name(),
	            coupon.isPublished(),
	            coupon.isRedeemed()
	        );
	    }
	}