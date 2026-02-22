package com.casestudy.couriertracking.infrastructure.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.casestudy.couriertracking.domain.model.Store;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Loads store data from stores.json on application startup
 */
@Component
@Slf4j
public class StoreDataLoader {

    @Getter
    private List<Store> stores;

    @PostConstruct
    public void loadStores() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("stores.json").getInputStream();
            stores = Collections.unmodifiableList(
                    mapper.readValue(inputStream, new TypeReference<List<Store>>() {
                    }));
        } catch (Exception e) {
            log.error("Failed to load stores.json", e);
            stores = Collections.emptyList();
        }
    }
}
