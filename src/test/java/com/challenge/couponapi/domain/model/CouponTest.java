package com.challenge.couponapi.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.challenge.couponapi.domain.enums.Status;
import com.challenge.couponapi.exception.BusinessException;

/**
 * Unit tests for Coupon domain entity.
 *
 * Focus: validate core business rules (sanitization, constraints, lifecycle
 * behavior) independent of infrastructure.
 */
@DisplayName("Coupon Domain Unit Tests")
class CouponTest {

	private final String VALID_CODE = "VAL100";
    private final String VALID_DESC = "Spring Sale 2026";
    private final BigDecimal VALID_DISCOUNT = new BigDecimal("10.00");
    private final OffsetDateTime FUTURE_DATE = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

    @Nested
    @DisplayName("Code Sanitization & Truncation")
    class SanitizationTests {

    	@ParameterizedTest
        @CsvSource({
            "PROM15, PROM15",          // Already clean
            "P@R@O@M15, PROM15",       // Adjusted: Now has 6 alphanumeric chars
            "prom15, PROM15",          // Case normalization
            "P-R-O-M-1-5, PROM15",     // Dash removal
            "PROMO2026, PROMO2",       // Truncate if > 6
            "!!SAVE50!!, SAVE50"       // Exact 6 result
        })
        @DisplayName("Should correctly sanitize and truncate code")
    	void shouldSanitizeCode(String input, String expected) {

    		// Act: create coupon with raw input
            Coupon coupon = Coupon.create(input, VALID_DESC, VALID_DISCOUNT, FUTURE_DATE, true);

            // Assert: code is sanitized, normalized and truncated
            assertEquals(expected, coupon.getCode());
        }

        @ParameterizedTest
        @ValueSource(strings = {"PR@M15", "ABC", "12345", " "})
        @DisplayName("Should throw exception when sanitized result is shorter than 6 characters")
        void shouldThrowWhenShort(String invalidInput) {
            // Act + Assert: invalid sanitized code must trigger business validation error
        	assertThrows(BusinessException.class, () -> 
                Coupon.create(invalidInput, VALID_DESC, VALID_DISCOUNT, FUTURE_DATE, true));
        }

        @Test
        @DisplayName("Should return empty string for rawCleanup when code is null")
        void rawCleanupNullHanding() {

            // Act: cleanup null input
            String result = Coupon.rawCleanup(null);

            // Assert: null input becomes empty string
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Business Validations (Constraints)")
    class ConstraintTests {

        @Test
        @DisplayName("Should fail when coupon code is null")
        void shouldFailNullCode() {
        	// Act + Assert: null code must be rejected
            assertThrows(BusinessException.class, () -> 
                Coupon.create(null, VALID_DESC, VALID_DISCOUNT, FUTURE_DATE, true));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should fail when expiration date is null")
        void shouldFailNullDate(OffsetDateTime nullDate) {

        	// Act: attempt to create with null expiration date
            BusinessException ex = assertThrows(BusinessException.class, () -> 
                Coupon.create(VALID_CODE, VALID_DESC, VALID_DISCOUNT, nullDate, true));

            // Assert: correct validation message is returned
            assertEquals("Expiration date is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should fail when expiration date is in the past")
        void shouldFailPastDate() {
        	
            // Arrange: date in the past
            OffsetDateTime pastDate = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);

            // Act + Assert: past date must be rejected
            assertThrows(BusinessException.class, () ->
                Coupon.create(VALID_CODE, VALID_DESC, VALID_DISCOUNT, pastDate, true));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.49", "0.0", "-5.0"})
        @DisplayName("Should fail when discount is below 0.5")
        void shouldFailLowDiscount(String val) {
        	// Act + Assert: discount below minimum threshold must fail
            assertThrows(BusinessException.class, () -> 
                Coupon.create(VALID_CODE, VALID_DESC, new BigDecimal(val), FUTURE_DATE, true));
        }

        @Test
        @DisplayName("Should fail when discount is null")
        void shouldFailNullDiscount() {
        	// Act + Assert: null discount must be rejected
            assertThrows(BusinessException.class, () -> 
                Coupon.create(VALID_CODE, VALID_DESC, null, FUTURE_DATE, true));
        }
    }

    @Nested
    @DisplayName("Lifecycle & Soft Delete")
    class LifecycleTests {

        @Test
        @DisplayName("Should instantiate with correct initial state")
        void shouldHaveCorrectInitialState() {

        	// Act: create new coupon instance
        	Coupon coupon = Coupon.create(VALID_CODE, VALID_DESC, VALID_DISCOUNT, FUTURE_DATE, false);

        	// Assert: verify default state values
            assertAll(
                () -> assertEquals(Status.ACTIVE, coupon.getStatus()),
                () -> assertFalse(coupon.isPublished()),
                () -> assertFalse(coupon.isRedeemed()),
                () -> assertNull(coupon.getId(), "ID should be null before persistence")
            );
        }

        @Test
        @DisplayName("Should perform soft delete by updating status")
        void shouldSoftDelete() {

            // Arrange: active coupon
            Coupon coupon = Coupon.create(VALID_CODE, VALID_DESC, VALID_DISCOUNT, FUTURE_DATE, true);

            // Act: execute soft delete
            coupon.delete();

            // Assert: status must change to DELETED
            assertEquals(Status.DELETED, coupon.getStatus());
        }

        @Test
        @DisplayName("Should block deletion if already deleted")
        void shouldFailDoubleDelete() {

            // Arrange: already deleted coupon
            Coupon coupon = Coupon.create(VALID_CODE, VALID_DESC, VALID_DISCOUNT, FUTURE_DATE, true);
            coupon.delete();

            // Act: attempt second deletion
            BusinessException ex = assertThrows(BusinessException.class, coupon::delete);

            // Assert: correct error message is returned
            assertEquals("Coupon already deleted", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Accessors & Technical Coverage")
    class TechnicalCoverageTests {

        @Test
        @DisplayName("Should exercise all getters for coverage")
        void shouldExerciseGetters() {

        	// Arrange: create coupon
            Coupon coupon = Coupon.create(VALID_CODE, VALID_DESC, VALID_DISCOUNT, FUTURE_DATE, true);

            // Assert: verify all getters return expected values
            assertAll(
                () -> assertEquals(VALID_DESC, coupon.getDescription()),
                () -> assertEquals(VALID_DISCOUNT, coupon.getDiscountValue()),
                () -> assertEquals(FUTURE_DATE, coupon.getExpirationDate()),
                () -> assertEquals(Status.ACTIVE, coupon.getStatus()),
                () -> assertTrue(coupon.isPublished()),
                () -> assertFalse(coupon.isRedeemed())
            );
        }

        @Test
        @DisplayName("Should exercise protected no-args constructor for JPA coverage")
        void shouldExerciseNoArgsConstructor() {

        	// Arrange: helper subclass to access protected constructor
            class JpaCoupon extends Coupon {
                public JpaCoupon() { super(); }
            }

            // Act + Assert: ensure constructor can be invoked
            assertNotNull(new JpaCoupon());
        }

        @Test
        @DisplayName("Should correctly cleanup raw code without business validation")
        void shouldExerciseRawCleanup() {
        	// Act + Assert: verify raw cleanup behavior without validation rules
            assertAll(
                () -> assertEquals("ABC123", Coupon.rawCleanup("abc-123!")),
                () -> assertEquals("", Coupon.rawCleanup(null)),
                () -> assertEquals("XYZ999", Coupon.rawCleanup("xyz999"))
            );
        }
    }
}