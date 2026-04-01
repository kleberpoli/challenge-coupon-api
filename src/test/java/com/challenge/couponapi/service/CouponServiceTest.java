package com.challenge.couponapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.challenge.couponapi.domain.model.Coupon;
import com.challenge.couponapi.dto.CouponRequest;
import com.challenge.couponapi.exception.BusinessException;
import com.challenge.couponapi.exception.ResourceNotFoundException;
import com.challenge.couponapi.repository.CouponRepository;

/**
 * Unit tests for CouponService.
 *
 * These tests focus on:
 * - Business flow orchestration (service layer responsibilities)
 * - Validation rules and exception scenarios
 * - Correct interaction with the repository layer
 *
 * The goal is to ensure the service behaves correctly independently
 * of infrastructure concerns (e.g., database).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Coupon Service Unit Tests")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Nested
    @DisplayName("Creation Orchestration")
    class CreateCoupon {

        @Test
        @DisplayName("Should successfully orchestrate coupon creation")
        void shouldCreateCoupon() {

            // Arrange: create a fully valid request to simulate a real creation scenario
            CouponRequest request = new CouponRequest("NEW010", "Desc", new BigDecimal("10.0"), 
                    OffsetDateTime.now().plusDays(1), true);

            // Mock: simulate that no coupon exists with the same code
            when(couponRepository.existsByCodeIncludingDeleted(anyString())).thenReturn(false);

            // Mock: simulate repository "save" behavior by returning the same entity received
            // This mimics typical persistence frameworks that return the saved entity
            when(couponRepository.save(any(Coupon.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act: execute the service method
            Coupon result = couponService.createCoupon(request);

            // Assert: ensure a result is returned and persistence was triggered
            assertNotNull(result);

            // Verify: confirm the service delegated the save operation to the repository
            verify(couponRepository).save(any(Coupon.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when coupon code already exists")
        void shouldThrowWhenCouponCodeExists() {

            // Arrange: request with a code that is already present in the system
            CouponRequest request = new CouponRequest("EXISTS", "Desc", new BigDecimal("10.0"),
                    OffsetDateTime.now().plusDays(1), true);

            // Mock: simulate that the code already exists (including soft-deleted records)
            when(couponRepository.existsByCodeIncludingDeleted(anyString())).thenReturn(true);

            // Act + Assert: service must reject creation with a business exception
            assertThrows(BusinessException.class, () -> couponService.createCoupon(request));

            // Verify: ensure no persistence attempt was made due to validation failure
            verify(couponRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle database concurrency conflicts")
        void shouldHandleConcurrencyConflict() {

            // Arrange: valid request with no prior code conflict
            CouponRequest request = new CouponRequest("CONCUR", "Desc", new BigDecimal("10.0"),
                    OffsetDateTime.now().plusDays(1), true);

			// Mock: simulate repository response indicating that no coupon exists with the given code
			// (including soft-deleted records), allowing the creation flow to proceed
            when(couponRepository.existsByCodeIncludingDeleted(anyString())).thenReturn(false);

            // Mock: simulate a database-level constraint violation (e.g., race condition)
            when(couponRepository.save(any()))
                .thenThrow(DataIntegrityViolationException.class);

            // Act + Assert: service must translate low-level exception into BusinessException
            assertThrows(BusinessException.class, () -> couponService.createCoupon(request));
        }
    }

    @Nested
    @DisplayName("Retrieval Operations")
    class GetCoupon {

        @Test
        @DisplayName("Should find coupon by technical ID")
        void shouldFindById() {

            // Arrange: create a valid coupon and mock repository response
            String id = UUID.randomUUID().toString();
            Coupon coupon = Coupon.create("FIND01", "Desc", new BigDecimal("10.0"),
                    OffsetDateTime.now().plusDays(1), true);

            // Mock: simulate repository returning the coupon for the given ID
            when(couponRepository.findById(id)).thenReturn(Optional.of(coupon));

            // Act + Assert: service should return the same coupon
            assertEquals(coupon, couponService.getCouponById(id));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ID is missing")
        void shouldThrowWhenCouponIdNotFound() {

            // Mock: simulate absence of coupon in repository
            when(couponRepository.findById(anyString())).thenReturn(Optional.empty());

            // Act + Assert: service must throw not found exception
            assertThrows(ResourceNotFoundException.class,
                    () -> couponService.getCouponById("404"));
        }

        @Test
        @DisplayName("Should find coupon by code with normalization")
        void shouldFindByCode() {

            // Arrange: input code with extra spaces and lowercase letters
            String code = "  test01  ";

            // Repository stores normalized (trimmed + uppercased) values
            Coupon coupon = Coupon.create("TEST01", "Desc", new BigDecimal("10.0"),
                    OffsetDateTime.now().plusDays(1), true);

            // Mock: expect normalized value to be used in query
            when(couponRepository.findByCode("TEST01")).thenReturn(Optional.of(coupon));

            // Act + Assert: service should normalize input and successfully retrieve the coupon
            assertNotNull(couponService.getCouponByCode(code));
        }
    }

    @Nested
    @DisplayName("Deletion Operations")
    class DeleteCoupon {

        @Test
        @DisplayName("Should trigger soft delete on existing coupon")
        void shouldDeleteSuccessfully() {

            // Arrange: create a spy to observe domain behavior (not just repository interaction)
            String id = UUID.randomUUID().toString();
            Coupon coupon = spy(Coupon.create("DEL001", "Desc", new BigDecimal("10.0"),
                    OffsetDateTime.now().plusDays(1), true));

            // Mock: repository returns an existing coupon for the given ID
            when(couponRepository.findById(id)).thenReturn(Optional.of(coupon));

            // Act: execute deletion
            couponService.deleteCoupon(id);

            // Verify: ensure domain logic (soft delete) was triggered
            // This confirms behavior, not just data access
            verify(coupon).delete();
        }
    }

    @Nested
    @DisplayName("Edge Cases & Exception Coverage")
    class EdgeCases {

        @Test
        @DisplayName("getCouponByCode - Should handle null code normalization")
        void shouldHandleNullCodeInGetCouponByCode() {

        	// Mock: repository returns empty when queried with normalized empty code (null input case)
            when(couponRepository.findByCode("")).thenReturn(Optional.empty());

            // Act + Assert: service should still follow normal flow and throw not found
            assertThrows(ResourceNotFoundException.class,
                    () -> couponService.getCouponByCode(null));

            // Verify: confirm normalization behavior (null -> "")
            verify(couponRepository).findByCode("");
        }

        @Test
        @DisplayName("getCouponByCode - Should execute lambda for ResourceNotFoundException")
        void shouldExecuteGetByCodeNotFoundLambda() {

            // Arrange: code that does not exist
            String code = "NOTFOUND";
            when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());

            // Act: capture thrown exception
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> couponService.getCouponByCode(code));

            // Assert: verify that exception message contains the searched code
            // This ensures the lambda responsible for building the exception was executed
            assertTrue(exception.getMessage().contains(code));
        }

        @Test
        @DisplayName("deleteCoupon - Should execute lambda for ResourceNotFoundException")
        void shouldExecuteDeleteNotFoundLambda() {

            // Arrange: invalid ID that does not exist in repository
            String id = "invalid-uuid";
            when(couponRepository.findById(id)).thenReturn(Optional.empty());

            // Act: capture thrown exception
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> couponService.deleteCoupon(id));

            // Assert: verify that exception message contains the requested ID
            // Ensures proper exception construction and traceability
            assertTrue(exception.getMessage().contains(id));
        }
    }
}