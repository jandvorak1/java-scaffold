package cz.cdcargo.javascaffold.shell.ui.webapp.platform.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WebCorsTest {

    @Test
    void testCreateCorsFeature() {
        var cors = new WebCors("http://localhost:5402,http://127.0.0.1:5402");
        var feature = cors.create();
        assertTrue(feature.isPresent());
    }

    @Test
    void testCreateWithoutOrigins() {
        assertTrue(new WebCors(" ").create().isEmpty());
    }
}
