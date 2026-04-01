package com.challenge.couponapi.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler to intercept and format all API errors.
 * 
 * Technical Note: This centralizes error logic, allowing Controllers to remain
 * "thin".
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Intercepts and handles business logic violations, such as duplicate codes or
	 * rule breaches.
	 * 
	 * @param ex The BusinessException thrown by the service layer.
	 * @return A ResponseEntity containing an ErrorResponse with 422 Unprocessable
	 *         Entity status.
	 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .title("Business Rule Violation")
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .timestamp(LocalDateTime.now())
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

	/**
	 * Intercepts and handles cases where a requested resource does not exist in the
	 * database.
	 * 
	 * @param ex The ResourceNotFoundException containing the missing identifier
	 *           details.
	 * @return A ResponseEntity containing an ErrorResponse with 404 Not Found
	 *         status.
	 */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

	/**
	 * Intercepts and processes Bean Validation failures triggered by {@code @Valid}
	 * annotations.
	 * 
	 * @param ex The exception containing the binding result and specific field
	 *           errors.
	 * @return A ResponseEntity containing an ErrorResponse with 400 Bad Request
	 *         status and a list of field-specific errors.
	 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
                .title("Invalid Input Data")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .detail("One or more fields failed validation.")
                .errors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

	/**
	 * Global fallback handler for any unhandled or unexpected exceptions. Prevents
	 * internal implementation details from leaking to the client.
	 * 
	 * @param ex The unexpected Exception encountered during request processing.
	 * @return A ResponseEntity containing a generic ErrorResponse with 500 Internal
	 *         Server Error status.
	 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .detail("An unexpected error occurred. Please contact support.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}