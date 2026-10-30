package com.perfecthousehold;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayConfigTest {

    @Test
    void testFlywayDateFormatMigrationNaming() {
        Configuration config = Flyway.configure()
                .sqlMigrationPrefix("V")
                .sqlMigrationSeparator("__")
                .sqlMigrationSuffixes(".sql");

        ResourceNameParser parser = new ResourceNameParser(config);
        ResourceName resource = parser.parse("V261030_1645__create_users_table.sql");

        assertTrue(resource.isValid(), "Filename V261030_1645__create_users_table.sql must be valid under 'V' prefix config");
        assertEquals("261030.1645", resource.getVersion().getVersion());
        assertEquals("create users table", resource.getDescription());
    }
}
