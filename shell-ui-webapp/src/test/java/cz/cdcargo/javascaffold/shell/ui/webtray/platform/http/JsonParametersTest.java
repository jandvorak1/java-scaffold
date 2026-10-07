package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.helidon.http.BadRequestException;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonValue;

class JsonParametersTest {

    @Test
    void testRequiredString() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "test").build());

        assertEquals("test", parameters.requiredString("value"));
    }

    @Test
    void testRequiredStringMissing() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().build());
        var exception = assertThrows(BadRequestException.class, () -> parameters.requiredString("value"));

        assertEquals("Missing required JSON property: value", exception.getMessage());
    }

    @Test
    void testOptionalString() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "test").build());

        assertEquals("test", parameters.optionalString("value").orElseThrow());
    }

    @Test
    void testOptionalStringEmpty() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().build());

        assertTrue(parameters.optionalString("value").isEmpty());
    }

    @Test
    void testRequiredNonBlankString() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "test").build());

        assertEquals("test", parameters.requiredNonBlankString("value"));
    }

    @Test
    void testRequiredNonBlankStringThrows() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", " ").build());
        var exception = assertThrows(BadRequestException.class, () -> parameters.requiredNonBlankString("value"));

        assertEquals("Invalid JSON property: value", exception.getMessage());
    }

    @Test
    void testRequiredInt() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", 123).build());

        assertEquals(123, parameters.requiredInt("value"));
    }

    @Test
    void testRequiredIntType() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "123").build());

        assertThrows(BadRequestException.class, () -> parameters.requiredInt("value"));
    }

    @Test
    void testOptionalInt() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", 123).build());

        assertEquals(123, parameters.optionalInt("value").orElseThrow());
    }

    @Test
    void testRequiredLong() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", 123456789L).build());

        assertEquals(123456789L, parameters.requiredLong("value"));
    }

    @Test
    void testOptionalLong() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", 123456789L).build());

        assertEquals(123456789L, parameters.optionalLong("value").orElseThrow());
    }

    @Test
    void testRequiredDouble() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", 123.45).build());

        assertEquals(123.45, parameters.requiredDouble("value"));
    }

    @Test
    void testOptionalDouble() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", 123.45).build());

        assertEquals(123.45, parameters.optionalDouble("value").orElseThrow());
    }

    @Test
    void testRequiredBigDecimal() {
        var expected = new BigDecimal("123.45");
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", expected).build());

        assertEquals(expected, parameters.requiredBigDecimal("value"));
    }

    @Test
    void testOptionalBigDecimal() {
        var expected = new BigDecimal("123.45");
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", expected).build());

        assertEquals(expected, parameters.optionalBigDecimal("value").orElseThrow());
    }

    @Test
    void testRequiredBoolean() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", true).build());

        assertTrue(parameters.requiredBoolean("value"));
    }

    @Test
    void testOptionalBoolean() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", false).build());

        assertFalse(parameters.optionalBoolean("value").orElseThrow());
    }

    @Test
    void testRequiredLocalDate() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "2026-08-23").build());

        assertEquals(LocalDate.of(2026, 8, 23), parameters.requiredLocalDate("value"));
    }

    @Test
    void testRequiredLocalDateInvalid() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "invalid").build());
        var exception = assertThrows(BadRequestException.class, () -> parameters.requiredLocalDate("value"));

        assertEquals("Invalid JSON property: value", exception.getMessage());
    }

    @Test
    void testOptionalLocalDate() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "2026-08-23").build());

        assertEquals(LocalDate.of(2026, 8, 23), parameters.optionalLocalDate("value").orElseThrow());
    }

    @Test
    void testRequiredLocalTime() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "10:15:30").build());

        assertEquals(LocalTime.of(10, 15, 30), parameters.requiredLocalTime("value"));
    }

    @Test
    void testOptionalLocalTime() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "10:15:30").build());

        assertEquals(LocalTime.of(10, 15, 30), parameters.optionalLocalTime("value").orElseThrow());
    }

    @Test
    void testRequiredLocalDateTime() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "2026-08-23T10:15:30").build());

        assertEquals(LocalDateTime.of(2026, 8, 23, 10, 15, 30), parameters.requiredLocalDateTime("value"));
    }

    @Test
    void testOptionalLocalDateTime() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "2026-08-23T10:15:30").build());

        assertEquals(LocalDateTime.of(2026, 8, 23, 10, 15, 30),
                parameters.optionalLocalDateTime("value").orElseThrow());
    }

    @Test
    void testRequiredUUID() {
        var expected = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", expected.toString()).build());

        assertEquals(expected, parameters.requiredUUID("value"));
    }

    @Test
    void testOptionalUUID() {
        var expected = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", expected.toString()).build());

        assertEquals(expected, parameters.optionalUUID("value").orElseThrow());
    }

    @Test
    void testRequiredObject() {
        var expected = JsonValue.objectBuilder().set("name", "test").build();
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", expected).build());

        assertEquals(expected, parameters.requiredObject("value"));
    }

    @Test
    void testOptionalObject() {
        var expected = JsonValue.objectBuilder().set("name", "test").build();
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", expected).build());

        assertEquals(expected, parameters.optionalObject("value").orElseThrow());
    }

    @Test
    void testRequiredArray() {
        var expected = JsonArray.createStrings(List.of("one", "two"));
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", expected).build());

        assertEquals(expected, parameters.requiredArray("value"));
    }

    @Test
    void testOptionalArray() {
        var expected = JsonArray.createStrings(List.of("one", "two"));
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", expected).build());

        assertEquals(expected, parameters.optionalArray("value").orElseThrow());
    }

    @Test
    void testRequired() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "test").build());

        assertEquals(4, parameters.required("value", String::length));
    }

    @Test
    void testOptional() {
        var parameters = JsonParameters.of(JsonValue.objectBuilder().set("value", "test").build());

        assertEquals(4, parameters.optional("value", String::length).orElseThrow());
    }
}
