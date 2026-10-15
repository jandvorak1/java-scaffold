package cz.cdcargo.javascaffold.core.platform.preferences;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class FilePreferencesStoreTest {

    private static final List<String> TEST_PROPERTIES = List.of(
            "theme", "width", "costs", "maximized",
            "code", "last", "locale", "zone", "path", "level", "override");

    @TempDir
    Path tempDir;

    private final Map<String, String> originalSystemProperties = new HashMap<>();

    @BeforeEach
    void storeSystemProperties() {
        TEST_PROPERTIES.forEach(property -> originalSystemProperties.put(property, System.getProperty(property)));
    }

    @AfterEach
    void restoreSystemProperties() {
        originalSystemProperties.forEach((property, value) -> {
            if (value == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, value);
            }
        });
    }

    @Test
    void testGetReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.put("theme", "dark");

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.get("theme", "");
        assertEquals("dark", output);
        System.setProperty("theme", "light");
        var effectiveOutput = reloaded.get("theme", "default", null, "theme");
        assertEquals("light", effectiveOutput);
    }

    @Test
    void testGetIntReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putInt("width", 325);

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getInt("width", 0);
        assertEquals(325, output);
        System.setProperty("width", "440");
        var effectiveOutput = reloaded.getInt("width", 0, null, "width");
        assertEquals(440, effectiveOutput);
    }

    @Test
    void testGetLongReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLong("width", 325L);

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getLong("width", 0L);
        assertEquals(325L, output);
        System.setProperty("width", "440");
        var effectiveOutput = reloaded.getLong("width", 0L, null, "width");
        assertEquals(440L, effectiveOutput);
    }

    @Test
    void testGetDoubleReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putDouble("costs", 525.0);

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getDouble("costs", 0);
        assertEquals(525.0, output);
        System.setProperty("costs", "700.1");
        var effectiveOutput = reloaded.getDouble("costs", 0, null, "costs");
        assertEquals(700.1, effectiveOutput);
    }

    @Test
    void testGetFloatReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putFloat("costs", 525.0F);

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getFloat("costs", 0.0F);
        assertEquals(525.0F, output);
        System.setProperty("costs", "700.1");
        var effectiveOutput = reloaded.getFloat("costs", 0.0F, null, "costs");
        assertEquals(700.1F, effectiveOutput);
    }

    @Test
    void testGetBooleanReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putBoolean("maximized", true);

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getBoolean("maximized", false);
        assertTrue(output);
        System.setProperty("maximized", "false");
        var effectiveOutput = reloaded.getBoolean("maximized", true, null, "maximized");
        assertFalse(effectiveOutput);
    }

    @Test
    void testGetByteArrayReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putByteArray("code", "test".getBytes());

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getByteArray("code", "default".getBytes());
        assertArrayEquals("test".getBytes(), output);
        var overriddenValue = "override".getBytes();
        System.setProperty("code", Base64.getEncoder().encodeToString(overriddenValue));
        var effectiveOutput = reloaded.getByteArray("code", "default".getBytes(), null, "code");
        assertArrayEquals(overriddenValue, effectiveOutput);
    }

    @Test
    void testGetLocalDateReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLocalDate("last", LocalDate.of(2024, 6, 1));

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getLocalDate("last", LocalDate.EPOCH);
        assertEquals(LocalDate.parse("2024-06-01"), output);
        System.setProperty("last", "2026-05-24");
        var effectiveOutput = reloaded.getLocalDate("last", LocalDate.EPOCH, null, "last");
        assertEquals(LocalDate.of(2026, 5, 24), effectiveOutput);
    }

    @Test
    void testGetLocalTimeReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLocalTime("last", LocalTime.of(0, 0));

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getLocalTime("last", LocalTime.MIN);
        assertEquals(LocalTime.parse("00:00:00"), output);
        System.setProperty("last", "15:30:00");
        var effectiveOutput = reloaded.getLocalTime("last", LocalTime.MIN, null, "last");
        assertEquals(LocalTime.of(15, 30), effectiveOutput);
    }

    @Test
    void testGetLocalDateTimeReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLocalDateTime("last", LocalDateTime.of(2024, 6, 1, 12, 30));

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getLocalDateTime("last", LocalDateTime.MIN);
        assertEquals(LocalDateTime.parse("2024-06-01T12:30:00"), output);
        System.setProperty("last", "2026-05-24T15:30:00");
        var effectiveOutput = reloaded.getLocalDateTime("last", LocalDateTime.MIN, null, "last");
        assertEquals(LocalDateTime.of(2026, 5, 24, 15, 30), effectiveOutput);
    }

    @Test
    void testGetLocaleReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLocale("locale", Locale.forLanguageTag("cs-CZ"));

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getLocale("locale", Locale.forLanguageTag("en-US"));
        assertEquals(Locale.forLanguageTag("cs-CZ"), output);
        System.setProperty("locale", "pl-PL");
        var effectiveOutput = reloaded.getLocale("locale", Locale.forLanguageTag("en-US"), null, "locale");
        assertEquals(Locale.forLanguageTag("pl-PL"), effectiveOutput);
    }

    @Test
    void testGetZoneIdReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putZoneId("zone", ZoneId.of("Europe/Prague"));

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getZoneId("zone", ZoneId.of("America/New_York"));
        assertEquals(ZoneId.of("Europe/Prague"), output);
        System.setProperty("zone", "Europe/London");
        var effectiveOutput = reloaded.getZoneId("zone", ZoneId.of("America/New_York"), null, "zone");
        assertEquals(ZoneId.of("Europe/London"), effectiveOutput);
    }

    @Test
    void testGetPathReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putPath("path", Path.of("home", "test"));

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getPath("path", Path.of("home", "example"));
        assertEquals(Path.of("home", "test"), output);
        System.setProperty("path", Path.of("home", "property").toString());
        var effectiveOutput = reloaded.getPath("path", Path.of("home"), null, "path");
        assertEquals(Path.of("home", "property"), effectiveOutput);
    }

    @Test
    void testGetLevelReturnsStoredOrSystemPropertyValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLevel("level", Level.WARNING);

        // Act + Assert
        var reloaded = createPreferences();
        var output = reloaded.getLevel("level", Level.INFO);
        assertEquals(Level.WARNING, output);
        System.setProperty("level", Level.SEVERE.getName());
        var effectiveOutput = reloaded.getLevel("level", Level.INFO, null, "level");
        assertEquals(Level.SEVERE, effectiveOutput);
    }

    @Test
    void testGetIntReturnsDefaultForMissingOrInvalidValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.put("badInt", "invalid");

        // Act + Assert
        assertEquals(42, preferences.getInt("missingInt", 42));
        assertEquals(42, preferences.getInt("badInt", 42));
    }

    @Test
    void testGetLongReturnsDefaultForMissingOrInvalidValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.put("badLong", "invalid");

        // Act + Assert
        assertEquals(77L, preferences.getLong("missingLong", 77L));
        assertEquals(77L, preferences.getLong("badLong", 77L));
    }

    @Test
    void testGetBooleanReturnsDefaultForMissingOrInvalidValue() {
        // Arrange
        var preferences = createPreferences();
        preferences.put("badBool", "invalid");

        // Act + Assert
        assertTrue(preferences.getBoolean("missingBool", true));
        assertFalse(preferences.getBoolean("missingBool", false));
        assertTrue(preferences.getBoolean("badBool", true));
    }

    @Test
    void testGetReturnsDefaultForMissingValue() {
        // Arrange
        var preferences = createPreferences();

        // Act + Assert
        assertEquals("fallback", preferences.get("missingString", "fallback"));
        assertEquals("", preferences.get("blank", ""));
    }

    @Test
    void testPutRejectsNullKey() {
        // Arrange
        var preferences = createPreferences();

        // Act + Assert
        assertThrows(NullPointerException.class, () -> preferences.put(null, "x"));
    }

    @Test
    void testAtRejectsNullDirectory() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> FilePreferencesStore.at(null));
    }

    @Test
    void testGetRejectsNullKey() {
        // Arrange
        var preferences = createPreferences();

        // Act + Assert
        assertThrows(NullPointerException.class, () -> preferences.get(null, "x"));
    }

    @Test
    void testGetWithOverridesRejectsNullKey() {
        // Arrange
        var preferences = createPreferences();
        System.setProperty("override", "42");

        // Act + Assert
        assertThrows(NullPointerException.class, () -> preferences.get(null, "default", null, "override"));
    }

    @Test
    void testGetIntWithOverridesRejectsNullKey() {
        // Arrange
        var preferences = createPreferences();
        System.setProperty("override", "42");

        // Act + Assert
        assertThrows(NullPointerException.class, () -> preferences.getInt(null, 0, null, "override"));
    }

    @Test
    void testGetIgnoresBlankOverrideNames() {
        // Arrange
        var preferences = createPreferences();
        preferences.put("key", "stored");

        // Act + Assert
        assertEquals("stored", preferences.get("key", "default", " ", "\t"));
    }

    @Test
    void testAtStoresPreferencesInSpecifiedDirectory() {
        // Arrange
        var preferences = createPreferences();
        preferences.put("source", "application");

        // Act + Assert
        assertEquals("application", preferences.get("source", ""));
        assertTrue(Files.exists(tempDir.resolve("preferences").resolve("preferences.properties")));
    }

    @Test
    void testGetReloadsUnicodeValues() {
        // Arrange
        var preferences = createPreferences();
        preferences.put("caption", "Příliš žluťoučký kůň úpěl ďábelské ódy");
        preferences.put("city", "Žďár nad Sázavou");
        var reloaded = createPreferences();

        // Act + Assert
        assertEquals("Příliš žluťoučký kůň úpěl ďábelské ódy", reloaded.get("caption", ""));
        assertEquals("Žďár nad Sázavou", reloaded.get("city", ""));
    }

    @Test
    void testRemove() {
        // Arrange
        var preferences = createPreferences();

        // Act + Assert
        preferences.put("theme", "dark");
        assertEquals("dark", preferences.get("theme", "light"));
        preferences.remove("theme");
        assertEquals("light", preferences.get("theme", "light"));
    }

    private FilePreferencesStore createPreferences() {
        // Arrange + Act + Assert
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }
}
