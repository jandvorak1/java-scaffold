package cz.cdcargo.javascaffold.core.application.message;

import cz.cdcargo.javascaffold.core.domain.message.Message;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitleLengthException;

/**
 * Defines the inbound application boundary for creating a greeting message.
 *
 * Each invocation creates a message with a generated identifier from the
 * supplied title.
 */
@FunctionalInterface
public interface GenerateGreetingMessageUseCase {

    /**
     * Creates a greeting message from a title.
     *
     * @param title title used to create the greeting message
     * @return new message with a generated identifier and greeting title
     * @throws NullPointerException        if title is null
     * @throws MessageTitleLengthException if the generated greeting exceeds the
     *                                     title length limit
     */
    Message execute(String title);
}
