package cz.cdcargo.javascaffold.shell.ui.webapp.platform.password;

@FunctionalInterface
public interface PasswordVerifier {

    boolean matches(String password, String passwordHash);
}
