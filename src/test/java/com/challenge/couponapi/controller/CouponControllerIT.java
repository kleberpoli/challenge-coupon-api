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
 * Integration tests for the Coupon API endpoints.
 * 
 * Utilizes @WebMvcTest to focus solely on the web layer, including JSON
 * serialization, routing, and global exception handling.
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
     * Test Case: Successful Coupon Creation
     * Verifies if the controller returns 201 Created and the correct JSON body.
     */
    @Test
    @DisplayName("POST /coupons - Should return 201 when request is valid")
    void shouldCreateCouponSuccessfully() throws Exception {

    	// Arrange
        OffsetDateTime futureDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        CouponRequest request = new CouponRequest("PROM15@", "Desc", new BigDecimal("10.50"), futureDate, true);
        
        // The mock reflects the sanitized domain object (PRM15@ -> PROM15)
        Coupon mockCoupon = Coupon.create("PROM15", "Desc", new BigDecimal("10.50"), futureDate, true);

        when(couponService.createCoupon(any(CouponRequest.class))).thenReturn(mockCoupon);

        // Act & Assert
        mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PROM15"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(couponService, times(1)).createCoupon(any());
    }

    @Test
    @DisplayName("GET /coupons/{id} - Should return 200 when coupon exists")
    void shouldReturn200WhenCouponIdExists() throws Exception {

    	String id = UUID.randomUUID().toString();
        Coupon mockCoupon = Coupon.create("GOLD25", "Desc", new BigDecimal("25.0"), OffsetDateTime.now().plusDays(1), true);

        when(couponService.getCouponById(id)).thenReturn(mockCoupon);

        mockMvc.perform(get("/coupons/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GOLD25"));
    }

    @Test
    @DisplayName("GET /coupons/code/{code} - Should return 200 when code exists")
    void shouldReturn200WhenCouponCodeExists() throws Exception {

    	String code = "SAVE10";
        Coupon mockCoupon = Coupon.create(code, "Desc", new BigDecimal("10.0"), OffsetDateTime.now().plusDays(1), true);

        when(couponService.getCouponByCode(code)).thenReturn(mockCoupon);

        mockMvc.perform(get("/coupons/code/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE10"));
    }

    /**
     * Test Case: Resource Not Found
     * Verifies mapping of ResourceNotFoundException to HTTP 404 via ControllerAdvice.
     */
    @Test
    @DisplayName("GET /coupons/{id} - Should return 404 when ID does not exist")
    void shouldReturn404WhenCouponIdNotFound() throws Exception {

        String id = UUID.randomUUID().toString();

        when(couponService.getCouponById(id)).thenThrow(new ResourceNotFoundException("Coupon not found"));

        mockMvc.perform(get("/coupons/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    /**
     * Test Case: Resource Not Found
     * Verifies mapping of ResourceNotFoundException to HTTP 404 via ControllerAdvice.
     */
    @Test
    @DisplayName("GET /coupons/code/{code} - Should return 404 when code does not exist")
    void shouldReturn404WhenCouponCodeNotFound() throws Exception {

        String code = "TEST01";

        when(couponService.getCouponByCode(code)).thenThrow(new ResourceNotFoundException("Coupon not found"));

        mockMvc.perform(get("/coupons/code/{code}", code))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    /**
     * Test Case: Business Rule Violation
     * Verifies mapping of BusinessException to HTTP 422 Unprocessable Entity.
     */
    @Test
    @DisplayName("POST /coupons - Should return 422 for business rule violations")
    void shouldReturn422ForBusinessErrors() throws Exception {

    	CouponRequest validRequest = new CouponRequest("DUP001", "Desc", new BigDecimal("10.00"), OffsetDateTime.now().plusDays(1), true);

    	when(couponService.createCoupon(any())).thenThrow(new BusinessException("Coupon code already exists"));

        mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Coupon code already exists"));
    }

    /**
     * Test Case: Soft Delete
     * Verifies if the delete endpoint responds with 204 No Content.
     */
    @Test
    @DisplayName("DELETE /coupons/{id} - Should return 204 No Content")
    void shouldReturn204OnDelete() throws Exception {

        String id = UUID.randomUUID().toString();

        // Void method mocking
        doNothing().when(couponService).deleteCoupon(id);

        mockMvc.perform(delete("/coupons/{id}", id))
                .andExpect(status().isNoContent());

        verify(couponService, times(1)).deleteCoupon(id);
    }

    @Test
    @DisplayName("POST /coupons - Should return 400 when Bean Validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {

    	// Discount of 0.10 fails validation @Min(0.5) of the DTO
        CouponRequest invalidRequest = new CouponRequest("FAIL", "Short", new BigDecimal("0.10"), OffsetDateTime.now().plusDays(1), true);

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

        String id = UUID.randomUUID().toString();
        when(couponService.getCouponById(id)).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/coupons/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"));
    }
}