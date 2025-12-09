package org.courier.couriertracking.repository;

import org.courier.couriertracking.domain.CourierTrack;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CourierTrackRepository {

    private final Map<String, CourierTrack> tracks = new HashMap<>();

    public synchronized CourierTrack getOrCreate(String courierId) {
        return tracks.computeIfAbsent(courierId, CourierTrack::new);
    }

    public synchronized Optional<CourierTrack> findByCourierId(String courierId) {
        return Optional.ofNullable(tracks.get(courierId));
    }
}
