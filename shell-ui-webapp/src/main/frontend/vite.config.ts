import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
    base: "/bundle/",
    plugins: [
        vue({
            template: {
                compilerOptions: {
                    isCustomElement: tag => tag.startsWith("ui5-"),
                },
            },
        }),
    ],
    build: {
        outDir: "../resources/bundle",
        emptyOutDir: true,
        chunkSizeWarningLimit: 1000,
        cssCodeSplit: true,
        rolldownOptions: {
            input: "src/main.ts",
            output: {
                entryFileNames: "scripts/app.js",
                chunkFileNames: "scripts/app-[name]-[hash].js",
                codeSplitting: true,
                assetFileNames: (assetInfo) => {
                    const name = assetInfo.names?.[0] ?? "";
                    if (name.endsWith(".css")) {
                        return "styles/app.css";
                    }
                    if ([".woff", ".woff2", ".ttf", ".eot"].some((ext) => name.endsWith(ext))) {
                        return "fonts/[name][extname]";
                    }
                    return "other/[name]-[hash][extname]";
                },
            },
        },
    },
});