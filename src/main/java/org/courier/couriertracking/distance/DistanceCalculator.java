package org.courier.couriertracking.distance;

public interface DistanceCalculator {

    /**
     * @return distance in meters between two GPS points
     */
    double calculateDistanceMeters(double lat1, double lng1,
                                   double lat2, double lng2);
}
