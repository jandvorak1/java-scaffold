<script setup lang="ts" generic="T extends Record<string, unknown>">

import "@ui5/webcomponents/dist/Dialog.js";
import "@ui5/webcomponents/dist/Toolbar.js";
import "@ui5/webcomponents/dist/ToolbarButton.js";

import { onUnmounted, ref, toRaw } from "vue";
import { text } from "../api/i18n";

/**
 * Defines the input properties accepted by the edit dialog.
 */
type EditDialogProps<T> = {

    /** Text displayed in the dialog header. */
    readonly title?: string;

    /**
     * Validates the edited model before the dialog is confirmed.
     *
     * Returning false or rejecting keeps the dialog open. The function may
     * perform asynchronous validation.
     */
    readonly validate?: (model: T) => boolean | Promise<boolean>;
};

const properties = defineProps<EditDialogProps<T>>();
const editModel = ref<T | null>(null);
const opened = ref(false);
const validating = ref(false);

let resolveDialog: ((model: T | null) => void) | null = null;
let dialogVersion = 0;

/**
 * Opens the dialog with a deep editable copy of a model.
 *
 * The supplied model is not modified. Confirmation resolves the returned
 * promise with another deep copy, while cancellation resolves it with null.
 * Opening a new model cancels the previously pending edit operation.
 *
 * @param model structured-cloneable model to edit
 * @returns a promise containing the edited model, or null after cancellation
 * @throws DOMException if the supplied model cannot be cloned
 */
function open(model: T): Promise<T | null> {
    const modelCopy = cloneModel(model);
    const previousResolve = resolveDialog;
    resolveDialog = null;
    previousResolve?.(null);
    dialogVersion++;
    editModel.value = modelCopy;
    opened.value = true;
    validating.value = false;
    const promise = new Promise<T | null>(resolve => {
        resolveDialog = resolve;
    });
    return promise;
}

function finish(model: T | null): void {
    const result = model === null ? null : cloneModel(model);
    const resolve = resolveDialog;
    dialogVersion++;
    resolveDialog = null;
    opened.value = false;
    validating.value = false;
    editModel.value = null;
    resolve?.(result);
}

async function onOkClick(): Promise<void> {
    const model = editModel.value;
    if (!model || validating.value) {
        return;
    }
    const version = dialogVersion;
    validating.value = true;
    try {
        const valid = await properties.validate?.(model) ?? true;
        if (dialogVersion !== version || editModel.value !== model) {
            return;
        }
        if (!valid) {
            return;
        }
        finish(model);
    } catch {
        // Validation failures keep the current dialog open.
    } finally {
        if (dialogVersion === version) {
            validating.value = false;
        }
    }
}

function onCancelClick(): void {
    finish(null);
}

function onDialogClose(): void {
    if (resolveDialog) {
        finish(null);
    }
}

onUnmounted(() => {
    const resolve = resolveDialog;
    dialogVersion++;
    resolveDialog = null;
    resolve?.(null);
});

function cloneModel(model: T): T {
    return structuredClone(toRaw(model));
}

defineExpose({
    open,
});
</script>


<template>
    <ui5-dialog :open.prop="opened" :headerText.prop="properties.title" @close="onDialogClose">
        <slot v-if="editModel" :model="editModel"></slot>
        <ui5-toolbar slot="footer">
            <ui5-toolbar-button design="Emphasized" :text.prop="text('COMPONENTS_EDIT_DIALOG_OK')"
                :disabled.prop="validating" @click="onOkClick">
            </ui5-toolbar-button>
            <ui5-toolbar-button design="Transparent" :text.prop="text('COMPONENTS_EDIT_DIALOG_CANCEL')"
                @click="onCancelClick">
            </ui5-toolbar-button>
        </ui5-toolbar>
    </ui5-dialog>
</template>


