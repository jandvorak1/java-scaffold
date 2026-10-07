/**
 * Configures requests and failure handling for a backend availability monitor.
 * Values are captured when the monitor is created.
 */
export type HeartbeatOptions = {
    /**
     * Nonblank URL used for status requests.
     *
     * @default "/api/webtray/status"
     */
    statusUrl?: string;

    /**
     * Time between scheduled checks in milliseconds. Checks are skipped while
     * a request from the same monitoring run is still pending.
     * Must be finite and between 1 and 2147483647.
     *
     * @default 5000
     */
    intervalMs?: number;

    /**
     * Request timeout in milliseconds, including reading the response body.
     * Must be finite and between 1 and 2147483647.
     *
     * @default 2000
     */
    timeoutMs?: number;

    /**
     * Positive safe integer specifying how many consecutive failures stop a run.
     *
     * @default 3
     */
    maxFailures?: number;

    /**
     * Called once after a run stops because it reached the failure limit.
     * Manual stopping does not invoke this callback.
     */
    onFailure?: () => void;
};

/**
 * Controls a reusable monitor whose lifecycle is managed by its caller.
 */
export type Heartbeat = {
    /**
     * Starts a new run with an immediate check and a reset failure count.
     * Has no effect while monitoring is already active.
     */
    start(): void;

    /**
     * Stops scheduling checks, clears the request timeout, and aborts any
     * pending request. Repeated calls are safe. Call when the owner is disposed.
     */
    stop(): void;

    /**
     * Reports whether checks are currently scheduled, regardless of backend health.
     *
     * @returns True while monitoring is active, otherwise false.
     */
    isRunning(): boolean;
};

/**
 * Creates a stopped browser monitor for a backend status endpoint.
 *
 * Successful checks require a successful HTTP response containing a JSON object
 * with a string status equal to ok, ignoring surrounding whitespace and case.
 * Network errors, timeouts, unsuccessful HTTP responses, and invalid payloads
 * count as failures. Success resets the consecutive failure count.
 *
 * Reaching the failure limit stops the run before invoking onFailure. Results
 * from a stopped run cannot change the state of a subsequent run.
 *
 * @param options Endpoint, timing, and failure callback settings.
 * @returns Controls for starting, stopping, and inspecting the monitor.
 * @throws RangeError If a timer value or failure limit is outside its valid range.
 * @throws Error If the endpoint URL is blank.
 */
export function useHeartbeat(options: HeartbeatOptions = {}): Heartbeat {
    const statusUrl = options.statusUrl?.trim() ?? "/api/webtray/status";
    const intervalMs = options.intervalMs ?? 5000;
    const timeoutMs = options.timeoutMs ?? 2000;
    const maxFailures = options.maxFailures ?? 3;
    const onFailure = options.onFailure;

    if (!statusUrl) {
        throw new Error("Heartbeat status URL must not be blank.");
    }
    validateTimer(intervalMs, "Heartbeat interval");
    validateTimer(timeoutMs, "Heartbeat timeout");
    if (!Number.isSafeInteger(maxFailures) || maxFailures < 1) {
        throw new RangeError("Heartbeat failure limit must be a positive safe integer.");
    }

    let timer: number | null = null;
    let timeout: number | null = null;
    let controller: AbortController | null = null;
    let failedCount = 0;
    let generation = 0;

    function start(): void {
        if (timer !== null) {
            return;
        }
        failedCount = 0;
        timer = window.setInterval(() => {
            void check();
        }, intervalMs);
        void check();
    }

    function stop(): void {
        generation++;
        failedCount = 0;
        if (timer !== null) {
            window.clearInterval(timer);
            timer = null;
        }
        if (timeout !== null) {
            window.clearTimeout(timeout);
            timeout = null;
        }
        controller?.abort();
        controller = null;
    }

    async function check(): Promise<void> {
        if (timer === null || controller !== null) {
            return;
        }
        const currentGeneration = generation;
        const requestController = new AbortController();
        controller = requestController;
        const timeoutId = window.setTimeout(() => {
            requestController.abort();
        }, timeoutMs);
        timeout = timeoutId;
        try {
            const response = await fetch(statusUrl, {
                method: "GET",
                cache: "no-store",
                signal: requestController.signal,
                headers: {
                    "Accept": "application/json",
                },
            });
            if (!response.ok) {
                throw new Error(`Heartbeat request failed with HTTP status ${response.status}.`);
            }
            const data: unknown = await response.json();
            if (typeof data !== "object" || data === null || !("status" in data)
                || typeof data.status !== "string" || data.status.trim().toLowerCase() !== "ok") {
                throw new Error("Unexpected heartbeat response.");
            }
            if (generation === currentGeneration) {
                failedCount = 0;
            }
        } catch {
            if (generation !== currentGeneration) {
                return;
            }
            failedCount++;
            if (failedCount >= maxFailures) {
                stop();
                onFailure?.();
            }
        } finally {
            window.clearTimeout(timeoutId);
            if (generation === currentGeneration) {
                timeout = null;
                controller = null;
            }
        }
    }

    function isRunning(): boolean {
        return timer !== null;
    }

    return {
        start,
        stop,
        isRunning,
    };
}

function validateTimer(value: number, name: string): void {
    if (!Number.isFinite(value) || value < 1 || value > 2147483647) {
        throw new RangeError(`${name} must be between 1 and 2147483647 milliseconds.`);
    }
}
