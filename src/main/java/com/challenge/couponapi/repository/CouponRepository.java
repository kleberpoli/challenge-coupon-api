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
	 * @param code The unique alphanumeric code assigned to the coupon.
	 * @return An Optional containing the found Coupon, or Optional.empty() if no
	 *         match exists.
	 */
	Optional<Coupon> findByCode(String code);

	/**
	 * Checks for the existence of a coupon code across the entire database,
	 * specifically including records marked as soft-deleted.
	 * 
	 * @param code The alphanumeric code to check for global uniqueness.
	 * @return True if the code exists in any state (active or deleted), false
	 *         otherwise.
	 */
	@Query(value = "SELECT COUNT(*) > 0 FROM coupons WHERE code = :code", nativeQuery = true)
	boolean existsByCodeIncludingDeleted(String code);

}