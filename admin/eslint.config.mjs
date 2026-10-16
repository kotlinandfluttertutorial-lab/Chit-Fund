// ESLint 9 flat configuration for ChitSmart admin dashboard.
// Uses only packages installed in devDependencies:
//   eslint@^9, @typescript-eslint/eslint-plugin, @typescript-eslint/parser,
//   eslint-plugin-react-hooks, @eslint/js (bundled with eslint 9),
//   globals (installed alongside eslint 9).
import js from "@eslint/js";
import globals from "globals";
import tsPlugin from "@typescript-eslint/eslint-plugin";
import tsParser from "@typescript-eslint/parser";
import reactHooks from "eslint-plugin-react-hooks";

export default [
  // Ignore build output and dependencies
  {
    ignores: ["dist/**", "node_modules/**"],
  },

  // Base JS recommended rules — applies to all files
  js.configs.recommended,

  // TypeScript + React source files — browser environment
  {
    files: ["src/**/*.{ts,tsx}"],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaVersion: 2022,
        sourceType: "module",
      },
      globals: {
        ...globals.browser,
      },
    },
    plugins: {
      "@typescript-eslint": tsPlugin,
      "react-hooks": reactHooks,
    },
    rules: {
      ...tsPlugin.configs.recommended.rules,
      ...reactHooks.configs.recommended.rules,
      // Relax no-explicit-any to warn only (codebase uses it in a few places)
      "@typescript-eslint/no-explicit-any": "warn",
      "@typescript-eslint/no-unused-vars": [
        "error",
        { argsIgnorePattern: "^_", varsIgnorePattern: "^_" },
      ],
    },
  },

  // Test files — add vitest globals, relax some rules
  {
    files: ["src/**/__tests__/**/*.{ts,tsx}", "src/**/*.{test,spec}.{ts,tsx}"],
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
      },
    },
    rules: {
      "@typescript-eslint/no-explicit-any": "off",
    },
  },
];
