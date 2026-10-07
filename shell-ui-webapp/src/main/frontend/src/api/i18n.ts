import { getI18nBundle, registerI18nLoader, } from "@ui5/webcomponents-base/dist/i18nBundle.js";
import { getConfig } from "./config";

const BUNDLE_ID = "app";
const SUPPORTED_LOCALES = getConfig().supportedLocales;

type I18nBundle = Awaited<ReturnType<typeof getI18nBundle>>;

let bundle: I18nBundle | null = null;
let registered = false;

/**
 * Registers the application's i18n resource loaders for all supported locales.
 *
 * The loaders are registered only once, even if this function is called
 * multiple times.
 */
export function registerAppI18n(): void {
    if (registered) {
        return;
    }
    SUPPORTED_LOCALES.forEach(localeToRegister => {
        registerI18nLoader(BUNDLE_ID, localeToRegister, async localeId => {
            const url = `${import.meta.env.BASE_URL}i18n/messagebundle_${localeId}.json`;
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`Failed to load i18n bundle '${localeId}'.`);
            }
            return await response.json();
        },
        );
    });
    registered = true;
}

/**
 * Initializes the application's i18n bundle.
 *
 * Registers the resource loaders if necessary and loads the bundle
 * for the current locale.
 */
export async function initI18n(): Promise<void> {
    registerAppI18n();
    bundle = await getI18nBundle(BUNDLE_ID);
}

/**
 * Returns the translated text for the specified key.
 *
 * If the i18n bundle has not been initialized yet, the key itself
 * is returned.
 *
 * @param key Translation key.
 * @param values Values used to replace placeholders in the translated text.
 * @returns Translated text, or the key if the bundle is not initialized.
 */
export function text(key: string, ...values: Array<string | number>): string {
    if (!bundle) {
        return key;
    }
    return bundle.getText(key, ...values);
}