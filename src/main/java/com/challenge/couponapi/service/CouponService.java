package com.challenge.couponapi.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.challenge.couponapi.domain.model.Coupon;
import com.challenge.couponapi.dto.CouponRequest;
import com.challenge.couponapi.exception.BusinessException;
import com.challenge.couponapi.exception.ResourceNotFoundException;
import com.challenge.couponapi.repository.CouponRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service orchestrator for Coupon operations.
 * 
 * Technical Note: Following Clean Architecture, business rules reside within
 * the Domain Model (Coupon entity). This service acts as a mediator between the
 * external API and the persistence layer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

	private final CouponRepository couponRepository;

	/**
	 * Retrieves a coupon from the database using its technical UUID.
	 *
	 * @param id The unique internal identifier of the coupon.
	 * @return The found Coupon entity.
	 * @throws ResourceNotFoundException If no coupon matches the provided ID.
	 */
	@Transactional(readOnly = true)
	public Coupon getCouponById(String id) {
		return couponRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No coupon found with ID: " + id));
	}

	/**
	 * Retrieves an active coupon using its business code. The input is normalized
	 * to uppercase to ensure a consistent lookup.
	 *
	 * @param code The alphanumeric code assigned to the coupon.
	 * @return The found Coupon entity.
	 * @throws ResourceNotFoundException If no active coupon matches the provided
	 *                                   code.
	 */
	public Coupon getCouponByCode(String code) {
	    String normalizedCode = (code != null) ? code.toUpperCase().trim() : "";
	    return couponRepository.findByCode(normalizedCode)
	            .orElseThrow(() -> new ResourceNotFoundException("No coupon found with code: " + code));
	}

	/**
	 * Orchestrates the creation of a new coupon by validating uniqueness and
	 * delegating business rules to the domain model.
	 *
	 * @param request DTO containing the data for the new coupon.
	 * @return The persisted Coupon entity.
	 * @throws BusinessException If the code is already registered or a concurrency
	 *                           conflict occurs.
	 */
	@Transactional
	public Coupon createCoupon(CouponRequest request) {
		log.info("Starting coupon creation process for code: {}", request.code());

		// 1. Normalizes the code for the search
	    String cleanCode = Coupon.rawCleanup(request.code());

		// 2. Initial Check: Checks if the code exists in ANY status (including DELETED)
		if (couponRepository.existsByCodeIncludingDeleted(cleanCode)) {
			throw new BusinessException("Cannot create coupon: Code '" + cleanCode + "' is already registered");
		}

		try {
			// 3. Domain Logic: Create entity using its rich model validation
			Coupon coupon = Coupon.create(
					request.code(),
					request.description(),
					request.discountValue(),
					request.expirationDate(),
					request.published());

			// 4. Persistence: Handle potential race conditions at the database level
			return couponRepository.save(coupon);

		} catch (DataIntegrityViolationException e) {
			log.warn("Concurrency conflict: Coupon code '{}' was likely registered by another request", request.code());
			throw new BusinessException("Coupon code already exists (concurrent conflict)");
		}
	}

	/**
	 * Performs a soft delete on a coupon identified by its ID. Transitions the
	 * coupon status to 'DELETED' without removing the record from the database.
	 *
	 * @param id The unique internal identifier of the coupon to be deleted.
	 * @throws ResourceNotFoundException If the coupon does not exist.
	 */
	@Transactional
	public void deleteCoupon(String id) {
		log.info("Starting soft delete for coupon ID: {}", id);

	    Coupon coupon = couponRepository.findById(id)
	    		.orElseThrow(() -> new ResourceNotFoundException("Coupon not found with ID: " + id));

	    // Hibernate will automatically handle the persistence of the 'DELETED' status
	    coupon.delete();
	}
}