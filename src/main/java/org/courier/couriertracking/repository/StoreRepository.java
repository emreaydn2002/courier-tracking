package org.courier.couriertracking.repository;

import lombok.extern.slf4j.Slf4j;
import org.courier.couriertracking.domain.Store;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StoreRepository {

    private final ObjectMapper objectMapper;
    private final List<Store> stores = new ArrayList<>();

    public StoreRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadStores() throws IOException {
        ClassPathResource resource = new ClassPathResource("stores.json");
        try (InputStream is = resource.getInputStream()) {
            List<Store> loaded =
                    objectMapper.readValue(is, new TypeReference<List<Store>>() {});
            stores.clear();
            stores.addAll(loaded);
            log.info("Loaded {} stores from stores.json", stores.size());
        }
    }

    public List<Store> findAll() {
        return Collections.unmodifiableList(stores);
    }
}
