package com.challenge.couponapi.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.challenge.couponapi.exception.BusinessException;

@DisplayName("Status Enum Unit Tests")
class StatusTest {

	@ParameterizedTest
    @CsvSource({
        "ACTIVE, ACTIVE",
        "active, ACTIVE",
        "INACTIVE, INACTIVE",
        "inactive, INACTIVE",
        "DELETED , DELETED",
        "deleted , DELETED"
    })
    @DisplayName("Should convert valid strings to Status enum")
    void shouldConvertValidStrings(String input, Status expected) {

        // Act: convert input string to enum
        Status result = Status.fromString(input);

        // Assert: result matches expected enum value (case-insensitive, trimmed)
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should throw BusinessException for invalid status string")
    void shouldThrowExceptionForInvalidString() {
    	// Act + Assert: invalid value must trigger exception
        assertThrows(BusinessException.class, () -> Status.fromString("INVALID_STATUS"));
    }

    @Test
    @DisplayName("Should throw BusinessException for null input")
    void shouldThrowExceptionForNull() {
    	// Act + Assert: null input must be rejected
        assertThrows(BusinessException.class, () -> Status.fromString(null));
    }
}