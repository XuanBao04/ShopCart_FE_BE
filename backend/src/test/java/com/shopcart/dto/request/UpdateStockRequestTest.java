package com.shopcart.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation tests for UpdateStockRequest DTO
 * 
 * Demonstrates:
 * - Using Jakarta Bean Validation for testing JSR-380
 * - Testing @NotNull and @Min annotations
 * - Verifying constraint violation messages
 */
@Slf4j
@DisplayName("UpdateStockRequest DTO Validation Tests")
class UpdateStockRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        // Initialize validator without Spring context
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static final String VALID_QUANTITY = "10";

    @Test
    @DisplayName("Should build UpdateStockRequest with valid quantity using Lombok Builder")
    void testUpdateStockRequestBuilder() {
        // Arrange & Act
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(10)
                .build();

        // Assert
        assertEquals(10, request.getQuantity());
        assertNotNull(request);
        
        log.info("UpdateStockRequest: {}", request);
    }

    @Test
    @DisplayName("Should pass validation with quantity = 1 (minimum valid)")
    void testUpdateStockRequestValidMinimum() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(1)
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Should have no violations for minimum valid quantity");
        log.info("✓ Validation passed for minimum quantity (1)");
    }

    @Test
    @DisplayName("Should pass validation with quantity = 100")
    void testUpdateStockRequestValidQuantity() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(100)
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
        log.info("✓ Validation passed for quantity: 100");
    }

    @Test
    @DisplayName("Should fail validation when quantity is null")
    void testUpdateStockRequestNullQuantity() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(null)
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Should have violations for null quantity");
        assertEquals(1, violations.size());
        
        ConstraintViolation<UpdateStockRequest> violation = violations.iterator().next();
        assertEquals("Quantity is required", violation.getMessage());
        assertEquals("quantity", violation.getPropertyPath().toString());
        
        log.info("✓ Validation failed for null quantity: {}", violation.getMessage());
    }

    @Test
    @DisplayName("Should fail validation when quantity is zero")
    void testUpdateStockRequestZeroQuantity() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(0)
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Should have violations for zero quantity");
        assertEquals(1, violations.size());
        
        ConstraintViolation<UpdateStockRequest> violation = violations.iterator().next();
        assertEquals("Quantity must be greater than 0", violation.getMessage());
        
        log.info("✓ Validation failed for zero quantity");
    }

    @Test
    @DisplayName("Should fail validation when quantity is negative")
    void testUpdateStockRequestNegativeQuantity() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(-5)
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Should have violations for negative quantity");
        assertEquals(1, violations.size());
        
        ConstraintViolation<UpdateStockRequest> violation = violations.iterator().next();
        assertEquals("Quantity must be greater than 0", violation.getMessage());
        
        log.info("✓ Validation failed for negative quantity: {}", violation.getMessage());
    }

    @Test
    @DisplayName("Should test UpdateStockRequest equality using Lombok @Data")
    void testUpdateStockRequestEquality() {
        // Arrange
        UpdateStockRequest request1 = UpdateStockRequest.builder()
                .quantity(10)
                .build();

        UpdateStockRequest request2 = UpdateStockRequest.builder()
                .quantity(10)
                .build();

        UpdateStockRequest request3 = UpdateStockRequest.builder()
                .quantity(20)
                .build();

        // Act & Assert - Lombok generates equals() and hashCode()
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
        
        log.info("✓ Equality tests passed");
    }

    @Test
    @DisplayName("Should test UpdateStockRequest with no-args constructor")
    void testUpdateStockRequestNoArgsConstructor() {
        // Act - Using Lombok @NoArgsConstructor
        UpdateStockRequest request = new UpdateStockRequest();

        // Assert
        assertNotNull(request);
        assertNull(request.getQuantity());
        
        log.info("✓ No-args constructor works correctly");
    }

    @Test
    @DisplayName("Should test UpdateStockRequest with all-args constructor")
    void testUpdateStockRequestAllArgsConstructor() {
        // Act - Using Lombok @AllArgsConstructor
        UpdateStockRequest request = new UpdateStockRequest(25);

        // Assert
        assertNotNull(request);
        assertEquals(25, request.getQuantity());
        
        log.info("✓ All-args constructor works correctly");
    }
}
