package com.perfecthousehold;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DotenvLoaderTest {

    @Test
    void testLoadDotenv() {
        DotenvLoader.load();
        // Since .env exists with DB_PORT=5432, it should be in System properties
        String dbPort = System.getProperty("DB_PORT");
        assertNotNull(dbPort, "DB_PORT should be loaded from .env");
    }
}
