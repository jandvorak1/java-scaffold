<script setup lang="ts">

    import { onBeforeUnmount, onMounted, provide, ref } from "vue";
    import { text } from "./api/i18n";
    import { busyOverlayKey, type BusyOverlayService } from "./composables/useBusyOverlay";
    import { useHeartbeat } from "./composables/useHeartbeat";
    import { registerBusyDialogPresenter } from "./services/busyDialogService";
    import { registerMessageBoxPresenter, showFatal } from "./services/messageBoxService";
    import { registerQueryDialogPresenter } from "./services/queryDialogService";

    import ApplicationLayout from "./layouts/ApplicationLayout.vue";
    import BusyDialog from "./components/BusyDialog.vue";
    import BusyOverlay from "./components/BusyOverlay.vue";
    import MessageBox from "./components/MessageBox.vue";
    import QueryDialog from "./components/QueryDialog.vue";

    /**
     * Public instance of the application message dialog.
     */
    type MessageBoxComponent = InstanceType<typeof MessageBox>;
    /**
     * Public instance of the application confirmation dialog.
     */
    type QueryDialogComponent = InstanceType<typeof QueryDialog>;
    /**
     * Public instance of the background job dialog.
     */
    type BusyDialogComponent = InstanceType<typeof BusyDialog>;
    /**
     * Public instance of the reference-counted busy overlay.
     */
    type BusyOverlayComponent = InstanceType<typeof BusyOverlay>;

    const ready = ref(false);
    const messageBox = ref < MessageBoxComponent | null > (null);
    const queryDialog = ref < QueryDialogComponent | null > (null);
    const busyDialog = ref < BusyDialogComponent | null > (null);
    const busyOverlay = ref < BusyOverlayComponent | null > (null);

    const heartbeat = useHeartbeat({
        statusUrl: "/api/webtray/status",
        intervalMs: 5000,
        timeoutMs: 2000,
        maxFailures: 3,
        onFailure: () => {
            showFatal(text("COMPOSABLES_HEARTBEAT_ERROR_HEADER"), text("COMPOSABLES_HEARTBEAT_ERROR_MESSAGE"));
        },
    });

    /**
     * Delegates overlay requests to the mounted component.
     * Operations still execute when the overlay is unavailable, including during teardown.
     */
    const busyOverlayService: BusyOverlayService = {
        /**
         * Acquires one visibility reference when the overlay is available.
         */
        show(): void {
            busyOverlay.value?.show();
        },
        /**
         * Releases one visibility reference when the overlay is available.
         */
        hide(): void {
            busyOverlay.value?.hide();
        },
        /**
         * Executes work with the overlay when available and propagates its outcome.
         * Synchronous operation errors are returned as promise rejections.
         *
         * @param operation Asynchronous work to execute exactly once.
         * @returns A promise carrying the operation's result or rejection.
         */
        async run<T>(operation: () => Promise<T>): Promise<T> {
            const overlay = busyOverlay.value;
            return overlay ? overlay.run(operation) : operation();
        },
    };
    provide(busyOverlayKey, busyOverlayService);

    onMounted(() => {
        registerMessageBoxPresenter(messageBox.value);
        registerQueryDialogPresenter(queryDialog.value);
        registerBusyDialogPresenter(busyDialog.value);
        ready.value = true;
        heartbeat.start();
    });

    onBeforeUnmount(() => {
        heartbeat.stop();
        registerMessageBoxPresenter(null);
        registerQueryDialogPresenter(null);
        registerBusyDialogPresenter(null);
    });
</script>


<template>
    <ApplicationLayout v-if="ready" :heartbeat="heartbeat" />
    <MessageBox ref="messageBox" />
    <QueryDialog ref="queryDialog" />
    <BusyDialog ref="busyDialog" />
    <BusyOverlay ref="busyOverlay" />
</template>
