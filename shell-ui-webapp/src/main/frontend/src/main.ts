import { setDefaultFontLoading } from "@ui5/webcomponents-base/dist/config/Fonts.js";
import { setFetchDefaultLanguage } from "@ui5/webcomponents-base/dist/config/Language.js";

import "@ui5/webcomponents-base/dist/FontFace.css";
import "@ui5/webcomponents/dist/Assets.js";
import "@ui5/webcomponents-fiori/dist/Assets.js";

import { createApp } from "vue";
import { initI18n } from "./api/i18n";
import { router } from "./router";
import App from "./App.vue";

import "./app.css";

setDefaultFontLoading(false);
setFetchDefaultLanguage(true);

/**
 * Initializes translations and the initial route before mounting the application.
 *
 * Router installation starts navigation after translations are available to its
 * guards. Mounting waits for that navigation, including asynchronous route work.
 *
 * @returns A promise resolved after the root component is mounted.
 * @throws Error If the application mount element is missing.
 * Initialization and navigation failures reject the returned promise.
 */
async function bootstrap(): Promise<void> {
    const mountElement = document.querySelector<HTMLElement>("#app");
    if (!mountElement) {
        throw new Error("Application mount element #app is missing.");
    }

    await initI18n();

    const app = createApp(App);
    app.use(router);
    await router.isReady();
    app.mount(mountElement);
}

await bootstrap();
