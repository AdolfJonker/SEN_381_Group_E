import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  envDir: "../../",
  server: {
    port: 5173,
    proxy: {
      // Only proxy backend routes — do not catch frontend files like /api/client.js
      "/api/v1": {
        target: "http://localhost:8081",
        changeOrigin: true,
      },
    },
  },
});