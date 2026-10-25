package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MessageTest {

    @Test
    void testConstructor() {
        // Arrange + Act + Assert
        assertNotNull(createMessage());
    }

    @ParameterizedTest()
    @MethodSource("getMessageComponents")
    void testConstructorNull(String name, int index) throws Exception {
        // Arrange
        var record = createMessage();
        var components = Message.class.getRecordComponents();
        var types = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
        var values = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            values[i] = components[i].getAccessor().invoke(record);
        }
        values[index] = null;
        var constructor = Message.class.getDeclaredConstructor(types);

        // Act
        var exception = assertThrows(InvocationTargetException.class, () -> constructor.newInstance(values));

        // Assert
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }

    private static Message createMessage() {
        return new Message(new MessageID(UUID.randomUUID()),
                new MessageTitle("hello"));
    }

    private static Stream<Arguments> getMessageComponents() {
        var components = Message.class.getRecordComponents();
        return IntStream.range(0, components.length).mapToObj(i -> Arguments.of(components[i].getName(), i));
    }
}
