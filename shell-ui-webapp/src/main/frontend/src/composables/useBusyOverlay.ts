import { hasInjectionContext, inject, type InjectionKey } from "vue";

/**
 * Defines reference-counted operations exposed by the application's busy overlay.
 */
export type BusyOverlayService = {
    /**
     * Acquires one visibility reference. Each call must be balanced by hide.
     */
    show(): void;

    /**
     * Releases one visibility reference, keeping the overlay visible while
     * other references remain. Has no effect when no references are held.
     */
    hide(): void;

    /**
     * Holds a visibility reference until the supplied operation settles.
     * Concurrent calls keep the overlay visible until all operations finish.
     *
     * @param operation Asynchronous work to execute under the overlay.
     * @returns A promise preserving the operation's result or rejection.
     */
    run<T>(operation: () => Promise<T>): Promise<T>;
};

/**
 * Identifies the busy overlay provider in Vue's dependency injection hierarchy.
 */
export const busyOverlayKey: InjectionKey<BusyOverlayService> = Symbol("busyOverlay");

/**
 * Resolves the nearest busy overlay provider from the current Vue injection context.
 * Call during component setup or within an application injection context.
 *
 * @returns The service registered under busyOverlayKey.
 * @throws Error If there is no injection context or no matching provider.
 */
export function useBusyOverlay(): BusyOverlayService {
    if (!hasInjectionContext()) {
        throw new Error("BusyOverlay service requires a Vue injection context.");
    }
    const busyOverlay = inject(busyOverlayKey, undefined);
    if (!busyOverlay) {
        throw new Error("BusyOverlay service is not provided.");
    }
    return busyOverlay;
}
