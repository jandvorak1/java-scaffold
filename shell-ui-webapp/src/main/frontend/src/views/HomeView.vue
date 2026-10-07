<script setup lang="ts">

import "@ui5/webcomponents-fiori/dist/Page.js";
import "@ui5/webcomponents-fiori/dist/ProductSwitch.js";
import "@ui5/webcomponents-fiori/dist/ProductSwitchItem.js";

import "@ui5/webcomponents-icons/dist/form.js";

import { useRouter } from "vue-router";
import { text } from "../api/i18n";

const router = useRouter();

/**
 * Navigates to the message list through the application router.
 *
 * @returns A promise resolved when navigation completes or is prevented.
 * @throws Error If navigation fails unexpectedly.
 */
async function onClickMessage(): Promise<void> {
    await router.push({
        path: "/messages",
    });
}
</script>


<template>
    <ui5-page class="app-home-view" background-design="Solid">
        <ui5-product-switch>
            <ui5-product-switch-item icon="form" :titleText.prop="text('VIEWS_HOME_VIEW_SWITCH_MESSAGE_TITLE')"
                :subtitleText.prop="text('VIEWS_HOME_VIEW_SWITCH_MESSAGE_SUBTITLE')" @click="onClickMessage" />
        </ui5-product-switch>
    </ui5-page>
</template>

