import { text } from "./i18n";

/**
 * Identifies a lifecycle state reported by a background job.
 */
export type JobState = "READY" | "RUNNING" | "DONE" | "FAILED" | "CANCELLED";

/**
 * Contains a user-facing progress message reported by a background job.
 */
export type JobMessage = {
    readonly message: string;
};

/**
 * Contains a user-facing error reported by a failed background job.
 */
export type JobError = {
    readonly message: string;
};

/**
 * Describes the current state, incremental output, and optional result of a
 * background job.
 *
 * The offset and messages are present in status responses but may be absent in
 * responses that only create a job. A result is available only after successful
 * completion, while an error is available for a failed job.
 */
export type JobResponseModel = {
    readonly token: string;
    readonly state: JobState;
    readonly offset?: number;
    readonly messages?: readonly JobMessage[];
    readonly result?: unknown;
    readonly error?: JobError;
};

/**
 * Verifies that a background job reached successful completion.
 *
 * @param response response to verify
 * @throws Error if the job is not complete; the reported error message is used
 *               when it is not blank, otherwise a localized fallback is used
 */
export function requireJobSucceeded(response: JobResponseModel): void {
    if (response.state !== "DONE") {
        throw new Error(resolveErrorMessage(response.error));
    }
}

/**
 * Verifies successful completion and returns the background job result.
 *
 * The caller specifies the expected result type because the HTTP response does
 * not carry runtime type information.
 *
 * @param response response to verify
 * @returns the completed job result interpreted as the requested type
 * @throws Error if the job is not complete or does not contain a result
 */
export function requireJobResult<T>(response: JobResponseModel): T {
    requireJobSucceeded(response);
    if (response.result === undefined || response.result === null) {
        throw new Error(text("API_JOBS_ERROR_MESSAGE"));
    }
    return response.result as T;
}

function resolveErrorMessage(error: JobError | undefined): string {
    const message = error?.message.trim();
    if (!message) {
        return text("API_JOBS_ERROR_MESSAGE");
    }
    return message;
}
