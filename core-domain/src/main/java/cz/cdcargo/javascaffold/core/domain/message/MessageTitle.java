package cz.cdcargo.javascaffold.core.domain.message;

import java.util.Objects;

/**
 * Immutable non-blank title of a message.
 *
 * The value contains no more than 255 Unicode code points and is retained
 * without modification.
 *
 * @param value title text
 */
public record MessageTitle(String value) {

    private static final int MAX_LENGTH = 255;

    /**
     * Creates a message title after validating value.
     *
     * @param value title text
     * @throws NullPointerException        if value is null
     * @throws MessageTitleBlankException  if value is blank
     * @throws MessageTitleLengthException if value contains more than 255 Unicode
     *                                     code points
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
