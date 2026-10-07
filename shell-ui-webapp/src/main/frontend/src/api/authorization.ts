import { getConfig } from "./config";
import { text } from "./i18n";

/**
 * Indicates that the current user does not satisfy an authorization requirement.
 */
export class AuthorizationError extends Error {
    /**
     * Creates an authorization error with the supplied user-facing message.
     *
     * @param message Description of the failed authorization requirement.
     */
    constructor(message: string) {
        super(message);
        this.name = "AuthorizationError";
    }
}

/**
 * Determines whether the current user has a role with the given identifier.
 *
 * Role identifiers are compared exactly and are case-sensitive.
 *
 * @param role Identifier of the role to find.
 * @returns True when the current user has the role; otherwise false.
 */
export function hasRole(role: string): boolean {
    return getConfig().user.roles.includes(role);
}

/**
 * Determines whether the current user has a permission with the given identifier.
 *
 * Permission identifiers are compared exactly and are case-sensitive.
 *
 * @param permission Identifier of the permission to find.
 * @returns True when the current user has the permission; otherwise false.
 */
export function hasPermission(permission: string): boolean {
    return getConfig().user.permissions.includes(permission);
}

/**
 * Verifies that the current user has a required role.
 *
 * @param role Identifier of the required role.
 * @throws AuthorizationError When the current user does not have the role.
 */
export function requireRole(role: string): void {
    if (!hasRole(role)) {
        throw new AuthorizationError(text("API_AUTHORIZATION_ERROR_REQUIRE_ROLE", role));
    }
}

/**
 * Verifies that the current user has a required permission.
 *
 * @param permission Identifier of the required permission.
 * @throws AuthorizationError When the current user does not have the permission.
 */
export function requirePermission(permission: string): void {
    if (!hasPermission(permission)) {
        throw new AuthorizationError(text("API_AUTHORIZATION_ERROR_REQUIRE_PERMISSION", permission));
    }
}
