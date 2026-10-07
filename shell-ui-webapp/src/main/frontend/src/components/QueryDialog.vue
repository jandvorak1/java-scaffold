<script setup lang="ts">

import "@ui5/webcomponents/dist/Dialog.js";
import "@ui5/webcomponents/dist/Text.js";
import "@ui5/webcomponents/dist/Toolbar.js";
import "@ui5/webcomponents/dist/ToolbarButton.js";

import { onUnmounted, ref } from "vue";
import { text } from "../api/i18n";

type PendingQuery = Readonly<{
    header: string;
    message: string;
    resolve: (result: boolean) => void;
}>;

const open = ref(false);
const header = ref("");
const message = ref("");

let resolveQuery: ((result: boolean) => void) | null = null;
let closing = false;
let pendingQuery: PendingQuery | null = null;

/**
 * Displays a confirmation question and waits for the user's answer.
 *
 * Selecting Yes resolves the returned promise with true. Selecting No,
 * closing the dialog, or pressing Escape resolves it with false. A new query
 * replaces an open query. During a close transition, the latest query is
 * queued and any previously queued query is resolved with false.
 *
 * @param queryHeader text displayed in the dialog header
 * @param queryMessage question displayed in the dialog body
 * @returns a promise resolved with true only when the user selects Yes
 */
function show(queryHeader: string, queryMessage: string): Promise<boolean> {
    return new Promise<boolean>(resolve => {
        const query: PendingQuery = {
            header: queryHeader,
            message: queryMessage,
            resolve,
        };
        if (closing) {
            pendingQuery?.resolve(false);
            pendingQuery = query;
            return;
        }
        activateQuery(query);
    });
}

function activateQuery(query: PendingQuery): void {
    const previousResolve = resolveQuery;
    resolveQuery = null;
    previousResolve?.(false);
    header.value = query.header;
    message.value = query.message;
    resolveQuery = query.resolve;
    open.value = true;
}

function finish(result: boolean): void {
    const resolve = resolveQuery;
    resolveQuery = null;
    open.value = false;
    resolve?.(result);
}

function onYesClick(): void {
    finish(true);
}

function onNoClick(): void {
    finish(false);
}

function onBeforeClose(): void {
    closing = true;
}

function onClose(): void {
    const resolve = resolveQuery;
    resolveQuery = null;
    open.value = false;
    closing = false;
    resolve?.(false);

    const query = pendingQuery;
    pendingQuery = null;
    if (query) {
        activateQuery(query);
    }
}

onUnmounted(() => {
    const resolve = resolveQuery;
    const pendingResolve = pendingQuery?.resolve;
    resolveQuery = null;
    pendingQuery = null;
    resolve?.(false);
    pendingResolve?.(false);
});

defineExpose({
    show,
});
</script>


<template>
    <ui5-dialog :open.prop="open" :headerText.prop="header" @before-close="onBeforeClose" @close="onClose">
        <ui5-text> {{ message }} </ui5-text>
        <ui5-toolbar slot="footer">
            <ui5-toolbar-button design="Emphasized" :text.prop="text('COMPONENTS_QUERY_DIALOG_YES')"
                @click="onYesClick" />
            <ui5-toolbar-button :text.prop="text('COMPONENTS_QUERY_DIALOG_NO')" @click="onNoClick" />
        </ui5-toolbar>
    </ui5-dialog>
</template>
