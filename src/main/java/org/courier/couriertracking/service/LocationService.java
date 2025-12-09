package org.courier.couriertracking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.courier.couriertracking.domain.CourierTrack;
import org.courier.couriertracking.domain.StoreEntranceLog;
import org.courier.couriertracking.dto.LocationUpdateRequest;
import org.courier.couriertracking.event.LocationUpdatedEvent;
import org.courier.couriertracking.repository.CourierTrackRepository;
import org.courier.couriertracking.repository.StoreEntranceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final ApplicationEventPublisher eventPublisher;
    private final CourierTrackRepository courierTrackRepository;
    private final StoreEntranceRepository storeEntranceRepository;

    public void handleLocationUpdate(LocationUpdateRequest request) {
        log.info("Received location update: {}", request);
        // Observer pattern: publish event, listeners will react
        eventPublisher.publishEvent(new LocationUpdatedEvent(request));
    }

    public double getTotalTravelDistance(String courierId) {
        return courierTrackRepository.findByCourierId(courierId)
                .map(CourierTrack::getTotalDistanceMeters)
                .orElse(0.0);
    }

    public List<StoreEntranceLog> getEntrancesForCourier(String courierId) {
        return storeEntranceRepository.findByCourierId(courierId);
    }
}
