package org.courier.couriertracking.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.courier.couriertracking.distance.DistanceCalculator;
import org.courier.couriertracking.domain.Store;
import org.courier.couriertracking.dto.LocationUpdateRequest;
import org.courier.couriertracking.repository.StoreEntranceRepository;
import org.courier.couriertracking.repository.StoreRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreEntranceEventHandler {

    private static final double ENTRANCE_RADIUS_METERS = 100.0;

    private final StoreRepository storeRepository;
    private final DistanceCalculator distanceCalculator;
    private final StoreEntranceRepository storeEntranceRepository;

    @EventListener
    public void onLocationUpdated(LocationUpdatedEvent event) {
        LocationUpdateRequest req = event.request();

        for (Store store : storeRepository.findAll()) {
            double distance = distanceCalculator.calculateDistanceMeters(
                    req.getLat(), req.getLng(),
                    store.getLat(), store.getLng()
            );

            if (distance <= ENTRANCE_RADIUS_METERS) {
                storeEntranceRepository.logEntranceIfAllowed(
                        req.getCourierId(),
                        store,
                        req.getTime(),
                        distance
                );
                log.info("Courier {} entered radius of store {} ({} m)",
                        req.getCourierId(), store.getName(), distance);
            }
        }
    }
}
