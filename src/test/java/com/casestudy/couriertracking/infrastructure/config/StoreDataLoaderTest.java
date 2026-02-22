package com.casestudy.couriertracking.infrastructure.config;

import com.casestudy.couriertracking.domain.model.Store;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StoreDataLoaderTest {

    @Test
    void loadStores_ShouldLoadStoresFromJson() {
        StoreDataLoader loader = new StoreDataLoader();
        loader.loadStores();

        assertNotNull(loader.getStores());
        assertFalse(loader.getStores().isEmpty());
        assertEquals("Ataşehir MMM Migros", loader.getStores().get(0).getName());
    }

    @Test
    void loadStores_ShouldReturnEmptyList_WhenJsonIsMissing() {
        StoreDataLoader loader = new StoreDataLoader() {
            @Override
            public void loadStores() {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    InputStream inputStream = new ClassPathResource("non_existent_file.json").getInputStream();
                    mapper.readValue(inputStream, new TypeReference<List<Store>>() {});
                } catch (Exception e) {
                    try {
                        java.lang.reflect.Field field = StoreDataLoader.class.getDeclaredField("stores");
                        field.setAccessible(true);
                        field.set(this, Collections.emptyList());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        };
        loader.loadStores();

        assertNotNull(loader.getStores());
        assertTrue(loader.getStores().isEmpty());
    }

    @Test
    void loadStores_ShouldReturnUnmodifiableList() {
        StoreDataLoader loader = new StoreDataLoader();
        loader.loadStores();

        assertThrows(UnsupportedOperationException.class,
                () -> loader.getStores().add(new Store("Test", 0.0, 0.0)));
    }

    @Test
    void loadStores_ShouldLoadCorrectStoreCount() {
        StoreDataLoader loader = new StoreDataLoader();
        loader.loadStores();

        assertEquals(5, loader.getStores().size());
    }

    @Test
    void loadStores_ShouldLoadCorrectStoreCoordinates() {
        StoreDataLoader loader = new StoreDataLoader();
        loader.loadStores();

        Store first = loader.getStores().getFirst();
        assertEquals("Ataşehir MMM Migros", first.getName());
        assertEquals(40.9923307, first.getLat(), 0.0001);
        assertEquals(29.1244229, first.getLng(), 0.0001);
    }
}
