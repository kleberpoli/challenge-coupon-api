package com.challenge.couponapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.challenge.couponapi.domain.model.Coupon;

/**
 * Data access layer for Coupon entity. Uses Spring Data JPA to provide standard
 * CRUD and custom query derivation.
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {

	/**
	 * Retrieves a coupon by its unique business code.
	 * 
	 * Technical Note: Returns Optional to explicitly handle the absence of a value
	 * in the Service layer, avoiding potential NullPointerExceptions.
	 */
	Optional<Coupon> findByCode(String code);

	/**
	 * Checks whether a coupon code already exists in the database, including
	 * soft-deleted records.
	 *
	 * Technical note: This method is optimized for existence checks. Instead of
	 * retrieving full entities (as in findByCode), it executes a lightweight query
	 * that only verifies the presence of a matching record (e.g., SELECT COUNT(*) >
	 * 0).
	 *
	 * This approach improves performance and intentionally bypasses any soft-delete
	 * filters to ensure global uniqueness of coupon codes.
	 */
	@Query(value = "SELECT COUNT(*) > 0 FROM coupons WHERE code = :code", nativeQuery = true)
	boolean existsByCodeIncludingDeleted(String code);

}