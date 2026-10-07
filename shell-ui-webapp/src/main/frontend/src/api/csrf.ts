import { getConfig } from "./config";

/**
 * Returns the CSRF token provided in the application configuration.
 *
 * @returns The non-blank token used to protect HTTP requests.
 * @throws Error When the configured token is blank.
 */
export function getCsrfToken(): string {
    const token = getConfig().csrfToken;
    if (!token.trim()) {
        throw new Error("CSRF token is empty.");
    }
    return token;
}
