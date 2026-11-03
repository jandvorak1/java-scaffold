package cz.cdcargo.javascaffold.core.platform.preferences;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
    void setUp() {
        TEST_PROPERTIES.forEach(property -> originalSystemProperties.put(property, System.getProperty(property)));
    }

    @AfterEach
    void tearDown() {
        originalSystemProperties.forEach((property, value) -> {
            if (value == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, value);
            }
        });
    }

    @Test
    void testGet() {
        // Arrange
        var preferences = createPreferences();
        preferences.put("theme", "dark");

        // Act
        var reloaded = createPreferences();
        var output = reloaded.get("theme", "");

        // Assert
        assertEquals("dark", output);
    }

    @Test
    void testGetInt() {
        // Arrange
        var preferences = createPreferences();
        preferences.putInt("width", 325);

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getInt("width", 0);

        // Assert
        assertEquals(325, output);
    }

    @Test
    void testGetLong() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLong("width", 325L);

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getLong("width", 0L);

        // Assert
        assertEquals(325L, output);
    }

    @Test
    void testGetDouble() {
        // Arrange
        var preferences = createPreferences();
        preferences.putDouble("costs", 525.0);

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getDouble("costs", 0);

        // Assert
        assertEquals(525.0, output);
    }

    @Test
    void testGetFloat() {
        // Arrange
        var preferences = createPreferences();
        preferences.putFloat("costs", 525.0F);

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getFloat("costs", 0.0F);

        // Assert
        assertEquals(525.0F, output);
    }

    @Test
    void testGetBoolean() {
        // Arrange
        var preferences = createPreferences();
        preferences.putBoolean("maximized", true);

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getBoolean("maximized", false);

        // Assert
        assertTrue(output);
    }

    @Test
    void testGetByteArray() {
        // Arrange
        var preferences = createPreferences();
        preferences.putByteArray("code", "test".getBytes());

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getByteArray("code", "default".getBytes());

        // Assert
        assertArrayEquals("test".getBytes(), output);
    }

    @Test
    void testGetLocalDate() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLocalDate("last", LocalDate.of(2024, 6, 1));

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getLocalDate("last", LocalDate.EPOCH);

        // Assert
        assertEquals(LocalDate.parse("2024-06-01"), output);
    }

    @Test
    void testGetLocalTime() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLocalTime("last", LocalTime.of(0, 0));

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getLocalTime("last", LocalTime.MIN);

        // Assert
        assertEquals(LocalTime.parse("00:00:00"), output);
    }

    @Test
    void testGetLocalDateTime() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLocalDateTime("last", LocalDateTime.of(2024, 6, 1, 12, 30));

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getLocalDateTime("last", LocalDateTime.MIN);

        // Assert
        assertEquals(LocalDateTime.parse("2024-06-01T12:30:00"), output);
    }

    @Test
    void testGetLocale() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLocale("locale", Locale.forLanguageTag("cs-CZ"));

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getLocale("locale", Locale.forLanguageTag("en-US"));

        // Assert
        assertEquals(Locale.forLanguageTag("cs-CZ"), output);
    }

    @Test
    void testGetZoneId() {
        // Arrange
        var preferences = createPreferences();
        preferences.putZoneId("zone", ZoneId.of("Europe/Prague"));

        // Act +
        var reloaded = createPreferences();
        var output = reloaded.getZoneId("zone", ZoneId.of("America/New_York"));

        // Assert
        assertEquals(ZoneId.of("Europe/Prague"), output);
    }

    @Test
    void testGetPath() {
        // Arrange
        var preferences = createPreferences();
        preferences.putPath("path", Path.of("home", "test"));

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getPath("path", Path.of("home", "example"));

        // Assert
        assertEquals(Path.of("home", "test"), output);
    }

    @Test
    void testGetLevel() {
        // Arrange
        var preferences = createPreferences();
        preferences.putLevel("level", Level.WARNING);

        // Act
        var reloaded = createPreferences();
        var output = reloaded.getLevel("level", Level.INFO);

        // Assert
        assertEquals(Level.WARNING, output);
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
