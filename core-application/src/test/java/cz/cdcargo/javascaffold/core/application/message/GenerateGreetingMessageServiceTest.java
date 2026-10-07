package cz.cdcargo.javascaffold.core.application.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.domain.message.MessageTitleLengthException;

class GenerateGreetingMessageServiceTest {

    @Test
    void testExecuteCreatesGreetingWithGeneratedIdAndPreservedInput() {
        var id = UUID.fromString("a9f5ca4f-18d1-431d-a549-cf32a8ac806f");
        var service = new GenerateGreetingMessageService(() -> id);

        var result = service.execute("  John  ");

        assertEquals(id, result.id().value());
        assertEquals("Hello   John  !", result.title().value());
    }

    @Test
    void testExecuteRejectsNullTitleWithoutGeneratingId() {
        var generatorCalls = new AtomicInteger();
        var service = createService(generatorCalls);

        assertThrows(NullPointerException.class, () -> service.execute(null));
        assertEquals(0, generatorCalls.get());
    }

    @Test
    void testExecutePreservesBlankInputInGreeting() {
        var generatorCalls = new AtomicInteger();
        var service = createService(generatorCalls);

        var result = service.execute(" \t");

        assertEquals("Hello  \t!", result.title().value());
        assertEquals(1, generatorCalls.get());
    }

    @Test
    void testExecuteRejectsLongTitleWithoutGeneratingId() {
        var generatorCalls = new AtomicInteger();
        var service = createService(generatorCalls);

        assertThrows(MessageTitleLengthException.class,
                () -> service.execute("a".repeat(249)));
        assertEquals(0, generatorCalls.get());
    }

    @Test
    void testExecuteAcceptsMaximumLengthGreetingWithSupplementaryCharacters() {
        var title = "\uD83D\uDE00".repeat(248);
        var generatorCalls = new AtomicInteger();
        var service = createService(generatorCalls);

        var result = service.execute(title);

        assertEquals("Hello " + title + "!", result.title().value());
        assertEquals(1, generatorCalls.get());
    }

    @Test
    void testExecuteCreatesGreetingFromEmptyInput() {
        var generatorCalls = new AtomicInteger();
        var service = createService(generatorCalls);

        var result = service.execute("");

        assertEquals("Hello !", result.title().value());
        assertEquals(1, generatorCalls.get());
    }

    @Test
    void testConstructorUsesRandomIdGenerator() {
        var result = new GenerateGreetingMessageService().execute("John");

        assertNotNull(result.id().value());
        assertEquals("Hello John!", result.title().value());
    }

    @Test
    void testConstructorRejectsNullIdGenerator() {
        assertThrows(NullPointerException.class,
                () -> new GenerateGreetingMessageService(null));
    }

    @Test
    void testExecuteRejectsNullGeneratedId() {
        var service = new GenerateGreetingMessageService(() -> null);

        assertThrows(NullPointerException.class, () -> service.execute("John"));
    }

    private GenerateGreetingMessageService createService(AtomicInteger generatorCalls) {
        return new GenerateGreetingMessageService(() -> {
            generatorCalls.incrementAndGet();
            return UUID.randomUUID();
        });
    }
}
