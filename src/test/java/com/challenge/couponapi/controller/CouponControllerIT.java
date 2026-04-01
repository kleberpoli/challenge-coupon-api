package com.challenge.couponapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.challenge.couponapi.domain.model.Coupon;
import com.challenge.couponapi.dto.CouponRequest;
import com.challenge.couponapi.exception.BusinessException;
import com.challenge.couponapi.exception.ResourceNotFoundException;
import com.challenge.couponapi.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for CouponController.
 *
 * Focus: validate HTTP layer behavior (routing, JSON, status codes, and
 * exception handling) with mocked service layer.
 */
@WebMvcTest(CouponController.class)
@ActiveProfiles("test")
@DisplayName("Coupon Controller Integration Tests")
class CouponControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;

    /**
     * Scenario: valid request
     * Expect: 201 Created + correct response body
     */
    @Test
    @DisplayName("POST /coupons - Should return 201 when request is valid")
    void shouldCreateCouponSuccessfully() throws Exception {

    	// Arrange: build valid request and expected service response
        OffsetDateTime futureDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        CouponRequest request = new CouponRequest("PROM15@", "Desc", new BigDecimal("10.50"), futureDate, true);
        
        // Arrange: domain object reflects normalized/sanitized data
        Coupon mockCoupon = Coupon.create("PROM15", "Desc", new BigDecimal("10.50"), futureDate, true);

        // Mock: service returns created coupon
        when(couponService.createCoupon(any(CouponRequest.class))).thenReturn(mockCoupon);

        // Act + Assert: perform request and validate HTTP response
        mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PROM15"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify: ensure service was called once
        verify(couponService, times(1)).createCoupon(any());
    }

    @Test
    @DisplayName("GET /coupons/{id} - Should return 200 when coupon exists")
    void shouldReturn200WhenCouponIdExists() throws Exception {

    	// Arrange: existing coupon
    	String id = UUID.randomUUID().toString();
        Coupon mockCoupon = Coupon.create("GOLD25", "Desc", new BigDecimal("25.0"), OffsetDateTime.now().plusDays(1), true);

        // Mock: service returns coupon by ID
        when(couponService.getCouponById(id)).thenReturn(mockCoupon);

        // Act + Assert: request returns 200 with expected data
        mockMvc.perform(get("/coupons/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GOLD25"));
    }

    @Test
    @DisplayName("GET /coupons/code/{code} - Should return 200 when coupon code exists")
    void shouldReturn200WhenCouponCodeExists() throws Exception {

    	// Arrange: existing coupon by code
		String code = "SAVE10";
		Coupon mockCoupon = Coupon.create(code, "Desc", new BigDecimal("10.0"), OffsetDateTime.now().plusDays(1), true);

		// Mock: service returns coupon by code
        when(couponService.getCouponByCode(code)).thenReturn(mockCoupon);

        // Act + Assert: request returns 200 with expected data
        mockMvc.perform(get("/coupons/code/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE10"));
    }

    /**
     * Scenario: ID not found
     * Expect: 404 mapped by global exception handler
     */
    @Test
    @DisplayName("GET /coupons/{id} - Should return 404 when ID does not exist")
    void shouldReturn404WhenCouponIdNotFound() throws Exception {

    	// Arrange: non-existing ID
        String id = UUID.randomUUID().toString();

        // Mock: service throws not found exception
		when(couponService.getCouponById(id)).thenThrow(new ResourceNotFoundException("Coupon not found"));

		// Act + Assert: request returns 404 with standardized error body
        mockMvc.perform(get("/coupons/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    /**
     * Scenario: coupon code not found
     * Expect: 404 mapped by global exception handler
     */
    @Test
    @DisplayName("GET /coupons/code/{code} - Should return 404 when coupon code does not exist")
    void shouldReturn404WhenCouponCodeNotFound() throws Exception {

    	// Arrange: non-existing code
        String code = "TEST01";

        // Mock: service throws not found exception
        when(couponService.getCouponByCode(code)).thenThrow(new ResourceNotFoundException("Coupon not found"));

        // Act + Assert: request returns 404
        mockMvc.perform(get("/coupons/code/{code}", code))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    /**
     * Scenario: business rule violation
     * Expect: 422 Unprocessable Entity
     */
    @Test
    @DisplayName("POST /coupons - Should return 422 for business rule violations")
    void shouldReturn422ForBusinessErrors() throws Exception {

    	// Arrange: valid request structure
    	CouponRequest validRequest = new CouponRequest("DUP001", "Desc", new BigDecimal("10.00"), OffsetDateTime.now().plusDays(1), true);

    	// Mock: service throws business exception
    	when(couponService.createCoupon(any())).thenThrow(new BusinessException("Coupon code already exists"));

    	// Act + Assert: request returns 422 with error detail
        mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Coupon code already exists"));
    }

    /**
     * Scenario: successful deletion
     * Expect: 204 No Content
     */
    @Test
    @DisplayName("DELETE /coupons/{id} - Should return 204 No Content")
    void shouldReturn204OnDelete() throws Exception {

    	// Arrange: existing ID
        String id = UUID.randomUUID().toString();

        // Mock: simulate void service method (no exception)
        doNothing().when(couponService).deleteCoupon(id);

        // Act + Assert: request returns 204
        mockMvc.perform(delete("/coupons/{id}", id))
                .andExpect(status().isNoContent());

        // Verify: ensure delete was invoked once
        verify(couponService, times(1)).deleteCoupon(id);
    }

    @Test
    @DisplayName("POST /coupons - Should return 400 when Bean Validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {

        // Arrange: invalid request (violates DTO validation rules)
        // Discount below minimum allowed value
		CouponRequest invalidRequest = new CouponRequest("FAIL01", "Short", new BigDecimal("0.10"),
				OffsetDateTime.now().plusDays(1), true);

		// Act + Assert: request returns 400 with validation error details
        mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Input Data"))
                .andExpect(jsonPath("$.errors[0].field").value("discountValue"));
    }

    @Test
    @DisplayName("GET /coupons/{id} - Should return 500 for unexpected errors")
    void shouldReturn500OnGenericException() throws Exception {

    	// Arrange: any ID
        String id = UUID.randomUUID().toString();

        // Mock: simulate unexpected runtime failure
		when(couponService.getCouponById(id)).thenThrow(new RuntimeException("Database connection failed"));

		// Act + Assert: request returns 500 with generic error response
        mockMvc.perform(get("/coupons/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"));
    }
}