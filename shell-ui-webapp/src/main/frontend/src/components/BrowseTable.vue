<script setup lang="ts" generic="T extends Record<string, unknown>">

import "@ui5/webcomponents/dist/Input.js";
import "@ui5/webcomponents/dist/MessageStrip.js";
import "@ui5/webcomponents/dist/Table.js";
import "@ui5/webcomponents/dist/TableCell.js";
import "@ui5/webcomponents/dist/TableHeaderCell.js";
import "@ui5/webcomponents/dist/TableHeaderRow.js";
import "@ui5/webcomponents/dist/TableRow.js";
import "@ui5/webcomponents/dist/Toolbar.js";
import "@ui5/webcomponents/dist/ToolbarButton.js";
import "@ui5/webcomponents/dist/ToolbarItem.js";
import "@ui5/webcomponents/dist/ToolbarSpacer.js";

import "@ui5/webcomponents-icons/dist/decline.js";
import "@ui5/webcomponents-icons/dist/download.js";
import "@ui5/webcomponents-icons/dist/search.js";

import { computed, nextTick, onActivated, onBeforeUnmount, onDeactivated, onMounted, ref, useSlots, watch } from "vue";
import { getConfig } from "../api/config";
import { text } from "../api/i18n";
import { useBusyOverlay } from "../composables/useBusyOverlay";

import type Input from "@ui5/webcomponents/dist/Input.js";

/**
 * Represents horizontal alignment options for a table header cell.
 */
type HorizontalAlign = "Start" | "Center" | "End";

/**
 * Represents a local sorting direction for table rows.
 */
type SortDirection = "None" | "Ascending" | "Descending";

/**
 * Stores the sorting selection persisted for a table instance.
 */
type TableState = {
    /**
     * Identifies the sorted column, or is null when sorting is disabled.
     */
    readonly sortColumn: string | null;

    /**
     * Specifies the direction selected for the persisted column.
     */
    readonly sortDirection: SortDirection;
};

/**
 * Configures rendering and local processing for an item property.
 */
type Column<T> = {

    /**
     * Identifies the item property rendered in the column.
     */
    readonly key: Extract<keyof T, string>;

    /**
     * Provides the label displayed in the table header.
     */
    readonly title: string;

    /**
     * Determines whether local sorting is available for the column.
     *
     * @default false
     */
    readonly sortable?: boolean;

    /**
     * Determines whether the displayed value is included in local text search.
     *
     * @default false
     */
    readonly searchable?: boolean;

    /**
     * Determines whether the column is included in CSV export.
     *
     * @default true
     */
    readonly exportable?: boolean;

    /**
     * Formats the raw value for display, local search, and CSV export.
     */
    readonly formatter?: (value: unknown, item: T) => string;

    /**
     * Sets the width passed to the UI5 header cell.
     */
    readonly width?: string;

    /**
     * Sets the horizontal alignment of the header content.
     */
    readonly horizontalAlign?: HorizontalAlign;
};

/**
 * Defines the properties accepted by the browse table component.
 */
type BrowseTableProps<T> = {

    /**
     * Supplies the items rendered as table rows.
     */
    readonly items: readonly T[];

    /**
     * Supplies the columns used to render and process item properties.
     */
    readonly columns: readonly Column<T>[];

    /**
     * Selects the item property used as the Vue key for every row.
     *
     * Uses the row index when this property is omitted or its value is not a string or number.
     */
    readonly rowKey?: Extract<keyof T, string>;

    /**
     * Reports the maximum number of items returned by the backend.
     *
     * Does not limit the items displayed in the table.
     *
     * @default 1000
     */
    readonly maxItems?: number;

    /**
     * Determines whether to show a message that more backend items are available.
     *
     * @default false
     */
    readonly hasMore?: boolean;

    /**
     * Provides the text displayed when no rows are available.
     *
     * @default ""
     */
    readonly noDataText?: string;

    /**
     * Provides the local storage key used to persist the sorting state.
     *
     * Does not persist the sorting state when this property is omitted.
     */
    readonly stateKey?: string;

    /**
     * Determines whether the CSV export action is available.
     *
     * @default true
     */
    readonly exportable?: boolean;

    /**
     * Sets the file name assigned to the exported CSV file.
     *
     * @default "export.csv"
     */
    readonly exportFileName?: string;

    /**
     * Sets the separator used between fields in the exported CSV file.
     *
     * @default ";"
     */
    readonly csvDelimiter?: string;

};

const MIN_TABLE_HEIGHT = 300;
const BOTTOM_TABLE_MARGIN = 16;

const properties = withDefaults(
    defineProps<BrowseTableProps<T>>(),
    {
        maxItems: 1000,
        hasMore: false,
        noDataText: "",
        exportable: true,
        exportFileName: "export.csv",
        csvDelimiter: ";",
    }
);

const sortColumn = ref<Extract<keyof T, string> | null>(null);
const sortDirection = ref<SortDirection>("None");
const busyOverlay = useBusyOverlay();
const searchInputText = ref("");
const searchText = ref("");
const locale = getConfig().locale.replace("_", "-");
const tableRef = ref<HTMLElement | null>(null);
const toolbarRef = ref<HTMLElement | null>(null);
const messageStripRef = ref<HTMLElement | null>(null);
const tableHeight = ref(`${MIN_TABLE_HEIGHT}px`);
const slots = useSlots();
const tableRenderVersion = ref(0);

let resizeObserver: ResizeObserver | null = null;
let isTableHeightTrackingActive = false;
let isComponentActive = false;

const searchable = computed(() =>
    properties.columns.some(column => column.searchable)
);

const hasToolbar = computed(() =>
    searchable.value || properties.exportable || Boolean(slots["toolbar-actions"])
);

const filteredItems = computed(() => {
    const search = searchText.value.trim();
    if (search.length === 0) {
        return properties.items;
    }
    const searchableColumns = properties.columns.filter(column => column.searchable);
    if (searchableColumns.length === 0) {
        return properties.items;
    }
    const query = search.toLocaleLowerCase(locale);
    return properties.items.filter(item =>
        searchableColumns.some(column => {
            const value = displayValue(item, column);
            return value.toLocaleLowerCase(locale).includes(query);
        })
    );
});

const sortedItems = computed(() => {
    if (!sortColumn.value || sortDirection.value === "None") {
        return filteredItems.value;
    }
    const column = sortColumn.value;
    const direction = sortDirection.value;
    return [...filteredItems.value].sort((left, right) => {
        const result = compareValues(left[column], right[column]);
        return direction === "Ascending" ? result : -result;
    });
});

function onSearchInput(event: Event): void {
    const input = event.currentTarget as Input;
    searchInputText.value = input.value;
}

async function onSearchClick(): Promise<void> {
    await applySearchText(searchInputText.value);
}

async function onCancelClick(): Promise<void> {
    searchInputText.value = "";
    await applySearchText("");
}

async function applySearchText(value: string): Promise<void> {
    if (searchText.value === value) {
        return;
    }
    await busyOverlay.run(async () => {
        await waitForRender();
        searchText.value = value;
        await waitForRender();
    });
}

function onExportCsv(): void {
    const csv = createCsv();
    const blob = new Blob(["\uFEFF", csv], {
        type: "text/csv;charset=utf-8",
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    try {
        link.href = url;
        link.download = properties.exportFileName;
        link.style.display = "none";
        document.body.appendChild(link);
        link.click();
    } finally {
        link.remove();
        URL.revokeObjectURL(url);
    }
}

function displayValue(item: T, column: Column<T>): string {
    const value = item[column.key];
    if (column.formatter) {
        return column.formatter(value, item);
    }
    if (value === null || value === undefined) {
        return "";
    }
    return String(value);
}

function getRowKey(item: T, index: number): string | number {
    if (!properties.rowKey) {
        return index;
    }
    const value = item[properties.rowKey];
    return typeof value === "string" || typeof value === "number" ? value : index;
}

function compareValues(left: unknown, right: unknown): number {
    if (left === null || left === undefined) {
        return right === null || right === undefined ? 0 : 1;
    }
    if (right === null || right === undefined) {
        return -1;
    }
    if (typeof left === "number" && typeof right === "number") {
        return left - right;
    }
    if (left instanceof Date && right instanceof Date) {
        return left.getTime() - right.getTime();
    }
    return String(left).localeCompare(String(right), locale, {
        numeric: true,
        sensitivity: "base",
    });
}

function isSortDirection(value: unknown): value is SortDirection {
    return (value === "None" || value === "Ascending" || value === "Descending");
}

function saveState(): void {
    if (!properties.stateKey) {
        return;
    }
    const state: TableState = {
        sortColumn: sortColumn.value,
        sortDirection: sortDirection.value,
    };
    try {
        localStorage.setItem(`BrowseTable:${properties.stateKey}`, JSON.stringify(state));
    } catch {
        // State persistence is not critical for table operation.
    }
}

function loadState(): void {
    if (!properties.stateKey) {
        return;
    }
    try {
        const value = localStorage.getItem(`BrowseTable:${properties.stateKey}`);
        if (!value) {
            return;
        }
        const state = JSON.parse(value) as unknown;
        if (!isTableState(state)) {
            return;
        }
        if (state.sortDirection === "None") {
            if (state.sortColumn === null) {
                sortColumn.value = null;
                sortDirection.value = "None";
            }
            return;
        }
        const column = properties.columns.find(column => column.key === state.sortColumn && column.sortable);
        if (!column) {
            return;
        }
        sortColumn.value = column.key;
        sortDirection.value = state.sortDirection;
    } catch {
        // Ignore invalid stored state.
    }
}

function isTableState(value: unknown): value is TableState {
    if (typeof value !== "object" || value === null || Array.isArray(value)) {
        return false;
    }
    const state = value as Record<string, unknown>;
    return (typeof state.sortColumn === "string" || state.sortColumn === null) && isSortDirection(state.sortDirection);
}

function updateSort(column: Column<T>): void {
    if (sortColumn.value !== column.key) {
        sortColumn.value = column.key;
        sortDirection.value = "Ascending";
    } else if (sortDirection.value === "Ascending") {
        sortDirection.value = "Descending";
    } else if (sortDirection.value === "Descending") {
        sortColumn.value = null;
        sortDirection.value = "None";
    } else {
        sortDirection.value = "Ascending";
    }
    saveState();
}

async function sortBy(column: Column<T>): Promise<void> {
    if (!column.sortable) {
        return;
    }
    await busyOverlay.run(async () => {
        await waitForRender();
        updateSort(column);
        await waitForRender();
    });
}

function sortIndicator(column: Column<T>): SortDirection {
    return sortColumn.value === column.key ? sortDirection.value : "None";
}

async function waitForRender(): Promise<void> {
    await nextTick();
    await new Promise<void>(resolve => {
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                resolve();
            });
        });
    });
}

function escapeCsvValue(value: string): string {
    const safeValue = /^[\t\r\n ]*[=+\-@]/.test(value) ? `'${value}` : value;
    const escaped = safeValue.replaceAll("\"", "\"\"");
    return `"${escaped}"`;
}

function createCsv(): string {
    const columns = properties.columns.filter(column => column.exportable !== false);
    const header = columns.map(column => escapeCsvValue(column.title)).join(properties.csvDelimiter);
    const rows = sortedItems.value.map(item => columns.map(column => escapeCsvValue(displayValue(item, column))).join(properties.csvDelimiter));
    return [header, ...rows].join("\r\n");
}

function updateTableHeight(): void {
    const table = tableRef.value;
    if (!table) {
        return;
    }
    const tableTop = table.getBoundingClientRect().top;
    const height = window.innerHeight - tableTop - BOTTOM_TABLE_MARGIN;
    tableHeight.value = `${Math.max(MIN_TABLE_HEIGHT, Math.floor(height))}px`;
}

function initResizeObserver(): void {
    resizeObserver?.disconnect();
    resizeObserver = new ResizeObserver(() => {
        updateTableHeight();
    });
    if (toolbarRef.value) {
        resizeObserver.observe(toolbarRef.value);
    }
    if (messageStripRef.value) {
        resizeObserver.observe(messageStripRef.value);
    }
    updateTableHeight();
}

function startTableHeightTracking(): void {
    if (!isTableHeightTrackingActive) {
        window.addEventListener("resize", updateTableHeight);
        isTableHeightTrackingActive = true;
    }
    initResizeObserver();
}

function stopTableHeightTracking(): void {
    resizeObserver?.disconnect();
    resizeObserver = null;
    if (isTableHeightTrackingActive) {
        window.removeEventListener("resize", updateTableHeight);
        isTableHeightTrackingActive = false;
    }
}

watch(
    [() => properties.hasMore, hasToolbar],
    async () => {
        await nextTick();
        if (isComponentActive) {
            startTableHeightTracking();
        }
    }
);

watch(
    [searchText, () => properties.items],
    () => {
        tableRenderVersion.value++;
    }
);

onMounted(() => {
    loadState();
});

onActivated(async () => {
    isComponentActive = true;
    await nextTick();
    startTableHeightTracking();
});

onDeactivated(() => {
    isComponentActive = false;
    stopTableHeightTracking();
});

onBeforeUnmount(() => {
    isComponentActive = false;
    stopTableHeightTracking();
});
</script>


<template>
    <div class="wrapper">
        <ui5-toolbar ref="toolbarRef" v-if="hasToolbar" class="toolbar">
            <ui5-toolbar-item v-if="searchable">
                <ui5-input :placeholder.prop="text('COMPONENTS_BROWSE_TABLE_PLACEHOLDER')
                    " :value.prop="searchInputText" @input="onSearchInput" />
            </ui5-toolbar-item>
            <ui5-toolbar-button v-if="searchable" icon="search" :text.prop="text('COMPONENTS_BROWSE_TABLE_SEARCH')"
                @click="onSearchClick" />
            <ui5-toolbar-button v-if="searchable" icon="decline" :text.prop="text('COMPONENTS_BROWSE_TABLE_CANCEL')"
                @click="onCancelClick" />

            <ui5-toolbar-button v-if="exportable" icon="download" :text.prop="text('COMPONENTS_BROWSE_TABLE_EXPORT')
                " @click="onExportCsv" />

            <ui5-toolbar-spacer />
            <slot name="toolbar-actions"></slot>
        </ui5-toolbar>
        <ui5-message-strip ref="messageStripRef" v-if="hasMore" design="Information" hide-close-button> {{
            text("COMPONENTS_BROWSE_TABLE_RESULT",
                maxItems) }} </ui5-message-strip>
        <ui5-table ref="tableRef" overflow-mode="Scroll" :noDataText.prop="noDataText" class="table"
            :key="tableRenderVersion" :style="{ height: tableHeight }">
            <ui5-table-header-row slot="headerRow" sticky>
                <ui5-table-header-cell v-for="column in columns" :key="column.key" :width.prop="column.width"
                    :horizontalAlign.prop="column.horizontalAlign" :sortIndicator.prop="sortIndicator(column)"
                    :tabindex="column.sortable ? 0 : undefined" :class="{ sortable: column.sortable }"
                    @click="sortBy(column)" @keydown.enter.prevent="sortBy(column)"
                    @keydown.space.prevent="sortBy(column)">
                    {{ column.title }}
                </ui5-table-header-cell>
            </ui5-table-header-row>

            <ui5-table-row v-for="(item, index) in sortedItems" :key="getRowKey(item, index)">
                <ui5-table-cell v-for="column in columns" :key="column.key">
                    <slot :name="`cell-${column.key}`" :item="item" :value="item[column.key]" :column="column">
                        {{ displayValue(item, column) }}
                    </slot>
                </ui5-table-cell>
            </ui5-table-row>
        </ui5-table>
    </div>
</template>


<style scoped>
.toolbar {
    position: sticky;
    top: 0;
    z-index: 2;
}

.sortable {
    cursor: pointer;
}
</style>
