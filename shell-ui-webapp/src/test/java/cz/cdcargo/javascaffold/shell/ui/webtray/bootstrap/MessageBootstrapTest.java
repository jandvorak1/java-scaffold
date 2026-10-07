package cz.cdcargo.javascaffold.shell.ui.webapp.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.application.message.GenerateGreetingMessageService;
import cz.cdcargo.javascaffold.core.application.message.ReadAllMessageService;
import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;

class MessageBootstrapTest {

    @TempDir
    Path tempDir;

    @Test
    void testMessageBootstrapConstructorWithNullPreferences() {
        var exception = assertThrows(NullPointerException.class, () -> new MessageBootstrap(null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }

    @Test
    void testBuildGenerateGreetingMessageUseCaseReturnsGenerateGreetingMessageService() {
        var bootstrap = createBootstrap();

        var useCase = bootstrap.buildGenerateGreetingMessageUseCase();

        assertInstanceOf(GenerateGreetingMessageService.class, useCase);
    }

    @Test
    void testBuildGenerateGreetingMessageUseCaseGeneratesGreetingMessage() {
        var bootstrap = createBootstrap();

        var message = bootstrap.buildGenerateGreetingMessageUseCase().execute("Jane Doe");

        assertNotNull(message.id());
        assertEquals("Hello Jane Doe!", message.title().value());
    }

    @Test
    void testBuildReadAllMessageUseCaseReturnsReadAllMessageService() {
        var bootstrap = createBootstrap();

        var useCase = bootstrap.buildReadAllMessageUseCase();

        assertInstanceOf(ReadAllMessageService.class, useCase);
    }

    private MessageBootstrap createBootstrap() {
        return new MessageBootstrap(FilePreferencesStore.at(tempDir.resolve("preferences")));
    }
}
