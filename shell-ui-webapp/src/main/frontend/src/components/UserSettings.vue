<script setup lang="ts">

import "@ui5/webcomponents/dist/ComboBox.js";
import "@ui5/webcomponents/dist/ComboBoxItem.js";
import "@ui5/webcomponents/dist/Label.js";
import "@ui5/webcomponents/dist/Switch.js";
import "@ui5/webcomponents/dist/Text.js";
import "@ui5/webcomponents/dist/Toast.js";

import "@ui5/webcomponents-fiori/dist/UserSettingsDialog.js";
import "@ui5/webcomponents-fiori/dist/UserSettingsItem.js";
import "@ui5/webcomponents-fiori/dist/UserSettingsView.js";
import "@ui5/webcomponents-fiori/dist/UserSettingsAppearanceView.js";
import "@ui5/webcomponents-fiori/dist/UserSettingsAppearanceViewGroup.js";
import "@ui5/webcomponents-fiori/dist/UserSettingsAppearanceViewItem.js";

import "@ui5/webcomponents-icons/dist/palette.js";

import type ComboBox from "@ui5/webcomponents/dist/ComboBox.js";
import type ComboBoxItem from "@ui5/webcomponents/dist/ComboBoxItem.js";
import type Switch from "@ui5/webcomponents/dist/Switch.js";
import type Toast from "@ui5/webcomponents/dist/Toast.js";
import type UserSettingsAppearanceViewItem from "@ui5/webcomponents-fiori/dist/UserSettingsAppearanceViewItem.js";

import { getTheme, setTheme } from "@ui5/webcomponents-base/dist/config/Theme.js";
import { computed, nextTick, onBeforeUnmount, ref } from "vue";
import { getJson, postJson } from "../api/http";
import { text } from "../api/i18n";
import { useBusyOverlay } from "../composables/useBusyOverlay";
import { showError } from "../services/messageBoxService";

const SUPPORTED_THEMES = [
    "sap_horizon",
    "sap_horizon_dark",
    "sap_horizon_hcb",
    "sap_horizon_hcw",
    "sap_fiori_3",
    "sap_fiori_3_dark",
    "sap_fiori_3_hcb",
    "sap_fiori_3_hcw",
] as const;

/**
 * Names the themes offered by the appearance selector.
 */
type UserSettingsTheme = typeof SUPPORTED_THEMES[number];

/**
 * Carries the appearance item selected by the user.
 */
type AppearanceSelectionChangeEvent = CustomEvent<{
    readonly item: UserSettingsAppearanceViewItem;
}>;

/**
 * Defines validated preferences exchanged with the system preferences endpoint.
 */
type UserSettingsPreferencesModel = {
    readonly locale: string;
    readonly decimalFormat: string;
    readonly dateFormat: string;
    readonly timeFormat: string;
    readonly dateTimeFormat: string;
    readonly theme: UserSettingsTheme;
    readonly touch: boolean;
    readonly timeZone: string;
};

/**
 * Describes a selectable format with localized labels and a formatting pattern.
 */
type UserSettingsFormatModel = {
    readonly id: string;
    readonly title: string;
    readonly description: string;
    readonly pattern: string;
};

/**
 * Contains the formats returned by a system format endpoint.
 */
type UserSettingsFormatsModel = {
    readonly formats: readonly UserSettingsFormatModel[];
};

/**
 * Identifies a supported time zone and its display name.
 */
type UserSettingsTimeZoneModel = {
    readonly id: string;
    readonly name: string;
};

/**
 * Contains the selectable time zones returned by the system endpoint.
 */
type UserSettingsTimeZonesModel = {
    readonly timezones: readonly UserSettingsTimeZoneModel[];
};

/**
 * Identifies a locale with its translated and native display names.
 */
type UserSettingsLocaleModel = {
    readonly tag: string;
    readonly name: string;
    readonly subname: string;
};

/**
 * Contains the available locales returned by the system endpoint.
 */
type UserSettingsLocalesModel = {
    readonly locales: readonly UserSettingsLocaleModel[];
};

const opening = ref(false);
const openState = ref(false);
const saving = ref(false);
const themeChanging = ref(false);
const forceClosing = ref(false);
const formDisabled = computed(() => opening.value || saving.value || forceClosing.value);

const theme = ref<UserSettingsTheme>("sap_fiori_3");
const touch = ref(false);
let appliedTheme = getTheme();
let themeChangeVersion = 0;
let themeChangeQueue = Promise.resolve();
let disposed = false;

const locale = ref<ComboBox | null>(null);
const decimalFormat = ref<ComboBox | null>(null);
const dateFormat = ref<ComboBox | null>(null);
const timeFormat = ref<ComboBox | null>(null);
const dateTimeFormat = ref<ComboBox | null>(null);
const timeZone = ref<ComboBox | null>(null);

const savedToast = ref<Toast | null>(null);
const reloadToast = ref<Toast | null>(null);

const busyOverlay = useBusyOverlay();

const preferences = ref<UserSettingsPreferencesModel>({
    locale: "en-US",
    decimalFormat: "space_comma",
    dateFormat: "dd_mm_yyyy_dot",
    timeFormat: "hh_mm_ss_24",
    dateTimeFormat: "dd_mm_yyyy_dot_hh_mm_ss",
    theme: "sap_fiori_3",
    touch: false,
    timeZone: "Europe/Prague",
});

const locales = ref<UserSettingsLocalesModel>({
    locales: [],
});

const decimalFormats = ref<UserSettingsFormatsModel>({
    formats: [],
});

const dateFormats = ref<UserSettingsFormatsModel>({
    formats: [],
});

const timeFormats = ref<UserSettingsFormatsModel>({
    formats: [],
});

const dateTimeFormats = ref<UserSettingsFormatsModel>({
    formats: [],
});

const timeZones = ref<UserSettingsTimeZonesModel>({
    timezones: [],
});


/**
 * Fetches and validates preferences and available choices before opening the dialog.
 *
 * Initializes the theme selection from the active application theme. Requests
 * are ignored while the component is busy, already open, closing, or disposed.
 * Loading failures keep the dialog closed and display an error message.
 * Changes are saved on closing; theme previews apply immediately.
 *
 * @returns A promise resolved after initialization or error handling completes.
 */
async function open(): Promise<void> {
    if (disposed || opening.value || openState.value || forceClosing.value || saving.value || themeChanging.value) {
        return;
    }
    opening.value = true;
    try {
        await busyOverlay.run(async () => {
            const [
                loadedLocales,
                loadedDecimalFormats,
                loadedDateFormats,
                loadedTimeFormats,
                loadedDateTimeFormats,
                loadedTimeZones,
                loadedPreferences,
            ] = await Promise.all([
                loadLocales(),
                loadDecimalFormats(),
                loadDateFormats(),
                loadTimeFormats(),
                loadDateTimeFormats(),
                loadTimeZones(),
                loadPreferences(),
            ]);

            if (!preferencesAreSupported(loadedPreferences, loadedLocales, loadedDecimalFormats,
                loadedDateFormats, loadedTimeFormats, loadedDateTimeFormats, loadedTimeZones)) {
                throw new Error(text("COMPONENTS_USER_SETTINGS_OPEN_MESSAGE"));
            }
            if (disposed) {
                return;
            }

            locales.value = loadedLocales;
            decimalFormats.value = loadedDecimalFormats;
            dateFormats.value = loadedDateFormats;
            timeFormats.value = loadedTimeFormats;
            dateTimeFormats.value = loadedDateTimeFormats;
            timeZones.value = loadedTimeZones;
            preferences.value = loadedPreferences;

            forceClosing.value = false;

            openState.value = true;

            await populateForm(loadedPreferences);
        });
    } catch (error) {
        openState.value = false;
        if (!disposed) {
            showError(text("COMPONENTS_USER_SETTINGS_OPEN_TITLE"), resolveErrorMessage(error, "COMPONENTS_USER_SETTINGS_OPEN_MESSAGE"));
        }
    } finally {
        opening.value = false;
    }
}

async function onBeforeClose(event: Event): Promise<void> {
    if (forceClosing.value) {
        return;
    }
    event.preventDefault();
    if (disposed || opening.value || saving.value || themeChanging.value) {
        return;
    }
    saving.value = true;
    try {
        const model = collectForm();
        if (preferencesEqual(model, preferences.value)) {
            requestClose();
            return;
        }
        const savedPreferences =
            await busyOverlay.run(async () => {
                await savePreferences(model);
                return model;
            });
        if (disposed) {
            return;
        }
        const reloadRequired = requiresReload(preferences.value, savedPreferences);
        preferences.value = savedPreferences;
        if (reloadRequired) {
            if (reloadToast.value) {
                reloadToast.value.open = true;
            }
        } else {
            if (savedToast.value) {
                savedToast.value.open = true;
            }
        }
        requestClose();
    } catch (error) {
        if (!disposed) {
            showError(text("COMPONENTS_USER_SETTINGS_SAVE_TITLE"), resolveErrorMessage(error, "COMPONENTS_USER_SETTINGS_SAVE_MESSAGE"));
        }
    } finally {
        saving.value = false;
    }
}

function onClose(): void {
    forceClosing.value = false;
    openState.value = false;
}

function requestClose(): void {
    forceClosing.value = true;
    openState.value = false;
}

function onTouchChange(event: Event): void {
    if (disposed || formDisabled.value) {
        return;
    }
    const target = event.currentTarget as Switch;
    touch.value = target.checked;
}

function onAppearanceChange(event: Event): void {
    if (disposed || formDisabled.value) {
        return;
    }
    const selectionEvent = event as AppearanceSelectionChangeEvent;
    const itemKey = selectionEvent.detail.item.dataset.itemKey;
    if (!isSupportedTheme(itemKey) || itemKey === theme.value) {
        return;
    }
    const version = ++themeChangeVersion;
    theme.value = itemKey;
    themeChanging.value = true;
    const change = themeChangeQueue.then(async () => {
        if (disposed) {
            return;
        }
        appliedTheme = getTheme();
        try {
            await setTheme(itemKey);
            appliedTheme = itemKey;
        } catch (error) {
            try {
                await setTheme(appliedTheme);
            } catch {
                // Preserve the original theme error.
            }
            throw error;
        }
    });
    themeChangeQueue = change.catch(() => undefined);
    void change.then(() => {
        if (themeChangeVersion === version) {
            themeChanging.value = false;
        }
    }, error => {
        if (themeChangeVersion === version) {
            theme.value = isSupportedTheme(appliedTheme) ? appliedTheme : preferences.value.theme;
            themeChanging.value = false;
            if (!disposed) {
                showError(text("COMPONENTS_USER_SETTINGS_OPEN_TITLE"),
                    resolveErrorMessage(error, "COMPONENTS_USER_SETTINGS_THEME_MESSAGE"));
            }
        }
    });
}

async function populateForm(model: UserSettingsPreferencesModel): Promise<void> {
    appliedTheme = getTheme();
    theme.value = isSupportedTheme(appliedTheme) ? appliedTheme : model.theme;
    touch.value = model.touch;

    await nextTick();
    if (disposed) {
        return;
    }

    setComboBoxSelectedValue(locale.value, model.locale);
    setComboBoxSelectedValue(decimalFormat.value, model.decimalFormat);
    setComboBoxSelectedValue(dateFormat.value, model.dateFormat);
    setComboBoxSelectedValue(timeFormat.value, model.timeFormat);
    setComboBoxSelectedValue(dateTimeFormat.value, model.dateTimeFormat);
    setComboBoxSelectedValue(timeZone.value, model.timeZone);
}

function collectForm(): UserSettingsPreferencesModel {
    return {
        theme: theme.value,
        touch: touch.value,
        locale: getComboBoxSelectedValue(locale.value),
        decimalFormat: getComboBoxSelectedValue(decimalFormat.value),
        dateFormat: getComboBoxSelectedValue(dateFormat.value),
        timeFormat: getComboBoxSelectedValue(timeFormat.value),
        dateTimeFormat: getComboBoxSelectedValue(dateTimeFormat.value),
        timeZone: getComboBoxSelectedValue(timeZone.value),
    };
}

function preferencesEqual(left: UserSettingsPreferencesModel, right: UserSettingsPreferencesModel): boolean {
    return left.locale === right.locale
        && left.decimalFormat === right.decimalFormat
        && left.dateFormat === right.dateFormat
        && left.timeFormat === right.timeFormat
        && left.dateTimeFormat === right.dateTimeFormat
        && left.theme === right.theme
        && left.touch === right.touch
        && left.timeZone === right.timeZone;
}

function requiresReload(previous: UserSettingsPreferencesModel, current: UserSettingsPreferencesModel): boolean {
    return previous.locale !== current.locale
        || previous.decimalFormat !== current.decimalFormat
        || previous.dateFormat !== current.dateFormat
        || previous.timeFormat !== current.timeFormat
        || previous.dateTimeFormat !== current.dateTimeFormat
        || previous.touch !== current.touch
        || previous.timeZone !== current.timeZone;
}

function getComboBoxSelectedValue(comboBox: ComboBox | null): string {
    if (!comboBox) {
        throw new Error(text("COMPONENTS_USER_SETTINGS_SAVE_MESSAGE"));
    }
    const item = Array.from(comboBox.querySelectorAll<ComboBoxItem>("ui5-cb-item"))
        .find(item => item.value === comboBox.selectedValue
            && item.text?.toLowerCase() === comboBox.value.toLowerCase());
    if (!item || !item.value) {
        comboBox.valueState = "Negative";
        comboBox.focus();
        throw new Error(text("COMPONENTS_USER_SETTINGS_SAVE_MESSAGE"));
    }
    comboBox.valueState = "None";
    return item.value;
}

function setComboBoxSelectedValue(comboBox: ComboBox | null, selectedValue: string): void {
    if (!comboBox) {
        return;
    }
    comboBox.valueState = "None";
    comboBox.selectedValue = selectedValue;
    const item = Array.from(comboBox.querySelectorAll<ComboBoxItem>("ui5-cb-item")
    ).find(item => item.value === selectedValue);
    comboBox.value = item?.text ?? "";
}

function loadPreferences(): Promise<UserSettingsPreferencesModel> {
    return loadValidated("/api/system/preferences", isPreferencesModel);
}

function savePreferences(model: UserSettingsPreferencesModel): Promise<void> {
    return postJson<void>("/api/system/preferences", model);
}

function loadDecimalFormats(): Promise<UserSettingsFormatsModel> {
    return loadValidated("/api/system/formats/decimal", isFormatsModel);
}

function loadDateFormats(): Promise<UserSettingsFormatsModel> {
    return loadValidated("/api/system/formats/date", isFormatsModel);
}

function loadTimeFormats(): Promise<UserSettingsFormatsModel> {
    return loadValidated("/api/system/formats/time", isFormatsModel);
}

function loadDateTimeFormats(): Promise<UserSettingsFormatsModel> {
    return loadValidated("/api/system/formats/datetime", isFormatsModel);
}

function loadLocales(): Promise<UserSettingsLocalesModel> {
    return loadValidated("/api/system/locales", isLocalesModel);
}

function loadTimeZones(): Promise<UserSettingsTimeZonesModel> {
    return loadValidated("/api/system/timezones", isTimeZonesModel);
}

async function loadValidated<T>(url: string, validator: (value: unknown) => value is T): Promise<T> {
    const value = await getJson<unknown>(url);
    if (!validator(value)) {
        throw new Error(text("COMPONENTS_USER_SETTINGS_OPEN_MESSAGE"));
    }
    return value;
}

function isPreferencesModel(value: unknown): value is UserSettingsPreferencesModel {
    return isRecord(value)
        && isNonBlankString(value.locale)
        && isNonBlankString(value.decimalFormat)
        && isNonBlankString(value.dateFormat)
        && isNonBlankString(value.timeFormat)
        && isNonBlankString(value.dateTimeFormat)
        && isSupportedTheme(value.theme)
        && typeof value.touch === "boolean"
        && isNonBlankString(value.timeZone);
}

function isFormatsModel(value: unknown): value is UserSettingsFormatsModel {
    return isRecord(value)
        && Array.isArray(value.formats)
        && value.formats.every(item => isRecord(item)
            && isNonBlankString(item.id)
            && typeof item.title === "string"
            && typeof item.description === "string"
            && isNonBlankString(item.pattern))
        && hasUniqueValues(value.formats, "id");
}

function isLocalesModel(value: unknown): value is UserSettingsLocalesModel {
    return isRecord(value)
        && Array.isArray(value.locales)
        && value.locales.every(item => isRecord(item)
            && isNonBlankString(item.tag)
            && isNonBlankString(item.name)
            && isNonBlankString(item.subname))
        && hasUniqueValues(value.locales, "tag");
}

function isTimeZonesModel(value: unknown): value is UserSettingsTimeZonesModel {
    return isRecord(value)
        && Array.isArray(value.timezones)
        && value.timezones.every(item => isRecord(item)
            && isNonBlankString(item.id)
            && typeof item.name === "string")
        && hasUniqueValues(value.timezones, "id");
}

function preferencesAreSupported(model: UserSettingsPreferencesModel, localeModel: UserSettingsLocalesModel,
    decimalModel: UserSettingsFormatsModel, dateModel: UserSettingsFormatsModel,
    timeModel: UserSettingsFormatsModel, dateTimeModel: UserSettingsFormatsModel,
    timeZoneModel: UserSettingsTimeZonesModel): boolean {
    return localeModel.locales.some(item => item.tag === model.locale)
        && decimalModel.formats.some(item => item.id === model.decimalFormat)
        && dateModel.formats.some(item => item.id === model.dateFormat)
        && timeModel.formats.some(item => item.id === model.timeFormat)
        && dateTimeModel.formats.some(item => item.id === model.dateTimeFormat)
        && timeZoneModel.timezones.some(item => item.id === model.timeZone);
}

function hasUniqueValues(items: unknown[], property: string): boolean {
    const values = items.map(item => isRecord(item) ? item[property] : undefined);
    return new Set(values).size === values.length;
}

function isSupportedTheme(value: unknown): value is UserSettingsTheme {
    return typeof value === "string" && (SUPPORTED_THEMES as readonly string[]).includes(value);
}

function isNonBlankString(value: unknown): value is string {
    return typeof value === "string" && value.trim().length > 0;
}

function isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === "object" && value !== null && !Array.isArray(value);
}

function resolveErrorMessage(error: unknown, fallbackKey: string): string {
    const message = error instanceof Error ? error.message.trim() : "";
    return message || text(fallbackKey).trim();
}

onBeforeUnmount(() => {
    disposed = true;
    themeChangeVersion++;
});

defineExpose({
    open,
});
</script>


<template>
    <ui5-user-settings-dialog :open.prop="openState" :headerText.prop="text('COMPONENTS_USER_SETTINGS_TITLE_SETTINGS')"
        @before-close="onBeforeClose" @close="onClose">

        <ui5-user-settings-item icon="palette" :text.prop="text('COMPONENTS_USER_SETTINGS_TITLE_APPEARANCE')"
            :tooltip.prop="text('COMPONENTS_USER_SETTINGS_TITLE_APPEARANCE')"
            :headerText.prop="text('COMPONENTS_USER_SETTINGS_TITLE_APPEARANCE')">

            <ui5-user-settings-appearance-view :text.prop="text('COMPONENTS_USER_SETTINGS_TITLE_THEMES')"
                @selection-change="onAppearanceChange">
                <div slot="additionalContent">
                    <div class="app-additional-content-header">
                        <ui5-text id="user-settings-touch-text"> {{ text("COMPONENTS_USER_SETTINGS_APPEARANCE_TOUCH") }}
                        </ui5-text>
                        <ui5-switch :disabled.prop="formDisabled" :checked.prop="touch"
                            accessible-name-ref="user-settings-touch-text" @change="onTouchChange" />
                    </div>
                    <ui5-text class="app-additional-description"> {{ text("COMPONENTS_USER_SETTINGS_APPEARANCE_MESSAGE"
                    ) }} </ui5-text>
                </div>

                <ui5-user-settings-appearance-view-group
                    :headerText.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_HORIZON')">
                    <ui5-user-settings-appearance-view-item data-item-key="sap_horizon"
                        :selected.prop="theme === 'sap_horizon'"
                        :text.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_HORIZON_LIGHT')" />
                    <ui5-user-settings-appearance-view-item data-item-key="sap_horizon_dark"
                        :selected.prop="theme === 'sap_horizon_dark'"
                        :text.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_HORIZON_DARK')" />
                    <ui5-user-settings-appearance-view-item data-item-key="sap_horizon_hcb"
                        :selected.prop="theme === 'sap_horizon_hcb'"
                        :text.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_HORIZON_HCB')" />
                    <ui5-user-settings-appearance-view-item data-item-key="sap_horizon_hcw"
                        :selected.prop="theme === 'sap_horizon_hcw'"
                        :text.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_HORIZON_HCV')" />
                </ui5-user-settings-appearance-view-group>

                <ui5-user-settings-appearance-view-group
                    :headerText.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_FIORI')">
                    <ui5-user-settings-appearance-view-item data-item-key="sap_fiori_3"
                        :selected.prop="theme === 'sap_fiori_3'"
                        :text.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_FIORI_LIGHT')" />
                    <ui5-user-settings-appearance-view-item data-item-key="sap_fiori_3_dark"
                        :selected.prop="theme === 'sap_fiori_3_dark'"
                        :text.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_FIORI_DARK')" />
                    <ui5-user-settings-appearance-view-item data-item-key="sap_fiori_3_hcb"
                        :selected.prop="theme === 'sap_fiori_3_hcb'"
                        :text.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_FIORI_HCB')" />
                    <ui5-user-settings-appearance-view-item data-item-key="sap_fiori_3_hcw"
                        :selected.prop="theme === 'sap_fiori_3_hcw'"
                        :text.prop="text('COMPONENTS_USER_SETTINGS_APPEARANCE_FIORI_HCV')" />
                </ui5-user-settings-appearance-view-group>
            </ui5-user-settings-appearance-view>
        </ui5-user-settings-item>

        <ui5-user-settings-item :text.prop="text('COMPONENTS_USER_SETTINGS_TITLE_LANGUAGE')"
            :tooltip.prop="text('COMPONENTS_USER_SETTINGS_TITLE_LANGUAGE')"
            :headerText.prop="text('COMPONENTS_USER_SETTINGS_TITLE_LANGUAGE')">
            <ui5-user-settings-view>
                <div class="app-language-region-container">
                    <ui5-label for="user-settings-locale" class="app-language-region-label" show-colon> {{ text(
                        "COMPONENTS_USER_SETTINGS_LANGUAGE_LOCALE_TITLE") }} </ui5-label>
                    <ui5-combobox :disabled.prop="formDisabled" id="user-settings-locale" ref="locale"
                        class="app-language-region-control"
                        :placeholder.prop="text('COMPONENTS_USER_SETTINGS_LANGUAGE_LOCALE_PLACEHOLDER')">
                        <ui5-cb-item v-for="item in locales.locales" :key="item.tag" :value="item.tag" :text="item.name"
                            :additional-text="item.subname" />
                    </ui5-combobox>

                    <ui5-label for="user-settings-decimal-format" class="app-language-region-label" show-colon> {{ text(
                        "COMPONENTS_USER_SETTINGS_LANGUAGE_DECIMAL_FORMAT_TITLE") }}
                    </ui5-label>
                    <ui5-combobox :disabled.prop="formDisabled" id="user-settings-decimal-format" ref="decimalFormat"
                        class="app-language-region-control"
                        :placeholder.prop="text('COMPONENTS_USER_SETTINGS_LANGUAGE_DECIMAL_FORMAT_PLACEHOLDER')">
                        <ui5-cb-item v-for="format in decimalFormats.formats" :key="format.id" :value="format.id"
                            :text="format.title" :additional-text="format.description" />
                    </ui5-combobox>

                    <ui5-label for="user-settings-date-format" class="app-language-region-label" show-colon> {{ text(
                        "COMPONENTS_USER_SETTINGS_LANGUAGE_DATE_FORMAT_TITLE") }} </ui5-label>
                    <ui5-combobox :disabled.prop="formDisabled" id="user-settings-date-format" ref="dateFormat"
                        class="app-language-region-control"
                        :placeholder.prop="text('COMPONENTS_USER_SETTINGS_LANGUAGE_DATE_FORMAT_PLACEHOLDER')">
                        <ui5-cb-item v-for="format in dateFormats.formats" :key="format.id" :value="format.id"
                            :text="format.title" :additional-text="format.description" />
                    </ui5-combobox>

                    <ui5-label for="user-settings-time-format" class="app-language-region-label" show-colon> {{ text(
                        "COMPONENTS_USER_SETTINGS_LANGUAGE_TIME_FORMAT_TITLE") }} </ui5-label>
                    <ui5-combobox :disabled.prop="formDisabled" id="user-settings-time-format" ref="timeFormat"
                        class="app-language-region-control"
                        :placeholder.prop="text('COMPONENTS_USER_SETTINGS_LANGUAGE_TIME_FORMAT_PLACEHOLDER')">
                        <ui5-cb-item v-for="format in timeFormats.formats" :key="format.id" :value="format.id"
                            :text="format.title" :additional-text="format.description" />
                    </ui5-combobox>

                    <ui5-label for="user-settings-datetime-format" class="app-language-region-label" show-colon> {{
                        text("COMPONENTS_USER_SETTINGS_LANGUAGE_DATETIME_FORMAT_TITLE") }} </ui5-label>
                    <ui5-combobox :disabled.prop="formDisabled" id="user-settings-datetime-format" ref="dateTimeFormat"
                        class="app-language-region-control"
                        :placeholder.prop="text('COMPONENTS_USER_SETTINGS_LANGUAGE_DATETIME_FORMAT_PLACEHOLDER')">
                        <ui5-cb-item v-for="format in dateTimeFormats.formats" :key="format.id" :value="format.id"
                            :text="format.title" :additional-text="format.description" />
                    </ui5-combobox>

                    <ui5-label for="user-settings-timezone" class="app-language-region-label" show-colon> {{ text(
                        "COMPONENTS_USER_SETTINGS_LANGUAGE_TIMEZONE_TITLE") }} </ui5-label>
                    <ui5-combobox :disabled.prop="formDisabled" id="user-settings-timezone" ref="timeZone"
                        class="app-language-region-control"
                        :placeholder.prop="text('COMPONENTS_USER_SETTINGS_LANGUAGE_TIMEZONE_PLACEHOLDER')">
                        <ui5-cb-item v-for="zone in timeZones.timezones" :key="zone.id" :value="zone.id" :text="zone.id"
                            :additional-text="zone.name" />
                    </ui5-combobox>
                </div>
            </ui5-user-settings-view>
        </ui5-user-settings-item>
    </ui5-user-settings-dialog>

    <ui5-toast ref="savedToast" design="Positive"> {{ text("COMPONENTS_USER_SETTINGS_SAVE_MESSAGE_SUCCESS") }}
    </ui5-toast>

    <ui5-toast ref="reloadToast" design="Warning"> {{ text("COMPONENTS_USER_SETTINGS_SAVE_MESSAGE_SUCCESS") }} {{ text(
        "COMPONENTS_USER_SETTINGS_SAVE_MESSAGE_RELOAD") }}
    </ui5-toast>
</template>


<style scoped>
.app-language-region-container {
    display: flex;
    min-height: 2.5rem;
    align-items: flex-start;
    flex-direction: column;
    gap: 0.563rem;
}

.app-language-region-label {
    display: flex;
    flex: 1 0 0;
    width: 100%;
}

.app-language-region-control {
    display: flex;
    gap: 0.188rem;
    width: 100%;
}

.app-additional-content-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.5rem;
}

.app-additional-description {
    display: block;
    color: var(--sapContent_LabelColor);
}
</style>
