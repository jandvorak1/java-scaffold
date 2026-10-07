package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Defines and resolves the regional time zones offered by the application.
 *
 * A supported identifier consists of a recognized region and exactly one city
 * segment. Fixed offsets, aliases without a region, and identifiers containing
 * additional path segments are intentionally excluded.
 */
public final class TimeZones {

    /**
     * The default application time zone.
     */
    public static final ZoneId DEFAULT = ZoneId.of("Europe/Prague");
    private static final Set<ZoneId> SUPPORTED = loadSupported();

    private TimeZones() {
    }

    /**
     * Returns the supported time zones in an immutable set.
     *
     * @return supported time zones
     */
    public static Set<ZoneId> supported() {
        return SUPPORTED;
    }

    /**
     * Checks whether a time zone is supported by the application.
     *
     * @param zoneId time zone to check
     * @return true if the time zone is supported, otherwise false
     * @throws NullPointerException if zoneId is null
     */
    public static boolean isSupported(ZoneId zoneId) {
        Objects.requireNonNull(zoneId, "ZoneId must not be null");
        return SUPPORTED.contains(zoneId);
    }

    /**
     * Resolves an unsupported time zone to the default time zone.
     *
     * @param zoneId time zone to resolve
     * @return the supplied time zone when supported, otherwise the default time
     *         zone
     * @throws NullPointerException if zoneId is null
     */
    public static ZoneId resolve(ZoneId zoneId) {
        Objects.requireNonNull(zoneId, "ZoneId must not be null");
        return SUPPORTED.contains(zoneId) ? zoneId : DEFAULT;
    }

    /**
     * Returns supported time zones sorted by their identifiers.
     *
     * @return sorted supported time zones
     */
    public static List<ZoneId> supportedSorted() {
        return SUPPORTED.stream().sorted(Comparator.comparing(ZoneId::getId)).toList();
    }

    private static Set<ZoneId> loadSupported() {
        return ZoneId.getAvailableZoneIds().stream().filter(TimeZones::isSupportedRegion)
                .filter(TimeZones::hasSingleSegment).map(ZoneId::of)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static boolean isSupportedRegion(String id) {
        return id.startsWith("Africa/") || id.startsWith("America/") || id.startsWith("Antarctica/")
                || id.startsWith("Arctic/") || id.startsWith("Asia/") || id.startsWith("Australia/")
                || id.startsWith("Europe/") || id.startsWith("Indian/") || id.startsWith("Pacific/");
    }

    private static boolean hasSingleSegment(String id) {
        return id.indexOf('/') == id.lastIndexOf('/');
    }
}
