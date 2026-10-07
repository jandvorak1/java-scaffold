package cz.cdcargo.javascaffold.core.platform.build;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Provides metadata created during the application build.
 *
 * The metadata is loaded from the application resource when this class is
 * initialized and remains available for the lifetime of the process. The
 * application name, version, and build identifier are optional. The application
 * slug and environment variable prefix are required.
 */
public final class BuildMetadata {

    private static final String PROPERTIES_FILE = "/app.properties";
    private static final String NAME_KEY = "app.name";
    private static final String VERSION_KEY = "app.version";
    private static final String BUILD_KEY = "app.build";
    private static final String SLUG_KEY = "app.slug";
    private static final String ENV_PREFIX_KEY = "app.env.prefix";
    private static final String UNAVAILABLE_VALUE = "N/A";
    private static final Properties PROPERTIES = loadProperties();

    private BuildMetadata() {
    }

    /**
     * Returns the application name from the build metadata.
     *
     * @return the application name, or N/A when it is unavailable
     */
    public static String name() {
        return PROPERTIES.getProperty(NAME_KEY, UNAVAILABLE_VALUE);
    }

    /**
     * Returns the application version from the build metadata.
     *
     * @return the application version, or N/A when it is unavailable
     */
    public static String version() {
        return PROPERTIES.getProperty(VERSION_KEY, UNAVAILABLE_VALUE);
    }

    /**
     * Returns the build identifier from the build metadata.
     *
     * @return the build identifier, or N/A when it is unavailable
     */
    public static String build() {
        return PROPERTIES.getProperty(BUILD_KEY, UNAVAILABLE_VALUE);
    }

    /**
     * Returns the application slug used for local storage paths.
     *
     * @return the application slug
     * @throws IllegalStateException when the slug is missing or blank
     */
    public static String slug() {
        return requiredProperty(SLUG_KEY);
    }

    /**
     * Returns the environment variable prefix used by the application.
     *
     * @return the environment variable prefix
     * @throws IllegalStateException when the prefix is missing or blank
     */
    public static String envPrefix() {
        return requiredProperty(ENV_PREFIX_KEY);
    }

    private static String requiredProperty(String key) {
        var value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required build metadata property: " + key);
        }
        return value;
    }

    private static Properties loadProperties() {
        var properties = new Properties();
        var input = BuildMetadata.class.getResourceAsStream(PROPERTIES_FILE);
        if (input == null) {
            throw new IllegalStateException("Missing build metadata resource");
        }
        try (input; var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Failed to load build metadata resource: " + PROPERTIES_FILE, exception);
        }
        return properties;
    }
}
