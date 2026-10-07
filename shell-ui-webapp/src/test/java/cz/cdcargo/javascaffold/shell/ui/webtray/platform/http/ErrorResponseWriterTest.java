package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.helidon.http.HttpMediaType;

class ErrorResponseWriterTest {

    @Test
    void testErrorResponseWriterWithNullResponseWriter() {
        var exception = assertThrows(NullPointerException.class, () -> new ErrorResponseWriter(null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testSelectResponseTypeUsesHighestPrioritySupportedType() {
        var acceptedTypes = List.of(HttpMediaType.create("application/json;q=0.9"),
                HttpMediaType.create("text/plain;q=0.5"));

        assertEquals(ErrorResponseWriter.ResponseType.JSON, ErrorResponseWriter.selectResponseType(acceptedTypes));
    }

    @Test
    void testSelectResponseTypeIgnoresZeroQualityType() {
        var acceptedTypes = List.of(HttpMediaType.create("text/html;q=0"));

        assertEquals(ErrorResponseWriter.ResponseType.EMPTY, ErrorResponseWriter.selectResponseType(acceptedTypes));
    }

    @Test
    void testSelectResponseTypeWithWildcard() {
        var acceptedTypes = List.of(HttpMediaType.create("*/*"));

        assertEquals(ErrorResponseWriter.ResponseType.HTML, ErrorResponseWriter.selectResponseType(acceptedTypes));
    }
}
