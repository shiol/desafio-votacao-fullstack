import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173
  },
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./src/test/setup.ts",
    css: true,
    coverage: {
      provider: "v8",
      thresholds: {
        lines: 95,
        functions: 95,
        statements: 95,
        branches: 90
      },
      include: ["src/components/**", "src/pages/**", "src/api/**", "src/routes/**"],
      exclude: ["src/main.tsx", "src/styles.css", "src/types/**", "src/test/**", "src/vite-env.d.ts"]
    }
  }
});
