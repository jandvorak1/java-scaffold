import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { AuthorizationError, requireRole, requirePermission } from "../api/authorization";
import { text } from "../api/i18n";
import { showError } from "../services/messageBoxService";

import HomeView from "../views/HomeView.vue";
import MessageView from "../views/MessageView.vue";

const routes: RouteRecordRaw[] = [
    { path: "/", component: HomeView },
    { path: "/messages", component: MessageView },
    { path: "/:pathMatch(.*)*", redirect: "/" },
];

/**
 * Provides browser-history routing with authorization checks before navigation.
 *
 * Each matched route can require a role or permission through metadata. A missing
 * permission cancels navigation and displays a localized error. Invalid
 * metadata and unexpected failures remain visible to the application.
 */
export const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach(to => {
    try {
        for (const routeRecord of to.matched) {
            const requiredRole = getMetaString(routeRecord.meta.requiredRole, "requiredRole");
            const requiredPermission = getMetaString(routeRecord.meta.requiredPermission, "requiredPermission");
            if (requiredRole) {
                requireRole(requiredRole);
            }
            if (requiredPermission) {
                requirePermission(requiredPermission);
            }
        }
        return true;
    } catch (error) {
        if (error instanceof AuthorizationError) {
            const message = error.message.trim() || text("ROUTER_INDEX_PERMISSION_MESSAGE").trim();
            showError(text("ROUTER_INDEX_PERMISSION_TITLE"), message);
            return false;
        }
        throw error;
    }
});

/**
 * Reads one optional authorization requirement from route metadata.
 *
 * @param value raw metadata value
 * @param name metadata property name used in an invalid-value error
 * @returns trimmed requirement, or null when the property is omitted
 * @throws TypeError if a present value is not a nonblank string
 */
function getMetaString(value: unknown, name: string): string | null {
    if (value === undefined) {
        return null;
    }
    if (typeof value !== "string" || !value.trim()) {
        throw new TypeError(`Route metadata ${name} must be a nonblank string when provided.`);
    }
    return value.trim();
}
