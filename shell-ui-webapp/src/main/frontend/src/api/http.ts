import { getCsrfToken } from "./csrf";
import { text } from "./i18n";

/**
 * Sends a GET request and parses its successful JSON response.
 *
 * @param url URL of the resource to retrieve.
 * @returns The parsed response body.
 * @throws Error When the request fails or the response cannot be processed.
 */
export async function getJson<TResponse>(url: string): Promise<TResponse> {
    const response = await fetchResponse(url, {
        method: "GET",
        cache: "no-store",
        headers: {
            "Accept": "application/json",
        },
    });
    return readJsonResponse<TResponse>(response);
}

/**
 * Sends a POST request containing JSON and parses its successful JSON response.
 *
 * @param url URL of the resource to update.
 * @param body Value serialized as the request body.
 * @returns The parsed response body, or undefined for a response without content.
 * @throws Error When serialization or the request fails, or the response cannot be processed.
 */
export async function postJson<TResponse>(url: string, body: unknown): Promise<TResponse> {
    const response = await fetchResponse(url, {
        method: "POST",
        cache: "no-store",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json",
            "X-CSRF-Token": getCsrfToken(),
        },
        body: JSON.stringify(body),
    });
    return readJsonResponse<TResponse>(response);
}

/**
 * Sends a DELETE request and parses its successful JSON response.
 *
 * @param url URL of the resource to delete.
 * @returns The parsed response body, or undefined for a response without content.
 * @throws Error When the request fails or the response cannot be processed.
 */
export async function deleteJson<TResponse>(url: string): Promise<TResponse> {
    const response = await fetchResponse(url, {
        method: "DELETE",
        cache: "no-store",
        headers: {
            "Accept": "application/json",
            "X-CSRF-Token": getCsrfToken(),
        },
    });
    return readJsonResponse<TResponse>(response);
}

async function fetchResponse(url: string, init: RequestInit): Promise<Response> {
    try {
        return await fetch(url, init);
    } catch {
        throw new Error(text("API_HTTP_ERROR_REQUEST"));
    }
}

async function readJsonResponse<TResponse>(response: Response,): Promise<TResponse> {
    if (!response.ok) {
        throw await readErrorResponse(response);
    }
    if (response.status === 204 || response.status === 205) {
        return undefined as TResponse;
    }
    try {
        return await response.json() as TResponse;
    } catch {
        throw new Error(text("API_HTTP_ERROR_REQUEST"));
    }
}

async function readErrorResponse(response: Response): Promise<Error> {
    try {
        const body = await response.json() as unknown;
        const message = getErrorMessage(body);
        return new Error(message ?? text("API_HTTP_ERROR_REQUEST"));
    } catch {
        return new Error(text("API_HTTP_ERROR_REQUEST"));
    }
}

function getErrorMessage(value: unknown): string | null {
    if (!isRecord(value) || !isRecord(value.error) || typeof value.error.message !== "string") {
        return null;
    }
    const message = value.error.message.trim();
    if (!message) {
        return null;
    }
    return message;
}

function isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === "object" && value !== null && !Array.isArray(value);
}
