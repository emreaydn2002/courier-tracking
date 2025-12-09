package org.courier.couriertracking.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CourierTrack {

    private final String courierId;

    private Double lastLat;
    private Double lastLng;
    private Instant lastTime;

    private double totalDistanceMeters;

    public CourierTrack(String courierId) {
        this.courierId = courierId;
    }

    public boolean hasLastLocation() {
        return lastLat != null && lastLng != null && lastTime != null;
    }

    public void addDistance(double distanceMeters) {
        this.totalDistanceMeters += distanceMeters;
    }

    public void updateLastLocation(Double lat, Double lng, Instant time) {
        this.lastLat = lat;
        this.lastLng = lng;
        this.lastTime = time;
    }
}
