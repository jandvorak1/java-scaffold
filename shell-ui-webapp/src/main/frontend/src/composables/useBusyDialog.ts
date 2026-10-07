import { runBusyDialog } from "../services/busyDialogService";

/**
 * Exposes background job monitoring through the application's registered dialog.
 */
export type BusyDialogService = {
    /**
     * Delegates job monitoring to the presenter registered at call time.
     *
     * Cancellation is enabled by default and the default polling interval is
     * 1000 milliseconds. The returned promise rejects if no presenter is registered.
     */
    run: typeof runBusyDialog;
};

/**
 * Returns access to the global busy dialog without requiring Vue injection.
 * A presenter must be registered before calling run, not before creating this facade.
 *
 * @returns A facade preserving the dialog service's arguments and result type.
 */
export function useBusyDialog(): BusyDialogService {
    return {
        run: runBusyDialog,
    };
}
