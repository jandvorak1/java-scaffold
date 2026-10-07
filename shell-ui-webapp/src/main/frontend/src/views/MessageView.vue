<script setup lang="ts">

import "@ui5/webcomponents/dist/DateRangePicker.js";
import "@ui5/webcomponents/dist/Input.js";
import "@ui5/webcomponents/dist/Label.js";
import "@ui5/webcomponents/dist/Link.js";
import "@ui5/webcomponents/dist/MessageStrip.js";
import "@ui5/webcomponents/dist/ToolbarButton.js";
import "@ui5/webcomponents/dist/ToolbarItem.js";
import "@ui5/webcomponents/dist/ToolbarSeparator.js";

import "@ui5/webcomponents-icons/dist/refresh.js";
import "@ui5/webcomponents-icons/dist/form.js";

import BrowseTable from "../components/BrowseTable.vue";
import EditDialog from "../components/EditDialog.vue";

import { ref, onActivated, nextTick } from "vue";
import { postJson } from "../api/http";
import { text } from "../api/i18n";
import { requireJobResult } from "../api/jobs";
import { useBusyDialog } from "../composables/useBusyDialog.js";
import { showInfo, showError } from "../services/messageBoxService";

import type Input from "@ui5/webcomponents/dist/Input.js";

const STATE_KEY = "3433957a-5046-45a6-b779-15ff08cdff36";

type MessageViewRequestModel = {};

type MessageViewResponseModel = {
    records: MessageViewResponseModelItem[];
}

type MessageViewResponseModelItem = {
    id: string;
    title: string;
};

type MessageViewJobTokenModel = {
    token: string;
};

type TitleDialogModel = {
    title: string;
};

type TitleDialogInstance<T> = {
    open(model: T): Promise<T | null>;
};

type ValueState = "None" | "Negative";

const items = ref<MessageViewResponseModelItem[]>([]);
const titleDialog = ref<TitleDialogInstance<TitleDialogModel> | null>(null);
const titleInput = ref<Input | null>(null);
const titleValueState = ref<ValueState>("None");
const busyDialog = useBusyDialog();
const maxItems = ref(100);
const hasMore = ref(false);

const columns = [
    {
        key: "id" as const,
        title: text("VIEWS_MESSAGE_VIEW_ID"),
        width: "24rem",
        sortable: true,
        searchable: true,
    },
    {
        key: "title" as const,
        title: text("VIEWS_MESSAGE_VIEW_TITLE"),
        sortable: true,
        searchable: true,
    },
];

async function onReloadClick(): Promise<void> {
    try {        
        clearValidation();
        const requestModel = validateAndCreateRequestModel();
        if (requestModel == null) {
            return;
        }

        const response = await postJson<MessageViewJobTokenModel>("/api/message/read", {});
        const result = await busyDialog.run(response.token, false);
        const responseModel = requireJobResult<MessageViewResponseModel>(result);

        items.value = responseModel.records;
        hasMore.value = false;
        maxItems.value = items.value.length;
    } catch (error) {
        handleError(error);
    }
}

async function onGreetingClick(): Promise<void> {
    try {        
        clearValidation();
        const requestModel = validateAndCreateRequestModel();
        if (requestModel == null) {
            return;
        }

        const titleDialogInputModel: TitleDialogModel = {
            title: "",
        };
        const titleDialogOutputModel = await titleDialog.value?.open(titleDialogInputModel);
        if (!titleDialogOutputModel) {
            return;
        }

        const params = {
            ...requestModel,
            title: titleDialogOutputModel.title,
        };
        const response = await postJson<MessageViewJobTokenModel>("/api/message/greeting", params);
        const result = await busyDialog.run(response.token, false);
        const responseModel = requireJobResult<TitleDialogModel>(result);

        showInfo(text("VIEWS_MESSAGE_VIEW_GREETING"), responseModel.title);
    } catch (error) {
        handleError(error);
    }
}

function validateAndCreateRequestModel(): MessageViewRequestModel | null {
    return {};
}

function handleError(error: unknown): void {
        const errorMessage = error instanceof Error && error.message
        ? error.message
        : text("VIEWS_MESSAGE_VIEW_ERROR_LABEL");
    showError(text("VIEWS_MESSAGE_VIEW_ERROR_MESSAGE"), errorMessage);
}

function clearValidation(): void {
    titleValueState.value = "None";
}

function titleDialogEditValidation(model: TitleDialogModel): boolean {
    const valid = model.title.trim().length > 0;
    titleValueState.value = valid ? "None" : "Negative";
    if (!valid) {
        void nextTick(() => titleInput.value?.focus());
    }
    return valid;
}

onActivated(onReloadClick);
</script>


<template>
    <BrowseTable :items="items" :columns="columns" row-key="id" :state-key="STATE_KEY" :max-items="maxItems"
        :has-more="hasMore">
        <template #toolbar-actions>
            <ui5-toolbar-button icon="refresh" :text.prop="text('VIEWS_MESSAGE_VIEW_RELOAD')"
                @click="onReloadClick"></ui5-toolbar-button>

            <ui5-toolbar-separator></ui5-toolbar-separator>

            <ui5-toolbar-button icon="form" :text.prop="text('VIEWS_MESSAGE_VIEW_GREETING')"
                @click="onGreetingClick"></ui5-toolbar-button>
        </template>

        <template #cell-id="{ item }">
            {{ item.id }}
        </template>

        <template #cell-title="{ item }">
            {{ item.title }}
        </template>
    </BrowseTable>

    <EditDialog ref="titleDialog" :title="text('VIEWS_MESSAGE_VIEW_DIALOG_QUERY')"
        :validate="titleDialogEditValidation">
        <template #default="{ model }">
            <ui5-label for="titleDialogInput" show-colon>{{ text('VIEWS_MESSAGE_VIEW_DIALOG_TITLE_LABEL') }}</ui5-label>
            <ui5-input ref="titleInput" id="titleDialogInput" :value.prop="model.title"
                :placeholder.prop="text('VIEWS_MESSAGE_VIEW_DIALOG_TITLE_PLACEHOLDER')" :valueState.prop="titleValueState"
                @input="model.title = ($event.target as Input).value; clearValidation()">
                <div slot="valueStateMessage">{{ text('VIEWS_MESSAGE_VIEW_DIALOG_TITLE_REQUIRED') }}</div>
            </ui5-input>
        </template>
    </EditDialog>
</template>


<style scoped></style>
