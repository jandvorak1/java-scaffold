package cz.cdcargo.javascaffold.core.platform.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BuildMetadataTest {

    @Test
    void testBuild() {
        // Arrange + Act + Assert
        assertTrue(BuildMetadata.build().matches("\\d{10}"));
    }

    @Test
    void testName() {
        // Arrange + Act + Assert
        assertEquals("Java Scaffold", BuildMetadata.name());
    }

    @Test
    void testVersion() {
        // Arrange + Act + Assert
        assertEquals("1.0.0", BuildMetadata.version());
    }

    @Test
    void testEnvPrefix() {
        // Arrange + Act + Assert
        assertEquals("JAVA_SCAFFOLD", BuildMetadata.envPrefix());
    }

    @Test
    void testSlug() {
        // Arrange + Act + Assert
        assertEquals("java-scaffold", BuildMetadata.slug());
    }
}
