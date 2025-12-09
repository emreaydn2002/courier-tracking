package org.courier.couriertracking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.courier.couriertracking.domain.StoreEntranceLog;
import org.courier.couriertracking.dto.LocationUpdateRequest;
import org.courier.couriertracking.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/locations")
    public ResponseEntity<Void> updateLocation(@Valid @RequestBody LocationUpdateRequest request) {
        locationService.handleLocationUpdate(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/couriers/{courierId}/distance")
    public ResponseEntity<Double> getTotalDistance(@PathVariable String courierId) {
        double distance = locationService.getTotalTravelDistance(courierId);
        return ResponseEntity.ok(distance);
    }

    @GetMapping("/couriers/{courierId}/entrances")
    public ResponseEntity<List<StoreEntranceLog>> getEntrances(@PathVariable String courierId) {
        return ResponseEntity.ok(locationService.getEntrancesForCourier(courierId));
    }
}
