import type { Config } from "tailwindcss";

// ChitSmart design tokens — mirrors docs/design/google-stitch-master-prompt.md
const config: Config = {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        navy: {
          DEFAULT: "#073763",
          mid: "#002141",
          deep: "#041B3C",
          light: "#1769AA",
        },
        teal: {
          DEFAULT: "#14B8A6",
          accent: "#00B3A1",
          dark: "#003E37",
        },
        surface: {
          blue: "#F1F3FF",
          white: "#FFFFFF",
          bg: "#F7FAFC",
        },
        slate: {
          body: "#172B4D",
          secondary: "#64748B",
          hint: "#737780",
        },
        success: "#22C55E",
        warning: "#F59E0B",
        error: "#EF4444",
        border: "#E2E8F0",
      },
      fontFamily: {
        sans: ["Inter", "Roboto", "system-ui", "sans-serif"],
      },
      boxShadow: {
        card: "0px 1px 2px 0px rgba(0,0,0,0.05)",
        "card-md": "0px 1px_8px 0px rgba(7,55,99,0.06)",
      },
      borderRadius: {
        card: "12px",
        chip: "20px",
      },
    },
  },
  plugins: [],
};

export default config;
