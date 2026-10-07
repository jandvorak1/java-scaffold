<script setup lang="ts">

import "@ui5/webcomponents/dist/Button.js";

import "@ui5/webcomponents-fiori/dist/NavigationLayout.js";
import "@ui5/webcomponents-fiori/dist/ShellBar.js";
import "@ui5/webcomponents-fiori/dist/ShellBarBranding.js";
import "@ui5/webcomponents-fiori/dist/ShellBarItem.js";
import "@ui5/webcomponents-fiori/dist/SideNavigation.js";
import "@ui5/webcomponents-fiori/dist/SideNavigationItem.js";

import "@ui5/webcomponents-icons/dist/action-settings.js";
import "@ui5/webcomponents-icons/dist/form.js";
import "@ui5/webcomponents-icons/dist/home.js";
import "@ui5/webcomponents-icons/dist/log.js";
import "@ui5/webcomponents-icons/dist/menu2.js";

import { computed, ref } from "vue";
import { RouterView, useRoute, useRouter, } from "vue-router";
import { getConfig } from "../api/config";
import { postJson } from "../api/http";
import { text } from "../api/i18n";
import { showFatal } from "../services/messageBoxService";

import UserSettings from "../components/UserSettings.vue";

import type { Heartbeat } from "../composables/useHeartbeat";

/**
 * Supplies the backend monitor shared with the application shell.
 */
type ApplicationLayoutProps = {

    /**
     * Monitor stopped before requesting shutdown and kept stopped afterward.
     */
    heartbeat: Heartbeat;
};

/** Represents the exposed interface of the user settings dialog. */
type UserSettingsComponent = InstanceType<typeof UserSettings>;

const properties = defineProps<ApplicationLayoutProps>();

const userSettings = ref<UserSettingsComponent | null>(null);
const collapsed = ref(false);
const shutdownInProgress = ref(false);

const config = getConfig();
const appName = config.name;
const appVersion = config.version;

const router = useRouter();
const route = useRoute();

const navigationMode = computed(() => collapsed.value ? "Collapsed" : "Expanded");

/** Toggles between expanded and collapsed side navigation. */
function toggleMenu(): void {
    collapsed.value = !collapsed.value;
}

/** Opens the settings dialog when its component is mounted. */
function onClickSettings(): void {
    void userSettings.value?.open();
}

/**
 * Requests shutdown once after stopping backend availability checks.
 * Request failures are ignored because the backend may already be shut down.
 * The final shutdown message is displayed regardless of the request outcome.
 *
 * @returns A promise resolved after the final shutdown message is displayed.
 */
async function onClickShutdown(): Promise<void> {
    if (shutdownInProgress.value) {
        return;
    }
    shutdownInProgress.value = true;
    const shutdownTitle = text("LAYOUTS_APPLICATION_LAYOUT_SHUTDOWN_TITLE");
    properties.heartbeat.stop();
    showFatal(shutdownTitle, text("LAYOUTS_APPLICATION_LAYOUT_SHUTDOWN_PROCESSING"));
    await waitForBrowserPaint();
    try {
        await postJson<void>("/api/webtray/shutdown", undefined);
    } catch {
        // The backend may already be shut down, so no recovery is needed.
    }
    showFatal(shutdownTitle, text("LAYOUTS_APPLICATION_LAYOUT_SHUTDOWN_DONE"));
}

/** Navigates to the home view. */
function onClickHome(): void {
    void router.push({ path: "/", });
}

/** Navigates to the message view. */
function onClickMessage(): void {
    void router.push({ path: "/messages", });
}

/**
 * Gives the browser an opportunity to paint the shutdown message.
 * A timer releases the wait if animation frames are suspended in a hidden tab.
 *
 * @returns A promise resolved after two animation frames or the fallback timer.
 */
function waitForBrowserPaint(): Promise<void> {
    return new Promise(resolve => {
        let frameId = 0;
        const finish = (): void => {
            window.clearTimeout(timeoutId);
            window.cancelAnimationFrame(frameId);
            resolve();
        };
        const timeoutId = window.setTimeout(finish, 100);
        frameId = window.requestAnimationFrame(() => {
            frameId = window.requestAnimationFrame(finish);
        });
    });
}
</script>


<template>
    <ui5-navigation-layout class="app-navigation-layout" :mode.prop="navigationMode">
        <ui5-shellbar slot="header" :primaryTitle.prop="appName" :secondaryTitle.prop="appVersion"
            :collapsed.prop="collapsed">
            <ui5-button slot="startButton" icon="menu2" design="Transparent"
                :tooltip.prop="text('LAYOUTS_APPLICATION_LAYOUT_MENU_TOGGLE')" @click="toggleMenu" />

            <ui5-button slot="startButton" icon="home" design="Transparent"
                :tooltip.prop="text('LAYOUTS_APPLICATION_LAYOUT_MENU_HOME')" @click="onClickHome" />

            <ui5-shellbar-branding slot="branding" target="_self" @click="onClickHome"> <span
                    class="app-shellbar-branding-text"> {{ appName }} {{ appVersion }} </span>
                <img slot="logo" src="/images/logo.svg" :alt="`${appName} ${appVersion}`" width="32" height="32">
            </ui5-shellbar-branding>

            <ui5-shellbar-item icon="action-settings" :text.prop="text('LAYOUTS_APPLICATION_LAYOUT_MENU_SETTINGS')"
                @click="onClickSettings" />

            <ui5-shellbar-item icon="log" :text.prop="text('LAYOUTS_APPLICATION_LAYOUT_MENU_SHUTDOWN')"
                @click="onClickShutdown" />
        </ui5-shellbar>

        <ui5-side-navigation slot="sideContent">
            <ui5-side-navigation-item :text.prop="text('LAYOUTS_APPLICATION_LAYOUT_MENU_HOME')" icon="home"
                :selected.prop="route.path === '/'" @click="onClickHome" />

            <ui5-side-navigation-item :text.prop="text('LAYOUTS_APPLICATION_LAYOUT_MENU_MESSAGE')" icon="form"
                :selected.prop="route.path === '/messages'" @click="onClickMessage" />
        </ui5-side-navigation>

        <main>
            <RouterView v-slot="{ Component }">
                <KeepAlive>
                    <component :is="Component" />
                </KeepAlive>
            </RouterView>
        </main>
    </ui5-navigation-layout>
    <UserSettings ref="userSettings" />
</template>


<style scoped>
.app-navigation-layout {
    height: 100vh;
}

.app-shellbar-branding-text {
    font-family: "72", "72full", "Arial", sans-serif;
    font-weight: 400;
    font-size: 0.875rem;
}
</style>
