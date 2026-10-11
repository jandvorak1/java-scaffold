package cz.cdcargo.javascaffold.shell.ui.webapp.platform.password;

@FunctionalInterface
public interface PasswordHasher {

    String hash(String password);
}
