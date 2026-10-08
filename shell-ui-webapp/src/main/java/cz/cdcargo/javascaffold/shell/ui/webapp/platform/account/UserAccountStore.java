package cz.cdcargo.javascaffold.shell.ui.webapp.platform.account;

import java.util.Optional;

public interface UserAccountStore {

    Optional<UserAccount> findByEmail(String email);

}
