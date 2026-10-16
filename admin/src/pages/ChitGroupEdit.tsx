import { useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useParams, useNavigate } from "react-router-dom";
import { Download, Send, ArrowLeft, AlertCircle } from "lucide-react";

// ── Validation schema ──────────────────────────────────────────────────────

const schema = z.object({
  name: z.string().min(3, "Name must be at least 3 characters"),
  chit_value: z.coerce.number().min(50000, "Minimum chit value is ₹50,000"),
  duration_months: z.coerce.number().min(6, "Minimum 6 months").max(60, "Maximum 60 months"),
  max_members: z.coerce.number().min(2, "Minimum 2 members").max(50, "Maximum 50 members"),
  commission_pct: z.coerce.number().min(0).max(5, "Maximum 5%"),
  first_due_date: z.string().min(1, "First due date is required"),
  payment_window_days: z.coerce.number().min(1, "Minimum 1 day").max(10, "Maximum 10 days"),
});

type FormData = z.infer<typeof schema>;

// ── Helpers ────────────────────────────────────────────────────────────────

function addMonths(dateStr: string, months: number): string {
  const d = new Date(dateStr);
  d.setMonth(d.getMonth() + months);
  return d.toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
}

function formatCurrency(val: number): string {
  if (!val || isNaN(val)) return "₹0";
  return `₹${val.toLocaleString("en-IN")}`;
}

// ── Preview schedule ───────────────────────────────────────────────────────

interface ScheduleRow {
  month: number;
  dueDate: string;
  amount: string;
}

function generateSchedule(chitValue: number, duration: number, firstDueDate: string): ScheduleRow[] {
  if (!chitValue || !duration || !firstDueDate) return [];
  const monthly = Math.round(chitValue / duration);
  return Array.from({ length: duration }, (_, i) => ({
    month: i + 1,
    dueDate: addMonths(firstDueDate, i),
    amount: formatCurrency(monthly),
  }));
}

// ── Component ──────────────────────────────────────────────────────────────

export default function ChitGroupEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [showConfirm, setShowConfirm] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: isEdit
      ? {
          name: "Family Savings 50K",
          chit_value: 50000,
          duration_months: 20,
          max_members: 20,
          commission_pct: 5,
          first_due_date: "2026-11-01",
          payment_window_days: 5,
        }
      : {
          commission_pct: 5,
          payment_window_days: 5,
        },
  });

  const watched = useWatch({ control });
  const chitValue = Number(watched.chit_value) || 0;
  const duration = Number(watched.duration_months) || 0;
  const firstDueDate = watched.first_due_date || "";
  const monthlyAmount = duration > 0 ? Math.round(chitValue / duration) : 0;
  const schedule = generateSchedule(chitValue, duration, firstDueDate);
  const previewRows = schedule.slice(0, 5);
  const extraRows = schedule.length > 5 ? schedule.length - 5 : 0;

  function showToast(msg: string) {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  }

  function onSaveDraft(data: FormData) {
    console.log("Draft saved", data);
    showToast("Group saved as draft");
  }

  function onPublishConfirm() {
    setShowConfirm(false);
    showToast("Group published successfully");
    setTimeout(() => navigate("/chit-groups"), 1000);
  }

  const inputClass =
    "w-full px-3 py-2 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6] focus:border-transparent";
  const errorClass = "text-xs text-red-500 mt-1";
  const labelClass = "block text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-1.5";

  return (
    <div className="p-6 space-y-6 relative">
      {/* Toast */}
      {toast && (
        <div className="fixed top-5 right-6 z-50 flex items-center gap-2 bg-[#073763] text-white text-sm px-4 py-2.5 rounded-lg shadow-lg">
          <span>✓</span>
          {toast}
        </div>
      )}

      {/* Header */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate("/chit-groups")}
          className="p-2 rounded-lg border border-[#E2E8F0] hover:bg-[#F1F3FF] transition-colors"
        >
          <ArrowLeft size={16} className="text-[#64748B]" />
        </button>
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">
            {isEdit ? "Edit Chit Group" : "Create Chit Group"}
          </h2>
          <p className="text-sm text-[#64748B]">{isEdit ? `Editing group ID: ${id}` : "Fill in the details to create a new chit group"}</p>
        </div>
      </div>

      {/* Two-column layout: form + preview */}
      <div className="grid grid-cols-3 gap-6">
        {/* Form — 2/3 */}
        <div className="col-span-2 space-y-5">
          {/* Section 1: Group Details */}
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
            <h3 className="font-semibold text-[#041B3C] mb-5 pb-3 border-b border-[#E2E8F0]">
              Group Details
            </h3>
            <div className="grid grid-cols-2 gap-4">
              {/* Name */}
              <div className="col-span-2">
                <label className={labelClass}>Group Name *</label>
                <input type="text" {...register("name")} className={inputClass} placeholder="e.g. Family Savings 50K" />
                {errors.name && <p className={errorClass}>{errors.name.message}</p>}
              </div>

              {/* Chit Value */}
              <div>
                <label className={labelClass}>Chit Value (₹) *</label>
                <input type="number" {...register("chit_value")} className={inputClass} placeholder="50000" min={50000} />
                {errors.chit_value && <p className={errorClass}>{errors.chit_value.message}</p>}
              </div>

              {/* Duration */}
              <div>
                <label className={labelClass}>Duration (Months) *</label>
                <input type="number" {...register("duration_months")} className={inputClass} placeholder="20" min={6} max={60} />
                {errors.duration_months && <p className={errorClass}>{errors.duration_months.message}</p>}
              </div>

              {/* Max Members */}
              <div>
                <label className={labelClass}>Max Members *</label>
                <input type="number" {...register("max_members")} className={inputClass} placeholder="20" min={2} max={50} />
                {errors.max_members && <p className={errorClass}>{errors.max_members.message}</p>}
              </div>

              {/* Commission % */}
              <div>
                <label className={labelClass}>Foreman Commission (%) *</label>
                <input type="number" {...register("commission_pct")} className={inputClass} placeholder="5" min={0} max={5} step={0.25} />
                {errors.commission_pct && <p className={errorClass}>{errors.commission_pct.message}</p>}
              </div>
            </div>
          </div>

          {/* Section 2: Installment Config */}
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
            <h3 className="font-semibold text-[#041B3C] mb-5 pb-3 border-b border-[#E2E8F0]">
              Installment Configuration
            </h3>
            <div className="grid grid-cols-2 gap-4">
              {/* Monthly Amount — read only */}
              <div>
                <label className={labelClass}>Monthly Amount (Auto-Calculated)</label>
                <div className="w-full px-3 py-2 rounded-lg border border-[#E2E8F0] bg-[#F7FAFC] text-sm font-semibold text-[#14B8A6]">
                  {monthlyAmount > 0 ? formatCurrency(monthlyAmount) : "—"}
                </div>
                <p className="text-xs text-[#64748B] mt-1">Chit Value ÷ Duration</p>
              </div>

              {/* First Due Date */}
              <div>
                <label className={labelClass}>First Due Date *</label>
                <input type="date" {...register("first_due_date")} className={inputClass} />
                {errors.first_due_date && <p className={errorClass}>{errors.first_due_date.message}</p>}
              </div>

              {/* Payment Window */}
              <div>
                <label className={labelClass}>Payment Window (Days) *</label>
                <input type="number" {...register("payment_window_days")} className={inputClass} placeholder="5" min={1} max={10} />
                {errors.payment_window_days && <p className={errorClass}>{errors.payment_window_days.message}</p>}
              </div>
            </div>
          </div>

          {/* CTAs */}
          <div className="flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => navigate("/chit-groups")}
              className="px-4 py-2 rounded-lg border border-[#E2E8F0] text-sm font-medium text-[#64748B] hover:bg-[#F1F3FF] transition-colors"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleSubmit(onSaveDraft)}
              className="flex items-center gap-2 px-4 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
            >
              <Download size={14} />
              Save as Draft
            </button>
            <button
              type="button"
              onClick={handleSubmit(() => setShowConfirm(true))}
              className="flex items-center gap-2 px-4 py-2 rounded-lg bg-[#14B8A6] text-white text-sm font-medium hover:bg-[#00B3A1] transition-colors"
            >
              <Send size={14} />
              Publish Group
            </button>
          </div>
        </div>

        {/* Preview Panel — 1/3 */}
        <div className="col-span-1">
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5 sticky top-6">
            <h3 className="font-semibold text-[#041B3C] mb-1">Installment Preview</h3>
            <p className="text-xs text-[#64748B] mb-4">Auto-generated from your configuration</p>

            {previewRows.length === 0 ? (
              <div className="py-8 text-center text-sm text-[#64748B]">
                Fill in chit value, duration, and first due date to see a preview.
              </div>
            ) : (
              <>
                <table className="w-full text-xs">
                  <thead className="bg-[#F7FAFC]">
                    <tr>
                      <th className="px-2 py-2 text-left text-[#64748B] font-semibold">Month</th>
                      <th className="px-2 py-2 text-left text-[#64748B] font-semibold">Due Date</th>
                      <th className="px-2 py-2 text-right text-[#64748B] font-semibold">Amount</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#E2E8F0]">
                    {previewRows.map((row) => (
                      <tr key={row.month}>
                        <td className="px-2 py-2 text-[#172B4D]">{row.month}</td>
                        <td className="px-2 py-2 text-[#64748B]">{row.dueDate}</td>
                        <td className="px-2 py-2 text-right font-medium text-[#14B8A6]">{row.amount}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {extraRows > 0 && (
                  <p className="text-xs text-[#64748B] text-center mt-3 pb-1">
                    … and {extraRows} more months
                  </p>
                )}
                <div className="mt-4 pt-3 border-t border-[#E2E8F0] space-y-1.5">
                  <div className="flex justify-between text-xs">
                    <span className="text-[#64748B]">Total Duration</span>
                    <span className="font-semibold text-[#041B3C]">{duration} months</span>
                  </div>
                  <div className="flex justify-between text-xs">
                    <span className="text-[#64748B]">Monthly Installment</span>
                    <span className="font-semibold text-[#14B8A6]">{formatCurrency(monthlyAmount)}</span>
                  </div>
                  <div className="flex justify-between text-xs">
                    <span className="text-[#64748B]">Total Value</span>
                    <span className="font-semibold text-[#041B3C]">{formatCurrency(chitValue)}</span>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Publish Confirmation Dialog */}
      {showConfirm && (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-xl p-6 w-[420px] relative">
            <button
              onClick={() => setShowConfirm(false)}
              className="absolute top-4 right-4 text-[#64748B] hover:text-[#041B3C]"
            >
              <AlertCircle size={16} />
            </button>
            <h3 className="font-semibold text-[#041B3C] text-lg mb-2">Publish Chit Group?</h3>
            <p className="text-sm text-[#64748B] mb-5">
              Once published, this group will be visible to members for enrollment. The group details cannot be changed after the first member enrolls.
            </p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setShowConfirm(false)}
                className="px-4 py-2 rounded-lg border border-[#E2E8F0] text-sm font-medium text-[#64748B] hover:bg-[#F1F3FF] transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={onPublishConfirm}
                className="px-4 py-2 rounded-lg bg-[#14B8A6] text-white text-sm font-medium hover:bg-[#00B3A1] transition-colors"
              >
                Yes, Publish
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
