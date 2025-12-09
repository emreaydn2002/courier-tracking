package org.courier.couriertracking.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.courier.couriertracking.domain.CourierTrack;
import org.courier.couriertracking.distance.DistanceCalculator;
import org.courier.couriertracking.dto.LocationUpdateRequest;
import org.courier.couriertracking.repository.CourierTrackRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationDistanceEventHandler {

    private final CourierTrackRepository courierTrackRepository;
    private final DistanceCalculator distanceCalculator;

    @EventListener
    public void onLocationUpdated(LocationUpdatedEvent event) {
        LocationUpdateRequest req = event.request();

        CourierTrack track = courierTrackRepository.getOrCreate(req.getCourierId());

        if (track.hasLastLocation()) {
            double distance = distanceCalculator.calculateDistanceMeters(
                    track.getLastLat(), track.getLastLng(),
                    req.getLat(), req.getLng()
            );
            track.addDistance(distance);

            log.info("Courier {} traveled additional {} m, total {} m",
                    track.getCourierId(), distance, track.getTotalDistanceMeters());
        }

        track.updateLastLocation(req.getLat(), req.getLng(), req.getTime());
    }
}
