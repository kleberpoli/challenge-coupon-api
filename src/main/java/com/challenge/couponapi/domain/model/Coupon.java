package com.challenge.couponapi.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.challenge.couponapi.domain.enums.Status;
import com.challenge.couponapi.exception.BusinessException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a coupon entity that holds discount information. It includes logic
 * for creating, deleting, and validating coupon details.
 * 
 * Technical Note: Hibernate 6.3+ deprecates @Where in favor of @SQLRestriction.
 * While @SoftDelete is the modern standard for boolean types, @SQLRestriction
 * is used here to support the 'Status' Enum and ensure automated filtering
 */
@Entity
@Table(name = "coupons", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@SQLDelete(sql = "UPDATE coupons SET status = 'DELETED' WHERE id = ?")
@SQLRestriction("status <> 'DELETED'")
public class Coupon {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36, nullable = false, updatable = false)
	private String id;

	@NotNull(message = "Coupon code must not be null")
	@Size(min = 6, max = 6, message = "Coupon code must be exactly 6 characters")
	@Column(nullable = false, unique = true)
	private String code;

	@NotNull(message = "Description must not be null")
	@Size(max = 500, message = "Description must be less than or equal to 500 characters")
	@Column(nullable = false)
	private String description;

	@NotNull(message = "Discount value must not be null")
	@DecimalMin(value = "0.5", message = "Discount value must be at least 0.5")
	@Column(nullable = false)
	private BigDecimal discountValue; // BigDecimal to ensure accuracy and avoid rounding issues

	@NotNull(message = "Expiration date must not be null")
	@Column(nullable = false)
	private OffsetDateTime expirationDate;

	@NotNull(message = "Status must not be null")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@Column(nullable = false)
	private boolean published;

	@Column(nullable = false)
	private boolean redeemed;

	/**
	 * Required by JPA for entity instantiation.
	 */
	protected Coupon() {
	}

	/**
	 * Private constructor to ensure coupon creation via the create method only.
	 * 
	 * @param code           the unique identifier for the coupon
	 * @param description    coupon details
	 * @param discountValue  value of the discount
	 * @param expirationDate expiration date
	 * @param published      publication status
	 */
	private Coupon(String code, String description, BigDecimal discountValue, OffsetDateTime expirationDate,
			boolean published) {
		this.code = code;
		this.description = description;
		this.discountValue = discountValue;
		this.expirationDate = expirationDate;
		this.published = published;
		this.status = Status.ACTIVE;
	}

	/**
	 * Creates a coupon with proper validation.
	 * 
	 * @param code              the unique identifier for the coupon
	 * @param description       coupon details
	 * @param discountValue     value of the discount
	 * @param expirationDate    expiration date (ISO 8601 format)
	 * @param published         publication status
	 * @return a new Coupon instance
	 * @throws BusinessException if any validation fails or date format is invalid
	 */
	public static Coupon create(String code, String description, BigDecimal discountValue, OffsetDateTime expirationDate,
			boolean published) {

		// 1. Sanitize input first (Business Rule: Remove special chars before final
		// validation)
		String sanitizedCode = sanitizeAndValidateCode(code);

		// 2. Validate expiration date
		validateExpirationDate(expirationDate);

		// 3. Validate discount (Business Rule: Minimum 0.5)
		validateDiscountValue(discountValue);

		return new Coupon(sanitizedCode, description, discountValue, expirationDate, published);
	}

	/**
	 * Updates the status to DELETED (soft delete).
	 * 
	 * @throws BusinessException if already deleted
	 */
	public void delete() {
		// TODO check if redeemed coupons can be deleted
		if (this.status == Status.DELETED) {
			throw new BusinessException("Coupon already deleted");
		}

		this.status = Status.DELETED;
	}

	/**
	 * Performs a basic cleanup of the code by removing non-alphanumeric characters
	 * and converting to uppercase without enforcing business length rules.
	 * 
	 * @param code The raw input string to be cleaned.
	 * @return A normalized uppercase alphanumeric string, or an empty string if
	 *         input is null.
	 */
	public static String rawCleanup(String code) {
	    if (code == null) return "";
	    return code.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
	}

	/**
	 * Sanitizes the input code by removing special characters, converting to
	 * uppercase, and strictly validating that the final result contains exactly 6
	 * characters.
	 * 
	 * @param code The raw coupon code to validate and sanitize.
	 * @return A sanitized 6-character alphanumeric string.
	 * @throws BusinessException If the code is null or does not result in exactly 6
	 *                           characters.
	 */
	public static String sanitizeAndValidateCode(String code) {
	    if (code == null) throw new BusinessException("Coupon code cannot be null");

	    // 1. Remove special characters
	    String cleaned = code.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

	    // 2. Ensures the correct size as required (truncates if larger than 6)
	    if (cleaned.length() > 6) {
	        cleaned = cleaned.substring(0, 6);
	    }

	    // 3. Validates the minimum size (If after cleaning/trimming there are not 6 characters, returns an error)
	    if (cleaned.length() != 6) {
	        throw new BusinessException("Coupon code must result in exactly 6 alphanumeric characters");
	    }

	    return cleaned;
	}

	/**
	 * Validates that the expiration date is in the future.
	 *
	 * @param expirationDate the date to validate
	 * @throws BusinessException if the date is null or in the past
	 */
	private static void validateExpirationDate(OffsetDateTime expirationDate) {
		if (expirationDate == null) {
			throw new BusinessException("Expiration date is required");
		}

		// Java's isBefore() method already handles the offset difference
		// We compare it to the UTC instant (now) to neutralize any time zone variation
		if (expirationDate.isBefore(OffsetDateTime.now(java.time.ZoneOffset.UTC))) {
			throw new BusinessException("Expiration date cannot be in the past");
		}
	}

	/**
	 * Validates that the discount value meets the minimum business requirement.
	 * 
	 * @param discountValue The numeric value of the discount to be validated.
	 * @throws BusinessException If the value is null or less than the required
	 *                           minimum of 0.5.
	 */
	private static void validateDiscountValue(BigDecimal discountValue) {
		BigDecimal minDiscount = new BigDecimal("0.5");
		if (discountValue == null || discountValue.compareTo(minDiscount) < 0) {
			throw new BusinessException("Discount value must be at least 0.5");
		}
	}

	// Getters for the coupon attributes

	public String getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getDiscountValue() {
		return discountValue;
	}

	public OffsetDateTime getExpirationDate() {
		return expirationDate;
	}

	public Status getStatus() {
		return status;
	}

	public boolean isPublished() {
		return published;
	}

	public boolean isRedeemed() {
		return redeemed;
	}
}