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
 * Focuses on business orchestration, exception handling, and repository
 * interactions.
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

            CouponRequest request = new CouponRequest("NEW010", "Desc", new BigDecimal("10.0"), OffsetDateTime.now().plusDays(1), true);

            when(couponRepository.existsByCodeIncludingDeleted(anyString())).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Coupon result = couponService.createCoupon(request);

            assertNotNull(result);
            verify(couponRepository).save(any(Coupon.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when code already exists")
        void shouldThrowWhenCodeExists() {

        	CouponRequest request = new CouponRequest("EXISTS", "Desc", new BigDecimal("10.0"), OffsetDateTime.now().plusDays(1), true);

            when(couponRepository.existsByCodeIncludingDeleted(anyString())).thenReturn(true);

            assertThrows(BusinessException.class, () -> couponService.createCoupon(request));
            verify(couponRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle database concurrency conflicts")
        void shouldHandleConcurrencyConflict() {

            CouponRequest request = new CouponRequest("CONCUR", "Desc", new BigDecimal("10.0"), OffsetDateTime.now().plusDays(1), true);

            when(couponRepository.existsByCodeIncludingDeleted(anyString())).thenReturn(false);
            when(couponRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

            assertThrows(BusinessException.class, () -> couponService.createCoupon(request));
        }
    }

    @Nested
    @DisplayName("Retrieval Operations")
    class GetCoupon {

        @Test
        @DisplayName("Should find coupon by technical ID")
        void shouldFindById() {

            String id = UUID.randomUUID().toString();
            Coupon coupon = Coupon.create("FIND01", "Desc", new BigDecimal("10.0"), OffsetDateTime.now().plusDays(1), true);

            when(couponRepository.findById(id)).thenReturn(Optional.of(coupon));

            assertEquals(coupon, couponService.getCouponById(id));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ID is missing")
        void shouldThrowWhenIdNotFound() {
            when(couponRepository.findById(anyString())).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> couponService.getCouponById("404"));
        }

        @Test
        @DisplayName("Should find coupon by code with normalization")
        void shouldFindByCode() {

            String code = "  test01  ";
            Coupon coupon = Coupon.create("TEST01", "Desc", new BigDecimal("10.0"), OffsetDateTime.now().plusDays(1), true);

            when(couponRepository.findByCode("TEST01")).thenReturn(Optional.of(coupon));

            assertNotNull(couponService.getCouponByCode(code));
        }
    }

    @Nested
    @DisplayName("Deletion Operations")
    class DeleteCoupon {

        @Test
        @DisplayName("Should trigger soft delete on existing coupon")
        void shouldDeleteSuccessfully() {

            String id = UUID.randomUUID().toString();
            Coupon coupon = spy(Coupon.create("DEL001", "Desc", new BigDecimal("10.0"), OffsetDateTime.now().plusDays(1), true));

            when(couponRepository.findById(id)).thenReturn(Optional.of(coupon));

            couponService.deleteCoupon(id);

            verify(coupon).delete(); // Verifies if the domain method was called
        }
    }

    @Nested
    @DisplayName("Edge Cases & Exception Coverage")
    class EdgeCases {

        @Test
        @DisplayName("getCouponByCode - Should handle null code normalization")
        void shouldHandleNullCodeInGetByCode() {

            when(couponRepository.findByCode("")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> couponService.getCouponByCode(null));

            verify(couponRepository).findByCode("");
        }

        @Test
        @DisplayName("getCouponByCode - Should execute lambda for ResourceNotFoundException")
        void shouldExecuteGetByCodeNotFoundLambda() {

            String code = "NOTFOUND";
            when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> couponService.getCouponByCode(code));

            assertTrue(exception.getMessage().contains(code));
        }

        @Test
        @DisplayName("deleteCoupon - Should execute lambda for ResourceNotFoundException")
        void shouldExecuteDeleteNotFoundLambda() {

            String id = "invalid-uuid";
            when(couponRepository.findById(id)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> couponService.deleteCoupon(id));

            assertTrue(exception.getMessage().contains(id));
        }
    }
}