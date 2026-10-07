package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class MessageIDTest {

    @Test
    void testConstructorStoresValue() {
        var value = UUID.fromString("948becc5-322f-4eaf-bb20-4ffce6637e0f");
        var messageId = new MessageID(value);

        assertEquals(value, messageId.value());
    }

    @Test
    void testConstructorRejectsNullValue() {
        var exception = assertThrows(NullPointerException.class, () -> new MessageID(null));

        assertEquals("Value must not be null", exception.getMessage());
    }
}
