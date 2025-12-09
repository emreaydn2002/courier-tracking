package org.courier.couriertracking.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.Instant;

@Data
public class LocationUpdateRequest {

    @NotNull
    private Instant time;       // ISO-8601 formatında gelecek (ör. 2025-12-07T11:30:00Z)

    @NotBlank
    private String courierId;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double lat;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double lng;
}
