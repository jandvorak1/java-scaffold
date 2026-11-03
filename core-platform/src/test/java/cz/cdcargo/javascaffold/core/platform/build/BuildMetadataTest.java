package cz.cdcargo.javascaffold.core.platform.build;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertFalse(BuildMetadata.name().isBlank());
    }

    @Test
    void testVersion() {
        // Arrange + Act + Assert
        assertFalse(BuildMetadata.version().isBlank());
    }

    @Test
    void testEnvPrefix() {
        // Arrange + Act + Assert
        assertFalse(BuildMetadata.envPrefix().isBlank());
    }

    @Test
    void testSlug() {
        // Arrange + Act + Assert
        assertFalse(BuildMetadata.slug().isBlank());
    }
}
