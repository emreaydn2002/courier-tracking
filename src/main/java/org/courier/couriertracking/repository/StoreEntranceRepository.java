package org.courier.couriertracking.repository;

import org.courier.couriertracking.domain.Store;
import org.courier.couriertracking.domain.StoreEntranceLog;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StoreEntranceRepository {

    private static final long MIN_REENTRY_SECONDS = 60;

    private final List<StoreEntranceLog> logs = new ArrayList<>();
    private final Map<String, Instant> lastEntrances = new HashMap<>();

    private String key(String courierId, String storeName) {
        return courierId + "|" + storeName;
    }

    public synchronized void logEntranceIfAllowed(String courierId,
                                                  Store store,
                                                  Instant time,
                                                  double distanceMeters) {

        String k = key(courierId, store.getName());
        Instant last = lastEntrances.get(k);

        // 1 dakika içinde aynı mağazaya tekrar giriş sayma
        if (last != null && !time.isAfter(last.plusSeconds(MIN_REENTRY_SECONDS))) {
            return;
        }

        StoreEntranceLog log = new StoreEntranceLog(
                courierId,
                store.getName(),
                time,
                distanceMeters
        );
        logs.add(log);
        lastEntrances.put(k, time);
    }

    public synchronized List<StoreEntranceLog> findByCourierId(String courierId) {
        return logs.stream()
                .filter(l -> l.getCourierId().equals(courierId))
                .toList();
    }
}
