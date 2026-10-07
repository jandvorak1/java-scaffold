package cz.cdcargo.javascaffold.core.platform.preferences;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import cz.cdcargo.javascaffold.core.platform.build.BuildMetadata;
import cz.cdcargo.javascaffold.core.platform.naming.Texts;

/**
 * Persists preferences in a UTF-8 properties file.
 *
 * Values are stored in preferences.properties. The production store resolves
 * its directory from external configuration or the platform application
 * directory. Callers that require an isolated location can supply it
 * explicitly. Updates replace the destination file atomically when the file
 * system supports it. Access is serialized within this JVM.
 */
public final class FilePreferencesStore implements PreferencesStore {

    private static final String STORE_DIRECTORY = BuildMetadata.slug();
    private static final String PREFERENCES_FILE = "preferences.properties";
    private static final String ENVIRONMENT_VARIABLE = BuildMetadata.envPrefix() + "_PREFS_PATH";
    private static final String SYSTEM_PROPERTY = "prefs.path";
    private static final Object STORE_LOCK = new Object();

    private final Path root;
    private final String preferencesFileName;

    private FilePreferencesStore(Path root, String preferencesFileName) {
        this.root = Objects.requireNonNull(root, "Root must not be null").toAbsolutePath().normalize();
        this.preferencesFileName = Objects.requireNonNull(preferencesFileName,
                "Preferences file name must not be null");
    }

    /**
     * Creates a store that writes to the supplied directory.
     *
     * The directory is created when the first preference is stored. This
     * factory does not read environment variables or system properties.
     *
     * @param directory directory that will contain preferences.properties
     * @return store using the supplied directory
     * @throws NullPointerException if directory is null
     */
    public static FilePreferencesStore at(Path directory) {
        return new FilePreferencesStore(directory, PREFERENCES_FILE);
    }

    /**
     * Creates the production preference store.
     *
     * The directory is resolved from the application environment variable,
     * then the prefs.path system property, and finally the platform-specific
     * application directory.
     *
     * @return store using the configured application directory
     */
    public static FilePreferencesStore root() {
        return at(preferencesDirectory());
    }

    @Override
    public String get(String key, String defaultValue) {
        Objects.requireNonNull(key, "Key must not be null");
        synchronized (STORE_LOCK) {
            try {
                return load().getProperty(key, defaultValue);
            } catch (IOException | IllegalArgumentException e) {
                return defaultValue;
            }
        }
    }

    @Override
    public void put(String key, String value) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(value, "Value must not be null");
        synchronized (STORE_LOCK) {
            try {
                var properties = load();
                properties.setProperty(key, value);
                store(properties);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to store preferences", e);
            }
        }
    }

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, "Key must not be null");
        synchronized (STORE_LOCK) {
            try {
                var properties = load();
                if (properties.remove(key) != null) {
                    store(properties);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to remove preference", e);
            }
        }
    }

    private Properties load() throws IOException {
        var properties = new Properties();
        var file = preferencesFile();
        if (!Files.exists(file)) {
            return properties;
        }
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private void store(Properties properties) throws IOException {
        var file = preferencesFile();
        var directory = file.getParent();
        Files.createDirectories(directory);
        var temporaryFile = Files.createTempFile(directory, preferencesFileName, ".tmp");
        try {
            try (var writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE)) {
                properties.store(writer, null);
            }
            try {
                Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException failure) {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private Path preferencesFile() {
        return root.resolve(preferencesFileName);
    }

    private static Path preferencesDirectory() {
        var path = System.getenv(ENVIRONMENT_VARIABLE);
        if (path != null && !path.isBlank()) {
            return Path.of(path);
        }
        path = System.getProperty(SYSTEM_PROPERTY);
        if (path != null && !path.isBlank()) {
            return Path.of(path);
        }
        var os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return switch (os) {
            case String s when s.contains("win") -> {
                var appData = System.getenv("APPDATA");
                if (appData == null || appData.isBlank()) {
                    appData = System.getProperty("user.home");
                }
                yield Paths.get(appData, Texts.toPascalCase(STORE_DIRECTORY));
            }
            case String s when s.contains("mac") ->
                Paths.get(System.getProperty("user.home"), "Library", "Application Support",
                        Texts.toPascalCase(STORE_DIRECTORY));
            default -> {
                var xdg = System.getenv("XDG_CONFIG_HOME");
                var base = xdg != null && !xdg.isBlank()
                        ? Paths.get(xdg)
                        : Paths.get(System.getProperty("user.home"), ".config");
                yield base.resolve(STORE_DIRECTORY);
            }
        };
    }
}
