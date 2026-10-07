/**
 * Defines the synchronous presentation interface for application messages.
 * The implementation controls dialog appearance and visibility.
 */
export type MessageBoxPresenter = {

    /**
     * Presents a notification of successful completion.
     *
     * @param header Title displayed above the message.
     * @param message Plain-text message content.
     */
    success(header: string, message: string): void;

    /**
     * Presents a neutral informational notification.
     *
     * @param header Title displayed above the message.
     * @param message Plain-text message content.
     */
    info(header: string, message: string): void;

    /**
     * Presents a warning requiring the user's attention.
     *
     * @param header Title displayed above the message.
     * @param message Plain-text message content.
     */
    warning(header: string, message: string): void;

    /**
     * Presents a recoverable error notification.
     *
     * @param header Title displayed above the message.
     * @param message Plain-text message content.
     */
    error(header: string, message: string): void;

    /**
     * Presents a fatal error that the application dialog prevents the user from closing.
     *
     * @param header Title displayed above the message.
     * @param message Plain-text message content.
     */
    fatal(header: string, message: string): void;
};

// Currently registered message box presenter.
let presenter: MessageBoxPresenter | null = null;

/**
 * Sets the presenter used by subsequent message requests.
 *
 * Replaces the current registration without closing dialogs or disposing of the
 * previous presenter. Passing null enables the browser alert fallback.
 *
 * @param value Presenter to use, or null to remove the current registration.
 */
export function registerMessageBoxPresenter(value: MessageBoxPresenter | null): void {
    presenter = value;
}

/**
 * Displays a successful-operation notification through the current presenter.
 *
 * Uses a browser alert if no presenter is registered. Presenter exceptions
 * propagate to the caller.
 *
 * @param header Title displayed above the message.
 * @param message Plain-text message content.
 */
export function showSuccess(header: string, message: string): void {
    showMessage("success", header, message);
}

/**
 * Displays an informational notification through the current presenter.
 *
 * Uses a browser alert if no presenter is registered. Presenter exceptions
 * propagate to the caller.
 *
 * @param header Title displayed above the message.
 * @param message Plain-text message content.
 */
export function showInfo(header: string, message: string): void {
    showMessage("info", header, message);
}

/**
 * Displays a warning notification through the current presenter.
 *
 * Uses a browser alert if no presenter is registered. Presenter exceptions
 * propagate to the caller.
 *
 * @param header Title displayed above the message.
 * @param message Plain-text message content.
 */
export function showWarning(header: string, message: string): void {
    showMessage("warning", header, message);
}

/**
 * Displays a recoverable error notification through the current presenter.
 *
 * Removes leading message whitespace without changing letter case.
 *
 * Uses a browser alert if no presenter is registered. Presenter exceptions
 * propagate to the caller.
 *
 * @param header Title displayed above the message.
 * @param message Plain-text message content.
 */
export function showError(header: string, message: string): void {
    showMessage("error", header, message);
}

/**
 * Displays a fatal error through the current presenter.
 *
 * Removes leading message whitespace without changing letter case.
 *
 * Uses a browser alert if no presenter is registered. Presenter exceptions
 * propagate to the caller.
 * The alert remains dismissible and cannot enforce the application dialog
 * behavior that prevents user closing.
 *
 * @param header Title displayed above the message.
 * @param message Plain-text message content.
 */
export function showFatal(header: string, message: string): void {
    showMessage("fatal", header, message);
}

function showMessage(type: keyof MessageBoxPresenter, header: string, message: string): void {
    if (type === "error" || type === "fatal") {
        message = message.trimStart();
    }
    if (presenter) {
        presenter[type](header, message);
        return;
    }
    window.alert(header ? `${header}\n\n${message}` : message);
}
