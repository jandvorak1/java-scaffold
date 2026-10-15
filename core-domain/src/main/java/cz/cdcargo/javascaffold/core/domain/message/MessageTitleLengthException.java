package cz.cdcargo.javascaffold.core.domain.message;

import java.io.Serial;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;

/**
 * Business exception thrown when a message title exceeds its permitted length.
 */
public final class MessageTitleLengthException extends AbstractBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int maxLength;

    /**
     * Creates an exception for a title longer than maxLength Unicode code points.
     *
     * @param maxLength permitted maximum number of Unicode code points
     * @throws IllegalArgumentException if maxLength is negative
     */
    public MessageTitleLengthException(int maxLength) {
        super("Maximum length must not exceed " + maxLength + " characters");
        if (maxLength < 0) {
            throw new IllegalArgumentException("Maximum length must not be negative");
        }
        this.maxLength = maxLength;
    }

    /**
     * Returns the permitted maximum number of Unicode code points.
     *
     * @return permitted title length
     */
    public int maxLength() {
        return maxLength;
    }
}
