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
        assertEquals(expected, Status.fromString(input));
    }

    @Test
    @DisplayName("Should throw BusinessException for invalid status string")
    void shouldThrowExceptionForInvalidString() {
        assertThrows(BusinessException.class, () -> Status.fromString("INVALID_STATUS"));
    }

    @Test
    @DisplayName("Should throw BusinessException for null input")
    void shouldThrowExceptionForNull() {
        assertThrows(BusinessException.class, () -> Status.fromString(null));
    }
}