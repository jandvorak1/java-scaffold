package cz.cdcargo.javascaffold.shell.ui.webapp.platform.authentication;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.account.UserAccountStore;

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
}
