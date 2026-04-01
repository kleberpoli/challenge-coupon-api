package com.challenge.couponapi.exception;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Custom unchecked exception used to signal business rule violations.
	 * 
	 * @param message A descriptive error message explaining the specific business
	 *                rule that was violated.
	 */
	public BusinessException(String message) {
		super(message);
	}
}