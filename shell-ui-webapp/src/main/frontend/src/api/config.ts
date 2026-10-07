/**
 * Describes the authenticated user included in the application configuration.
 */
export type User = Readonly<{
    subject: string;
    roles: readonly string[];
    permissions: readonly string[];
}>;

/**
 * Describes the application configuration embedded by the backend.
 */
export type Config = Readonly<{
    name: string;
    version: string;
    build: string;
    csrfToken: string;
    locale: string;
    timeZone: string;
    supportedLocales: readonly string[];
    decimalFormat: string;
    dateFormat: string;
    timeFormat: string;
    dateTimeFormat: string;
    user: User;
}>;

let cachedConfig: Config | null = null;

/**
 * Returns the validated application configuration embedded in the document.
 *
 * The configuration is parsed and validated on the first call. Subsequent calls
 * return the cached instance.
 *
 * @returns The application configuration supplied by the backend.
 * @throws Error When the configuration element is missing or its content is invalid.
 */
export function getConfig(): Config {
    if (cachedConfig !== null) {
        return cachedConfig;
    }
    const element = document.querySelector<HTMLScriptElement>(
        "script[data-app-config]",
    );
    if (!element) {
        throw new Error("Application configuration element is missing.");
    }
    const json = element.textContent?.trim();
    if (!json) {
        throw new Error("Application configuration is empty.");
    }
    let parsed: unknown;
    try {
        parsed = JSON.parse(json) as unknown;
    } catch {
        throw new Error("Application configuration contains invalid JSON.");
    }
    if (!isConfig(parsed)) {
        throw new Error("Application configuration has an invalid structure.");
    }
    cachedConfig = parsed;
    return cachedConfig;
}

function isConfig(value: unknown): value is Config {
    if (!isRecord(value)) {
        return false;
    }
    return typeof value.name === "string"
        && typeof value.version === "string"
        && typeof value.build === "string"
        && typeof value.csrfToken === "string"
        && typeof value.locale === "string"
        && typeof value.timeZone === "string"
        && isStringArray(value.supportedLocales)
        && typeof value.decimalFormat === "string"
        && typeof value.dateFormat === "string"
        && typeof value.timeFormat === "string"
        && typeof value.dateTimeFormat === "string"
        && isUser(value.user);
}

function isUser(value: unknown): value is User {
    return isRecord(value)
        && typeof value.subject === "string"
        && isStringArray(value.roles)
        && isStringArray(value.permissions);
}

function isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isStringArray(value: unknown): value is string[] {
    return Array.isArray(value) && value.every(item => typeof item === "string");
}
