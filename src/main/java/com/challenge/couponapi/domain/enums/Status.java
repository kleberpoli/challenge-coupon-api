package com.challenge.couponapi.domain.enums;

import java.util.Arrays;

import com.challenge.couponapi.exception.BusinessException;

/**
 * Represents the possible states of a Coupon.
 */
public enum Status {

	ACTIVE, INACTIVE, DELETED;

	/**
	 * Resolves the Status enum from a string value using a fail-fast stream
	 * approach.
	 * 
	 * @param value the string to convert
	 * @return the corresponding Status
	 * @throws BusinessException if the status is unknown
	 */
	public static Status fromString(String value) {

		if (value == null) {
			throw new BusinessException("Status value cannot be null");
		}

		return Arrays.stream(Status.values()).filter(s -> s.name().equalsIgnoreCase(value.trim())) // Filter by name
				.findFirst() // Get match
				.orElseThrow(() -> new BusinessException("Unknown status: " + value));
	}
}