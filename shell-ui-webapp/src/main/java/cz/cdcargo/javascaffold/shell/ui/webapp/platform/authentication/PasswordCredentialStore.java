package cz.cdcargo.javascaffold.shell.ui.webapp.platform.authentication;

import java.util.Optional;

public interface PasswordCredentialStore {

    Optional<PasswordCredential> findBySubject(String subject);

}
