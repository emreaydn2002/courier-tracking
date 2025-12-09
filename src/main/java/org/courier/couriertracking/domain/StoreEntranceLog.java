package org.courier.couriertracking.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class StoreEntranceLog {

    private final String courierId;
    private final String storeName;
    private final Instant entranceTime;
    private final double distanceMeters;
}
