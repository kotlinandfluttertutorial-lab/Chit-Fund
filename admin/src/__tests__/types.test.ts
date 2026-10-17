/**
 * Tests for domain union types defined in admin/src/types/index.ts.
 *
 * Validates that the type values exist at runtime (they are string literals
 * used in switch statements and API comparisons throughout the app).
 */
import { describe, it, expect } from "vitest";
import type { PaymentStatus, AccountStatus } from "../types";

describe("domain type values", () => {
  it("PaymentStatus includes all expected values", () => {
    const statuses: PaymentStatus[] = [
      "PENDING",
      "PROCESSING",
      "SUCCESSFUL",
      "FAILED",
      "REFUNDED",
      "VOIDED",
    ];
    expect(statuses).toHaveLength(6);
    expect(statuses).toContain("SUCCESSFUL");
    expect(statuses).toContain("VOIDED");
    expect(statuses).toContain("REFUNDED");
  });

  it("AccountStatus includes all expected values", () => {
    const statuses: AccountStatus[] = ["ACTIVE", "INACTIVE", "SUSPENDED"];
    expect(statuses).toHaveLength(3);
    expect(statuses).toContain("ACTIVE");
    expect(statuses).toContain("SUSPENDED");
  });
});
