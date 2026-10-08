package cz.cdcargo.javascaffold.shell.ui.webapp.platform.authentication;

@FunctionalInterface
public interface PasswordVerifier {

    boolean matches(String password, String passwordHash);

}
