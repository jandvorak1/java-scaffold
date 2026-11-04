package cz.cdcargo.javascaffold.core.application.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GenerateGreetingMessageServiceTest {

    @Test
    void testConstructor() {
        // Arrange
        var service = new GenerateGreetingMessageService();

        // Act + Assert
        assertEquals("Hello World!", service.execute("World").title().value());
    }

    @Test
    void testConstructorNull() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new GenerateGreetingMessageService(null));
    }

    @Test
    void testExecute() {
        // Arrange
        var title = "World";
        var service = new GenerateGreetingMessageService();

        // Act
        var result = service.execute("World");

        // Assert
        assertEquals("Hello " + title + "!", result.title().value());
    }
}
