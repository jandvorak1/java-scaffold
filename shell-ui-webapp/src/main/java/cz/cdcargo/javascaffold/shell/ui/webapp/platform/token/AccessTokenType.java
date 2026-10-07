package cz.cdcargo.javascaffold.shell.ui.webapp.platform.token;

/**
 * Identifies the authentication channel in which an access token is valid.
 */
public enum AccessTokenType {
    /**
     * Token transported in the application session cookie.
     */
    SESSION,
    /**
     * Token transported through bearer authentication.
     */
    BEARER
}
