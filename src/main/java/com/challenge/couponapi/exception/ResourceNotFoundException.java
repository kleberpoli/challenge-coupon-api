package com.challenge.couponapi.exception;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Custom exception used to indicate that a specific resource could not be found
	 * in the system. Typically results in a 404 Not Found HTTP response.
	 *
	 * @param message A descriptive message containing the identifier or criteria of
	 *                the missing resource.
	 */
	public ResourceNotFoundException(String message) {
		super(message);
	}
}