package org.courier.couriertracking.service;

import org.courier.couriertracking.domain.StoreEntranceLog;
import org.courier.couriertracking.dto.LocationUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests business rules by calling LocationService directly with DTOs:
 *  - total travel distance is accumulated,
 *  - store entrances respect the 1-minute re-entry rule.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LocationServiceTest {

    @Autowired
    private LocationService locationService;

    @Test
    @DisplayName("Should accumulate total distance between courier locations")
    void shouldAccumulateTotalDistanceForCourier() {
        // Given: same courier, iki farklı konum
        LocationUpdateRequest first = buildLocation(
                "2025-12-07T11:30:00Z", "c1",
                40.99235, 29.12440
        );

        LocationUpdateRequest second = buildLocation(
                "2025-12-07T11:32:00Z", "c1",
                40.99300, 29.12500
        );

        // When: servis doğrudan DTO ile çağrılıyor
        locationService.handleLocationUpdate(first);
        locationService.handleLocationUpdate(second);

        double totalDistance = locationService.getTotalTravelDistance("c1");

        // Then: toplam mesafe > 0 olmalı
        assertThat(totalDistance)
                .as("total distance for courier c1")
                .isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Should log store entrances and apply 1-minute re-entry rule")
    void shouldApplyOneMinuteReentryRule() {
        // Ataşehir MMM Migros'a çok yakın üç konum
        LocationUpdateRequest first = buildLocation(
                "2025-12-07T11:30:00Z", "c1",
                40.99235, 29.12440
        );

        // 30 saniye sonrası, 1 dk dolmadığı için yeni giriş sayılmamalı
        LocationUpdateRequest second = buildLocation(
                "2025-12-07T11:30:30Z", "c1",
                40.99236, 29.12441
        );

        // 1 dk 10 sn sonrası, ikinci giriş sayılmalı
        LocationUpdateRequest third = buildLocation(
                "2025-12-07T11:31:40Z", "c1",
                40.99234, 29.12439
        );

        // When
        locationService.handleLocationUpdate(first);
        locationService.handleLocationUpdate(second);
        locationService.handleLocationUpdate(third);

        List<StoreEntranceLog> entrances =
                locationService.getEntrancesForCourier("c1");

        // Then
        assertThat(entrances)
                .as("store entrances for courier c1")
                .hasSize(2); // 1 dk kuralı nedeniyle 2 giriş

        assertThat(entrances.get(0).getStoreName())
                .isEqualTo("Ataşehir MMM Migros");
    }

    // --- helper ---

    private LocationUpdateRequest buildLocation(String time,
                                                String courierId,
                                                double lat,
                                                double lng) {
        LocationUpdateRequest req = new LocationUpdateRequest();
        req.setTime(Instant.parse(time));
        req.setCourierId(courierId);
        req.setLat(lat);
        req.setLng(lng);
        return req;
    }
}
