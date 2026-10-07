<script setup lang="ts">

import "@ui5/webcomponents/dist/BusyIndicator.js";
import "@ui5/webcomponents/dist/Dialog.js";
import "@ui5/webcomponents/dist/Toolbar.js";
import "@ui5/webcomponents/dist/ToolbarButton.js";

import "@ui5/webcomponents-icons/dist/decline.js";

import { computed, onBeforeUnmount, ref } from "vue";
import { text } from "../api/i18n";
import { deleteJson, getJson, postJson } from "../api/http";

import type { JobMessage, JobResponseModel, JobState, } from "../api/jobs";

type JobStatusResponse = JobResponseModel & {
    readonly offset: number;
    readonly messages: readonly JobMessage[];
};

type JobCleanup = "DELETE" | "CANCEL_AND_DELETE";

const open = ref(false);
const messages = ref<string[]>([]);
const cancelDisabled = ref(false);
const cancellable = ref(true);
const token = ref<string | null>(null);
const interval = ref(1000);
const messageOffset = ref(0);

let timeout: number | null = null;
let pollVersion = 0;
let resolveResult: ((result: JobResponseModel) => void) | null = null;
let resultPromise: Promise<JobResponseModel> | null = null;

const lastMessage = computed(() =>
    messages.value[messages.value.length - 1] ?? ""
);

/**
 * Opens the dialog and monitors a background job until it reaches a terminal
 * state.
 *
 * Starting another job requests cancellation of the previously monitored
 * backend job and completes its promise with the cancelled state.
 *
 * @param jobToken non-blank token identifying the background job
 * @param jobCancellable whether the user may cancel the job; defaults to true
 * @param jobInterval positive polling interval in milliseconds; defaults to 1000
 * @returns a promise resolved with the terminal job status, or rejected when
 *          the token or polling interval is invalid
 */
function run(jobToken: string, jobCancellable: boolean = true, jobInterval: number = 1000): Promise<JobResponseModel> {
    const normalizedToken = jobToken.trim();
    if (!normalizedToken) {
        return Promise.reject(new Error(text("COMPONENTS_BUSY_DIALOG_TOKEN_ERROR")));
    }
    if (!Number.isFinite(jobInterval) || jobInterval <= 0) {
        return Promise.reject(new Error(text("COMPONENTS_BUSY_DIALOG_INTERVAL_ERROR")));
    }
    if (token.value === normalizedToken && resultPromise) {
        return resultPromise;
    }
    finishCurrentIfNeeded();
    token.value = normalizedToken;
    cancellable.value = jobCancellable;
    interval.value = jobInterval;
    messageOffset.value = 0;
    messages.value = [text("COMPONENTS_BUSY_DIALOG_PROCESSING")];
    cancelDisabled.value = false;
    open.value = true;
    const promise = new Promise<JobResponseModel>(resolve => {
        resolveResult = resolve;
        void poll();
    });
    resultPromise = promise;
    return promise;
}

async function poll(): Promise<void> {
    if (!token.value) {
        return;
    }
    const currentToken = token.value;
    const currentPollVersion = pollVersion;
    const currentOffset = messageOffset.value;

    try {
        const value = await getJson<unknown>(
            `/api/system/jobs/${encodeURIComponent(currentToken)}?offset=${currentOffset}`
        );
        // Ignore a response belonging to an obsolete polling operation.
        if (token.value !== currentToken || pollVersion !== currentPollVersion) {
            return;
        }
        if (!isJobStatusResponse(value)
            || value.token !== currentToken
            || value.offset < currentOffset
            || value.messages.length !== value.offset - currentOffset) {
            throw new Error(text("COMPONENTS_BUSY_DIALOG_STATUS_ERROR"));
        }
        const status = value;
        messageOffset.value = status.offset;
        applyMessages(status);
        if (!isFinalState(status.state)) {
            timeout = window.setTimeout(() => {
                void poll();
            }, interval.value);
            return;
        }
        finish(status);

    } catch (error) {
        // Ignore an error belonging to an obsolete polling operation.
        if (token.value !== currentToken || pollVersion !== currentPollVersion) {
            return;
        }
        const errorMessage = resolveErrorMessage(error, "COMPONENTS_BUSY_DIALOG_STATUS_ERROR");
        addMessage(errorMessage);
        finish({
            token: currentToken,
            state: "FAILED",
            error: {
                message: errorMessage,
            },
        }, "CANCEL_AND_DELETE");
    }
}

async function cancel(): Promise<void> {
    if (!token.value || cancelDisabled.value) {
        return;
    }
    const currentToken = token.value;
    invalidatePolling();
    const currentPollVersion = pollVersion;
    cancelDisabled.value = true;
    addMessage(text("COMPONENTS_BUSY_DIALOG_CANCELLING"));
    try {
        await postJson<void>(`/api/system/jobs/${encodeURIComponent(currentToken)}/cancel`, undefined);
        if (token.value !== currentToken || pollVersion !== currentPollVersion) {
            return;
        }
        await poll();
    } catch (error) {
        if (token.value !== currentToken || pollVersion !== currentPollVersion) {
            return;
        }
        const errorMessage = resolveErrorMessage(error, "COMPONENTS_BUSY_DIALOG_CANCEL_ERROR");
        addMessage(errorMessage);
        cancelDisabled.value = false;
        timeout = window.setTimeout(() => {
            void poll();
        }, interval.value);
    }
}

function finish(result: JobResponseModel, cleanup: JobCleanup = "DELETE"): void {
    invalidatePolling();

    const currentToken = token.value;
    const resolve = resolveResult;

    token.value = null;
    resolveResult = null;
    resultPromise = null;
    open.value = false;
    cancelDisabled.value = false;
    messageOffset.value = 0;
    resolve?.(result);

    if (currentToken && cleanup === "DELETE") {
        void deleteJob(currentToken);
    } else if (currentToken && cleanup === "CANCEL_AND_DELETE") {
        void cancelAndDeleteJob(currentToken);
    }
}

function finishCurrentIfNeeded(): void {
    if (!token.value) {
        return;
    }
    const currentToken = token.value;
    finish({
        token: currentToken,
        state: "CANCELLED",
        error: {
            message: text("COMPONENTS_BUSY_DIALOG_REPLACED"),
        },
    }, "CANCEL_AND_DELETE");
}


function applyMessages(status: JobResponseModel): void {
    for (const item of status.messages ?? []) {
        addMessage(item.message);
    }
}

function addMessage(message: string): void {
    const normalizedMessage = message.trim();
    if (!normalizedMessage) {
        return;
    }
    const previousMessage = messages.value[messages.value.length - 1];
    if (previousMessage === normalizedMessage) {
        return;
    }
    messages.value = [
        ...messages.value,
        normalizedMessage,
    ];
}

function invalidatePolling(): void {
    pollVersion++;
    clearTimer();
}

function clearTimer(): void {
    if (timeout !== null) {
        window.clearTimeout(timeout);
        timeout = null;
    }
}

function isFinalState(state: JobState): state is "DONE" | "FAILED" | "CANCELLED" {
    return (state === "DONE" || state === "FAILED" || state === "CANCELLED");
}

function isJobStatusResponse(value: unknown): value is JobStatusResponse {
    if (!isRecord(value)
        || typeof value.token !== "string"
        || !value.token.trim()
        || !isJobState(value.state)
        || typeof value.offset !== "number"
        || !Number.isSafeInteger(value.offset)
        || value.offset < 0
        || !Array.isArray(value.messages)
        || !value.messages.every(isJobMessage)) {
        return false;
    }
    return value.error === undefined
        || (isRecord(value.error) && typeof value.error.message === "string");
}

function isJobState(value: unknown): value is JobState {
    return value === "READY"
        || value === "RUNNING"
        || value === "DONE"
        || value === "FAILED"
        || value === "CANCELLED";
}

function isJobMessage(value: unknown): value is JobMessage {
    return isRecord(value) && typeof value.message === "string";
}

function isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === "object" && value !== null && !Array.isArray(value);
}

function resolveErrorMessage(error: unknown, fallbackKey: string): string {
    const message = error instanceof Error ? error.message.trim() : "";
    if (!message) {
        return text(fallbackKey);
    }
    return message;
}

async function deleteJob(jobToken: string): Promise<void> {
    try {
        await deleteJson<void>(`/api/system/jobs/${encodeURIComponent(jobToken)}`);
    } catch {
        // Cleanup failure is not critical for the UI flow.
    }
}

async function cancelAndDeleteJob(jobToken: string): Promise<void> {
    try {
        await postJson<void>(`/api/system/jobs/${encodeURIComponent(jobToken)}/cancel`, undefined);
    } catch {
        // Preserve a worker in the registry when cancellation fails.
        return;
    }
    await deleteJob(jobToken);
}

function onBeforeClose(event: Event): void {
    if (token.value) {
        event.preventDefault();
    }
}

onBeforeUnmount(() => {
    finishCurrentIfNeeded();
});

defineExpose({
    run,
});
</script>


<template>
    <ui5-dialog class="app-busy-box" :open.prop="open" :headerText.prop="text('COMPONENTS_BUSY_DIALOG_TITLE')"
        state="Information" @before-close="onBeforeClose">
        <div class="app-busy-box-content">
            <ui5-busy-indicator class="app-busy-box-busy-indicator" active size="M" tabindex="-1"
                :text.prop="lastMessage || text('COMPONENTS_BUSY_DIALOG_PROCESSING')" />
        </div>
        <ui5-toolbar v-if="cancellable" slot="footer" design="Transparent">
            <ui5-toolbar-button :text.prop="text('COMPONENTS_BUSY_DIALOG_CANCEL')" design="Default" icon="decline"
                :disabled.prop="cancelDisabled" @click="cancel" />
        </ui5-toolbar>
    </ui5-dialog>
</template>


<style scoped>
.app-busy-box-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}
</style>
