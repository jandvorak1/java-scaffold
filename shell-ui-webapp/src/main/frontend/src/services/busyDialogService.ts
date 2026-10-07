import type { JobResponseModel } from "../api/jobs";

/**
 * Defines the dialog implementation used by the global background job service.
 */
export type BusyDialogPresenter = {

    /**
     * Displays and monitors a background job until monitoring completes.
     *
     * @param token Nonblank job identifier, validated by the presenter.
     * @param cancellable Whether the dialog offers user cancellation.
     * @param interval Positive, finite polling interval in milliseconds, at most 2147483647.
     * @returns A promise containing the final job response, including failed or
     * cancelled states. A resolved promise does not imply successful execution.
     */
    run(token: string, cancellable: boolean, interval: number): Promise<JobResponseModel>;
};

let presenter: BusyDialogPresenter | null = null;

/**
 * Sets the presenter used by subsequent calls to runBusyDialog.
 *
 * Replaces any existing registration. Passing null removes the registration.
 * This function does not stop existing monitoring or dispose of the old presenter;
 * its owner remains responsible for lifecycle cleanup.
 *
 * @param value Dialog presenter to use, or null to clear the registration.
 */
export function registerBusyDialogPresenter(value: BusyDialogPresenter | null): void {
    presenter = value;
}

/**
 * Starts monitoring through the presenter registered at the time of the call.
 *
 * Missing registration, invalid intervals, and synchronous presenter exceptions
 * are reported as promise rejections. Presenter results and rejections propagate
 * unchanged. The presenter determines how concurrent monitoring requests behave.
 *
 * @param token Job identifier passed to the presenter for validation.
 * @param cancellable Whether user cancellation is enabled; defaults to true.
 * @param interval Positive, finite polling interval in milliseconds, at most
 * 2147483647; defaults to 1000.
 * @returns The presenter's promise, or a rejected promise if delegation fails.
 * Failed and cancelled jobs may resolve normally and require checking the response state.
 */
export function runBusyDialog(token: string, cancellable: boolean = true, interval: number = 1000): Promise<JobResponseModel> {
    if (!presenter) {
        return Promise.reject(new Error("BusyDialog presenter is not registered."));
    }
    if (!Number.isFinite(interval) || interval <= 0 || interval > 2147483647) {
        return Promise.reject(new RangeError(
            "BusyDialog polling interval must be positive and at most 2147483647 milliseconds."
        ));
    }
    try {
        return presenter.run(token, cancellable, interval);
    } catch (error) {
        return Promise.reject(error);
    }
}
