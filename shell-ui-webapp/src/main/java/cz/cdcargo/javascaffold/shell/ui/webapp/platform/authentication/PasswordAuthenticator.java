package cz.cdcargo.javascaffold.shell.ui.webapp.platform.authentication;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.account.UserAccount;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.account.UserAccountStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthenticationException;

public final class PasswordAuthenticator {

    private final UserAccountStore accountStore;
    private final PasswordCredentialStore credentialStore;
    private final PasswordVerifier passwordVerifier;

    public PasswordAuthenticator(UserAccountStore accountStore, PasswordCredentialStore credentialStore,
            PasswordVerifier passwordVerifier) {

        this.accountStore = Objects.requireNonNull(accountStore, "User account store must not be null");
        this.credentialStore = Objects.requireNonNull(credentialStore, "Password credential store must not be null");
        this.passwordVerifier = Objects.requireNonNull(passwordVerifier, "Password verifier must not be null");
    }

    public UserAccount authenticate(String email, String password) {
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(password, "Password must not be null");

        if (email.isBlank() || password.isBlank()) {
            throw new AuthenticationException();
        }

        var account = accountStore.findByEmail(email)
                .filter(UserAccount::isActive)
                .orElseThrow(AuthenticationException::new);
        var credential = credentialStore
                .findBySubject(account.subject())
                .orElseThrow(AuthenticationException::new);

        if (!passwordVerifier.matches(password, credential.passwordHash())) {
            throw new AuthenticationException();
        }

        return account;
    }
}
