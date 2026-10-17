/**
 * Tests for admin/src/api/client.ts
 *
 * Validates real behaviour of the axios instance:
 *   - baseURL defaults to 'http://localhost:8000/api/v1'
 *   - 30-second timeout is configured
 *   - Content-Type header is set to application/json
 *   - Request interceptor attaches Authorization: Bearer <token> from localStorage
 *   - Request interceptor skips Authorization when no token is stored
 *
 * Uses vitest + jsdom (no server required).
 */
import { describe, it, expect, beforeEach } from "vitest";
import { apiClient } from "../api/client";

describe("apiClient — base configuration", () => {
  it("has the correct baseURL", () => {
    expect(apiClient.defaults.baseURL).toBe("http://localhost:8000/api/v1");
  });

  it("has a 30-second timeout", () => {
    expect(apiClient.defaults.timeout).toBe(30_000);
  });

  it("sets Content-Type to application/json", () => {
    expect(
      (apiClient.defaults.headers as Record<string, string>)["Content-Type"]
    ).toBe("application/json");
  });
});

describe("apiClient — request interceptor (JWT)", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("attaches Authorization: Bearer <token> when a token is stored", async () => {
    localStorage.setItem("admin_access_token", "test-jwt-token-abc123");

    // Access the first registered request interceptor handler directly.
    const handlers = (apiClient.interceptors.request as any).handlers;
    const handler = handlers[handlers.length - 1];
    const config = { headers: {} as Record<string, string> };
    const result = await handler.fulfilled(config);

    expect(result.headers["Authorization"]).toBe(
      "Bearer test-jwt-token-abc123"
    );
  });

  it("does NOT attach Authorization header when no token is stored", async () => {
    // localStorage is cleared in beforeEach — no token present.
    const handlers = (apiClient.interceptors.request as any).handlers;
    const handler = handlers[handlers.length - 1];
    const config = { headers: {} as Record<string, string> };
    const result = await handler.fulfilled(config);

    expect(result.headers["Authorization"]).toBeUndefined();
  });
});
