package org.courier.couriertracking.event;

import org.courier.couriertracking.dto.LocationUpdateRequest;

/**
 * Simple Spring event carrying the location update.
 * (Observer pattern via Spring's event mechanism)
 */
public record LocationUpdatedEvent(LocationUpdateRequest request) {
}
