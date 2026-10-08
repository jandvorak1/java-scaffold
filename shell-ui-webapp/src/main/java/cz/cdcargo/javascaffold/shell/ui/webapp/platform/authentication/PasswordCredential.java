package cz.cdcargo.javascaffold.shell.ui.webapp.platform.authentication;

import java.util.Objects;

public record PasswordCredential(String subject, String passwordHash) {

    public PasswordCredential {
        Objects.requireNonNull(subject, "Subject must not be null");
        Objects.requireNonNull(passwordHash, "Password hash must not be null");

        if (subject.isBlank()) {
            throw new IllegalArgumentException("Subject must not be blank");
        }
        if (passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
    }
}
