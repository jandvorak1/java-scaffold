import "vue-router";

declare module "vue-router" {
    /**
     * Adds authorization requirements checked by the application's navigation guard.
     *
     * Requirements from every matched route record must be satisfied. When a
     * record specifies both a role and a permission, both checks must pass.
     */
    interface RouteMeta {
        /**
         * Case-sensitive role identifier required by this route record.
         *
         * The guard trims surrounding whitespace and rejects blank values at
         * runtime. Omitting this property adds no role requirement for this
         * record and does not remove requirements declared by parent records.
         */
        requiredRole?: string;

        /**
         * Case-sensitive permission identifier required by this route record.
         *
         * The guard trims surrounding whitespace and rejects blank values at
         * runtime. Omitting this property adds no permission requirement for this
         * record and does not remove requirements declared by parent records.
         */
        requiredPermission?: string;
    }
}
