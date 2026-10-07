/**
 * Defines the presentation interface for asynchronous user confirmation.
 */
export type QueryDialogPresenter = {

    /**
     * Requests a decision through the application's query dialog.
     *
     * @param header Title displayed above the question.
     * @param message Plain-text question presented to the user.
     * @returns A promise resolved with true for Yes and false for No or dismissal.
     */
    show(header: string, message: string): Promise<boolean>;
};

let presenter: QueryDialogPresenter | null = null;

/**
 * Sets the presenter used by subsequent calls to showQuery.
 *
 * Replaces any previous registration. Passing null enables the browser confirm
 * fallback. Changing the registration does not close existing dialogs or settle
 * their promises; the previous presenter's owner remains responsible for cleanup.
 *
 * @param value Presenter to use, or null to remove the current registration.
 */
export function registerQueryDialogPresenter(value: QueryDialogPresenter | null): void {
    presenter = value;
}

/**
 * Requests confirmation through the presenter registered at call time.
 *
 * Without a presenter, calls the blocking browser confirm dialog and wraps its
 * result in a promise. The fallback uses the browser's confirmation and cancellation
 * buttons rather than the application's Yes and No labels.
 *
 * Synchronous exceptions become promise rejections. Presenter promises propagate
 * unchanged, and the presenter determines how concurrent requests are handled.
 *
 * @param header Dialog title; omitted from the fallback text when empty.
 * @param message Plain-text question, passed through without changing letter case.
 * @returns A promise resolved with true for confirmation and false for rejection
 * or dismissal, or rejected if displaying the query fails.
 */
export function showQuery(header: string, message: string): Promise<boolean> {
    try {
        if (presenter) {
            return presenter.show(header, message);
        }
        const result = window.confirm(header ? `${header}\n\n${message}` : message);
        return Promise.resolve(result);
    } catch (error) {
        return Promise.reject(error);
    }
}
