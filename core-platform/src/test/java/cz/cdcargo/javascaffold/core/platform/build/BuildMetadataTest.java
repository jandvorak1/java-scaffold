package cz.cdcargo.javascaffold.core.platform.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BuildMetadataTest {

    @Test
    void testBuild() {
        assertTrue(BuildMetadata.build().matches("\\d{10}"));
    }

    @Test
    void testName() {
        assertEquals("Java Scaffold", BuildMetadata.name());
    }

    @Test
    void testVersion() {
        assertEquals("1.0.0", BuildMetadata.version());
    }

    @Test
    void testEnvPrefix() {
        assertEquals("JAVA_SCAFFOLD", BuildMetadata.envPrefix());
    }

    @Test
    void testSlug() {
        assertEquals("java-scaffold", BuildMetadata.slug());
    }
}
