<script setup lang="ts">

import "@ui5/webcomponents/dist/Dialog.js";
import "@ui5/webcomponents/dist/Text.js";
import "@ui5/webcomponents/dist/Toolbar.js";
import "@ui5/webcomponents/dist/ToolbarButton.js";

import { ref } from "vue";
import { text } from "../api/i18n";

/**
 * Identifies a visual message-box design supported by the UI5 dialog.
 */
export type MessageBoxDesign = "None" | "Information" | "Positive" | "Negative" | "Critical";

/**
 * Defines the content and behavior of a message box.
 */
export interface MessageBoxOptions {

    /** Text displayed in the dialog header. */
    readonly header: string;

    /** Text displayed in the dialog body. */
    readonly message: string;

    /**
     * Visual design of the dialog.
     *
     * @default "None"
     */
    readonly design?: MessageBoxDesign;

    /**
     * Whether the user is allowed to close the dialog.
     *
     * @default true
     */
    readonly closeable?: boolean;
}

type MessageBoxState = Readonly<{
    header: string;
    message: string;
    design: MessageBoxDesign;
    closeable: boolean;
}>;

const open = ref(false);
const header = ref("");
const message = ref("");
const design = ref<MessageBoxDesign>("None");
const closeable = ref(true);
const forceClosing = ref(false);
let closing = false;
let pendingState: MessageBoxState | null = null;

/**
 * Displays a message box using the supplied content and behavior.
 *
 * A currently open message is replaced immediately. If the dialog is already
 * closing, the new message is displayed after the close transition finishes.
 *
 * @param options message-box configuration
 */
function show(options: MessageBoxOptions): void {
    const state = createState(options);
    if (closing) {
        pendingState = state;
        return;
    }
    applyState(state);
}

function createState(options: MessageBoxOptions): MessageBoxState {
    return {
        header: options.header,
        message: options.message,
        design: options.design ?? "None",
        closeable: options.closeable !== false,
    };
}

function applyState(state: MessageBoxState): void {
    header.value = state.header;
    message.value = state.message;
    design.value = state.design;
    closeable.value = state.closeable;
    forceClosing.value = false;
    open.value = true;
}

/**
 * Displays a closeable message with positive visual emphasis.
 *
 * @param header dialog header
 * @param message text to display
 */
function success(header: string, message: string): void {
    show({ header, message, design: "Positive", });
}

/**
 * Displays a closeable informational message.
 *
 * @param header dialog header
 * @param message text to display
 */
function info(header: string, message: string): void {
    show({ header, message, design: "Information", });
}

/**
 * Displays a closeable message with critical visual emphasis.
 *
 * @param header dialog header
 * @param message text to display
 */
function warning(header: string, message: string): void {
    show({ header, message, design: "Critical" });
}

/**
 * Displays a closeable message with negative visual emphasis.
 *
 * @param header dialog header
 * @param message text to display
 */
function error(header: string, message: string): void {
    show({ header, message, design: "Negative" });
}

/**
 * Displays a fatal message that cannot be closed by the user.
 *
 * The dialog remains open until it is replaced or forceClose is called.
 *
 * @param header dialog header
 * @param message text to display
 */
function fatal(header: string, message: string): void {
    show({ header, message, design: "Negative", closeable: false });
}

/**
 * Starts closing the message box when user closing is permitted.
 */
function close(): void {
    if (!open.value || closing || (!closeable.value && !forceClosing.value)) {
        return;
    }
    open.value = false;
}

/**
 * Starts closing the message box regardless of its user-closeable setting.
 */
function forceClose(): void {
    pendingState = null;
    if (!open.value || closing) {
        return;
    }
    forceClosing.value = true;
    open.value = false;
}

/**
 * Determines whether the message box is currently displayed.
 *
 * @returns true while the dialog is displayed, otherwise false
 */
function isOpen(): boolean {
    return open.value;
}

function onBeforeClose(event: Event): void {
    if (!closeable.value && !forceClosing.value) {
        event.preventDefault();
        return;
    }
    closing = true;
}

function onClose(): void {
    open.value = false;
    forceClosing.value = false;
    closing = false;
    const state = pendingState;
    pendingState = null;
    if (state) {
        applyState(state);
    }
}

defineExpose({
    show,
    success,
    info,
    warning,
    error,
    fatal,
    close,
    forceClose,
    isOpen,
});
</script>


<template>
    <ui5-dialog :open.prop="open" :headerText.prop="header" :state.prop="design" @before-close="onBeforeClose"
        @close="onClose">
        <ui5-text> {{ message }} </ui5-text>
        <ui5-toolbar v-if="closeable" slot="footer">
            <ui5-toolbar-button design="Emphasized" :text.prop="text('COMPONENTS_MESSAGE_BOX_BUTTON')" @click="close" />
        </ui5-toolbar>
    </ui5-dialog>
</template>
