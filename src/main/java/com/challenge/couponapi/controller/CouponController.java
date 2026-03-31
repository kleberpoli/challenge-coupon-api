package com.challenge.couponapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.challenge.couponapi.domain.model.Coupon;
import com.challenge.couponapi.dto.CouponRequest;
import com.challenge.couponapi.dto.CouponResponse;
import com.challenge.couponapi.service.CouponService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing coupon resources.
 * 
 * Technical Note: The controller is kept thin by delegating error handling to a
 * Global Exception Handler.
 */
@RestController
@RequestMapping("/coupons")
@Tag(name = "Coupons", description = "Endpoints for coupon lifecycle management")
public class CouponController {

	private final CouponService couponService;

	/**
	 * Manual constructor for Dependency Injection. This approach is preferred over
	 * Lombok in Controllers to ensure accurate code coverage instrumentation by
	 * JaCoCo.
	 */
	public CouponController(CouponService couponService) {
		this.couponService = couponService;
	}

	/**
	 * Creates a new coupon.
	 * @Valid triggers the Bean Validation rules defined in CouponRequest.
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a new coupon", description = "Validates and persists a new coupon.")
	public Coupon createCoupon(@RequestBody @Valid CouponRequest couponRequest) {
		return couponService.createCoupon(couponRequest);
	}

	/**
	 * Retrieves a coupon by its ID.
	 */
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Get coupon by ID", description = "Fetches a coupon using its ID.")
	public CouponResponse getCouponById(@PathVariable String id) {
        return CouponResponse.fromEntity(couponService.getCouponById(id));
    }

	/**
	 * Retrieves a coupon by its unique code.
	 */
	@GetMapping("/code/{code}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Get coupon by code", description = "Fetches a coupon using its unique code.")
	public CouponResponse getCouponByCode(@PathVariable String code) {
		return CouponResponse.fromEntity(couponService.getCouponByCode(code));
	}

	/**
	 * Deletes a coupon (Soft Delete).
	 */
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content is more standard for successful deletes
	@Operation(summary = "Delete a coupon", description = "Performs a soft delete on the coupon matching the given ID.")
	public void deleteCoupon(@PathVariable String id) {
		couponService.deleteCoupon(id);
	}
}