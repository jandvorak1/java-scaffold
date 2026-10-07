<script setup lang="ts">

import "@ui5/webcomponents/dist/BusyIndicator.js";

import { computed, onBeforeUnmount, ref, watch } from "vue";

const counter = ref(0);
let bodyAriaBusyManaged = false;
let previousBodyAriaBusy: string | null = null;

const visible = computed(() => counter.value > 0);

watch(visible, updateBodyAriaBusy, { flush: "sync" });

onBeforeUnmount(restoreBodyAriaBusy);

/**
 * Requests the busy overlay and its animation without an indicator delay.
 *
 * Each call adds an active operation and must be balanced by a call to hide.
 */
function show(): void {
    counter.value++;
}

/**
 * Finishes one active operation registered by show.
 *
 * The overlay stays visible until all active operations finish.
 * Calling hide when no operation is active has no effect.
 */
function hide(): void {
    counter.value = Math.max(0, counter.value - 1);
}

/**
 * Keeps the busy overlay visible for the duration of an asynchronous operation.
 *
 * Releases the operation's reference on success or failure without hiding the
 * overlay while other operations are active. Errors propagate to the caller.
 *
 * @param operation Asynchronous work to execute while the overlay is requested.
 * @returns A promise that preserves the operation's result or rejection.
 */
async function run<T>(operation: () => Promise<T>): Promise<T> {
    show();
    try {
        return await operation();
    } finally {
        hide();
    }
}

function updateBodyAriaBusy(isVisible: boolean): void {
    if (isVisible && !bodyAriaBusyManaged) {
        previousBodyAriaBusy = document.body.getAttribute("aria-busy");
        document.body.setAttribute("aria-busy", "true");
        bodyAriaBusyManaged = true;
    } else if (!isVisible) {
        restoreBodyAriaBusy();
    }
}

function restoreBodyAriaBusy(): void {
    if (!bodyAriaBusyManaged) {
        return;
    }
    if (previousBodyAriaBusy === null) {
        document.body.removeAttribute("aria-busy");
    } else {
        document.body.setAttribute("aria-busy", previousBodyAriaBusy);
    }
    previousBodyAriaBusy = null;
    bodyAriaBusyManaged = false;
}


defineExpose({
    show,
    hide,
    run,
});

</script>


<template>
    <div class="app-busy-overlay" :hidden="!visible" role="presentation">
        <ui5-busy-indicator active delay="0" size="M" />
    </div>
</template>


<style scoped>
.app-busy-overlay {
    position: fixed;
    inset: 0;
    z-index: 10000;
    display: flex;
    align-items: center;
    justify-content: center;
}

.app-busy-overlay::before {
    content: "";
    position: absolute;
    inset: 0;
    background: var(--sapBlockLayer_Background);
    opacity: var(--sapBlockLayer_Opacity);
}

.app-busy-overlay ui5-busy-indicator {
    position: relative;
    z-index: 1;
}

.app-busy-overlay[hidden] {
    display: none;
}
</style>
