package cz.cdcargo.javascaffold.shell.ui.webapp.platform.password;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryPasswordCredentialStore implements PasswordCredentialStore {

    private final Map<String, PasswordCredential> credentials;

    public InMemoryPasswordCredentialStore() {
        this(java.util.List.of());
    }

    public InMemoryPasswordCredentialStore(Collection<PasswordCredential> credentials) {
        Objects.requireNonNull(credentials, "Credentials must not be null");

        var map = new HashMap<String, PasswordCredential>();
        for (var credential : credentials) {
            Objects.requireNonNull(credential, "Credential must not be null");
            if (map.putIfAbsent(credential.subject(), credential) != null) {
                throw new IllegalArgumentException("Duplicate credential subject: " + credential.subject());
            }
        }
        this.credentials = Map.copyOf(map);
    }

    @Override
    public Optional<PasswordCredential> findBySubject(String subject) {
        Objects.requireNonNull(subject, "Subject must not be null");

        if (subject.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(credentials.get(subject));
    }
}
