package cz.cdcargo.javascaffold.core.domain.message;

import java.util.Objects;

/**
 * Represents an immutable, non-blank message title.
 *
 * A title contains at most 255 Unicode code points. Its supplied text is
 * preserved without modification.
 *
 * @param value validated message title
 */
public record MessageTitle(String value) {

    private static final int MAX_LENGTH = 255;

    /**
     * Creates a validated message title.
     *
     * @param value message title
     * @throws NullPointerException        if value is null
     * @throws MessageTitleBlankException  if value is blank
     * @throws MessageTitleLengthException if value exceeds 255 Unicode code
     *                                     points
     */
    public MessageTitle {
        Objects.requireNonNull(value, "Value must not be null");
        if (value.isBlank()) {
            throw new MessageTitleBlankException();
        }
        if (value.codePointCount(0, value.length()) > MAX_LENGTH) {
            throw new MessageTitleLengthException(MAX_LENGTH);
        }
    }
}
