package cz.cdcargo.javascaffold.core.domain.message;

import java.io.Serial;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;

/**
 * Signals that a message title exceeds its maximum number of Unicode code
 * points.
 */
public final class MessageTitleLengthException extends AbstractBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int maxLength;

    /**
     * Creates an exception describing a title that exceeds its length limit.
     *
     * @param maxLength maximum permitted number of Unicode code points
     * @throws IllegalArgumentException if maxLength is negative
     */
    public MessageTitleLengthException(int maxLength) {
        super(createMessage(maxLength));
        this.maxLength = maxLength;
    }

    /**
     * Returns the maximum title length that was exceeded.
     *
     * @return maximum permitted number of Unicode code points
     */
    public int maxLength() {
        return maxLength;
    }

    private static String createMessage(int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("Max length must not be negative");
        }
        return "Message title length must not exceed " + maxLength;
    }
}
