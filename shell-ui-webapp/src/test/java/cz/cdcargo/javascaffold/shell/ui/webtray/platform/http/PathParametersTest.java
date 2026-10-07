package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.helidon.common.parameters.Parameters;
import io.helidon.http.BadRequestException;

class PathParametersTest {

    @Test
    void testRequiredString() {
        var parameters = createParameters("test");
        var output = PathParameters.of(parameters);

        assertEquals("test", output.requiredString("value"));
    }

    @Test
    void testRequiredStringMissing() {
        var parameters = Parameters.empty("path");
        var output = PathParameters.of(parameters);
        var exception = assertThrows(BadRequestException.class, () -> output.requiredString("value"));

        assertEquals("Missing required path parameter: value", exception.getMessage());
    }

    @Test
    void testOptionalString() {
        var parameters = createParameters("test");
        var output = PathParameters.of(parameters);

        assertEquals("test", output.optionalString("value").orElseThrow());
    }

    @Test
    void testOptionalStringEmpty() {
        var parameters = Parameters.empty("path");
        var output = PathParameters.of(parameters);

        assertTrue(output.optionalString("value").isEmpty());
    }

    @Test
    void testRequiredNonBlankString() {
        var parameters = createParameters("test");
        var output = PathParameters.of(parameters);

        assertEquals("test", output.requiredNonBlankString("value"));
    }

    @Test
    void testRequiredNonBlankStringThrows() {
        var parameters = createParameters(" ");
        var output = PathParameters.of(parameters);
        var exception = assertThrows(BadRequestException.class, () -> output.requiredNonBlankString("value"));

        assertEquals("Invalid path parameter: value", exception.getMessage());
    }

    @Test
    void testRequiredInt() {
        var parameters = createParameters("123");
        var output = PathParameters.of(parameters);

        assertEquals(123, output.requiredInt("value"));
    }

    @Test
    void testRequiredIntInvalid() {
        var output = PathParameters.of(createParameters("invalid"));
        var exception = assertThrows(BadRequestException.class, () -> output.requiredInt("value"));

        assertEquals("Invalid path parameter: value", exception.getMessage());
    }

    @Test
    void testOptionalInt() {
        var parameters = createParameters("123");
        var output = PathParameters.of(parameters);

        assertEquals(123, output.optionalInt("value").orElseThrow());
    }

    @Test
    void testRequiredLong() {
        var parameters = createParameters("123456789");
        var output = PathParameters.of(parameters);

        assertEquals(123456789L, output.requiredLong("value"));
    }

    @Test
    void testOptionalLong() {
        var parameters = createParameters("123456789");
        var output = PathParameters.of(parameters);

        assertEquals(123456789L, output.optionalLong("value").orElseThrow());
    }

    @Test
    void testRequiredDouble() {
        var parameters = createParameters("123.45");
        var output = PathParameters.of(parameters);

        assertEquals(123.45, output.requiredDouble("value"));
    }

    @Test
    void testOptionalDouble() {
        var parameters = createParameters("123.45");
        var output = PathParameters.of(parameters);

        assertEquals(123.45, output.optionalDouble("value").orElseThrow());
    }

    @Test
    void testRequiredBigDecimal() {
        var parameters = createParameters("123.45");
        var output = PathParameters.of(parameters);

        assertEquals(new BigDecimal("123.45"), output.requiredBigDecimal("value"));
    }

    @Test
    void testOptionalBigDecimal() {
        var parameters = createParameters("123.45");
        var output = PathParameters.of(parameters);

        assertEquals(new BigDecimal("123.45"), output.optionalBigDecimal("value").orElseThrow());
    }

    @Test
    void testRequiredBoolean() {
        var parameters = createParameters("true");
        var output = PathParameters.of(parameters);

        assertTrue(output.requiredBoolean("value"));
    }

    @Test
    void testOptionalBoolean() {
        var parameters = createParameters("false");
        var output = PathParameters.of(parameters);

        assertFalse(output.optionalBoolean("value").orElseThrow());
    }

    @Test
    void testRequiredLocalDate() {
        var parameters = createParameters("2026-08-23");
        var output = PathParameters.of(parameters);

        assertEquals(LocalDate.of(2026, 8, 23), output.requiredLocalDate("value"));
    }

    @Test
    void testOptionalLocalDate() {
        var parameters = createParameters("2026-08-23");
        var output = PathParameters.of(parameters);

        assertEquals(LocalDate.of(2026, 8, 23), output.optionalLocalDate("value").orElseThrow());
    }

    @Test
    void testRequiredLocalTime() {
        var parameters = createParameters("10:15:30");
        var output = PathParameters.of(parameters);

        assertEquals(LocalTime.of(10, 15, 30), output.requiredLocalTime("value"));
    }

    @Test
    void testOptionalLocalTime() {
        var parameters = createParameters("10:15:30");
        var output = PathParameters.of(parameters);

        assertEquals(LocalTime.of(10, 15, 30), output.optionalLocalTime("value").orElseThrow());
    }

    @Test
    void testRequiredLocalDateTime() {
        var parameters = createParameters("2026-08-23T10:15:30");
        var output = PathParameters.of(parameters);

        assertEquals(LocalDateTime.of(2026, 8, 23, 10, 15, 30), output.requiredLocalDateTime("value"));
    }

    @Test
    void testOptionalLocalDateTime() {
        var parameters = createParameters("2026-08-23T10:15:30");
        var output = PathParameters.of(parameters);

        assertEquals(LocalDateTime.of(2026, 8, 23, 10, 15, 30), output.optionalLocalDateTime("value").orElseThrow());
    }

    @Test
    void testRequiredUUID() {
        var parameters = createParameters("123e4567-e89b-12d3-a456-426614174000");
        var output = PathParameters.of(parameters);

        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), output.requiredUUID("value"));
    }

    @Test
    void testOptionalUUID() {
        var parameters = createParameters("123e4567-e89b-12d3-a456-426614174000");
        var output = PathParameters.of(parameters);

        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                output.optionalUUID("value").orElseThrow());
    }

    private static Parameters createParameters(String value) {
        return Parameters.createSingleValueMap("test", Map.of("value", value));
    }
}
