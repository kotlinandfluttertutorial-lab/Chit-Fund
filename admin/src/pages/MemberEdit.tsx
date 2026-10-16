import { useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, ArrowRight, Check, Plus, Trash2, Save, Send } from "lucide-react";

// ── Schemas per step ───────────────────────────────────────────────────────

const personalSchema = z.object({
  full_name: z.string().min(2, "Name must be at least 2 characters"),
  date_of_birth: z.string().optional(),
  gender: z.enum(["MALE", "FEMALE", "OTHER", "PREFER_NOT_TO_SAY"]).optional(),
});

const contactSchema = z.object({
  mobile: z.string().regex(/^[6-9]\d{9}$/, "Enter a valid 10-digit Indian mobile number"),
  email: z.string().email("Invalid email address").optional().or(z.literal("")),
  alternate_mobile: z.string().regex(/^[6-9]\d{9}$/, "Invalid mobile").optional().or(z.literal("")),
  preferred_language: z.enum(["en", "hi", "ta", "te", "kn", "ml"]),
  street: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  pincode: z.string().regex(/^\d{6}$/, "Enter a valid 6-digit pincode").optional().or(z.literal("")),
});

const fullSchema = personalSchema.merge(contactSchema);
type FormData = z.infer<typeof fullSchema>;

// ── Bank account type ──────────────────────────────────────────────────────

interface BankAccount {
  id: string;
  bank_name: string;
  account_number: string;
  ifsc: string;
  account_type: string;
  upi_vpa?: string;
  is_primary: boolean;
}

// ── Helpers ────────────────────────────────────────────────────────────────

const inputClass =
  "w-full px-3 py-2 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6] focus:border-transparent";
const errorClass = "text-xs text-red-500 mt-1";
const labelClass = "block text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-1.5";

const STEPS = [
  { label: "Personal Info" },
  { label: "Contact" },
  { label: "Bank Accounts" },
  { label: "Review" },
];

// ── Step indicator ─────────────────────────────────────────────────────────

function StepIndicator({ current }: { current: number }) {
  return (
    <div className="flex items-center gap-0 mb-6">
      {STEPS.map((step, i) => {
        const done = i < current;
        const active = i === current;
        return (
          <div key={step.label} className="flex items-center">
            <div className="flex flex-col items-center">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold border-2 ${
                  done
                    ? "bg-[#14B8A6] border-[#14B8A6] text-white"
                    : active
                    ? "bg-white border-[#14B8A6] text-[#14B8A6]"
                    : "bg-white border-[#E2E8F0] text-[#64748B]"
                }`}
              >
                {done ? <Check size={14} /> : i + 1}
              </div>
              <span
                className={`text-xs mt-1 font-medium ${
                  active ? "text-[#14B8A6]" : done ? "text-[#041B3C]" : "text-[#64748B]"
                }`}
              >
                {step.label}
              </span>
            </div>
            {i < STEPS.length - 1 && (
              <div className={`h-0.5 w-16 mb-5 mx-1 ${i < current ? "bg-[#14B8A6]" : "bg-[#E2E8F0]"}`} />
            )}
          </div>
        );
      })}
    </div>
  );
}

// ── Component ──────────────────────────────────────────────────────────────

export default function MemberEditPage() {
  const { memberId } = useParams<{ memberId: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(memberId);
  const [step, setStep] = useState(0);
  const [toast, setToast] = useState<string | null>(null);

  // Bank accounts state
  const [bankAccounts, setBankAccounts] = useState<BankAccount[]>([]);
  const [showBankForm, setShowBankForm] = useState(false);
  const [newBank, setNewBank] = useState<Omit<BankAccount, "id" | "is_primary">>({
    bank_name: "",
    account_number: "",
    ifsc: "",
    account_type: "SAVINGS",
    upi_vpa: "",
  });

  const {
    register,
    handleSubmit,
    trigger,
    getValues,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(fullSchema),
    defaultValues: isEdit
      ? {
          full_name: "Ravi Kumar",
          date_of_birth: "1985-06-15",
          gender: "MALE",
          mobile: "9876543210",
          email: "ravi.kumar@email.com",
          preferred_language: "en",
          street: "12 MG Road",
          city: "Bengaluru",
          state: "Karnataka",
          pincode: "560001",
        }
      : { preferred_language: "en" },
  });

  function showToast(msg: string) {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  }

  async function handleNext() {
    let fieldsToValidate: (keyof FormData)[] = [];
    if (step === 0) fieldsToValidate = ["full_name"];
    if (step === 1) fieldsToValidate = ["mobile", "email"];
    const valid = await trigger(fieldsToValidate);
    if (valid) setStep((s) => s + 1);
  }

  function addBankAccount() {
    if (!newBank.bank_name || !newBank.account_number || !newBank.ifsc) return;
    setBankAccounts((prev) => [
      ...prev,
      { ...newBank, id: Date.now().toString(), is_primary: prev.length === 0 },
    ]);
    setNewBank({ bank_name: "", account_number: "", ifsc: "", account_type: "SAVINGS", upi_vpa: "" });
    setShowBankForm(false);
  }

  function removeBankAccount(id: string) {
    setBankAccounts((prev) => prev.filter((b) => b.id !== id));
  }

  function setPrimary(id: string) {
    setBankAccounts((prev) => prev.map((b) => ({ ...b, is_primary: b.id === id })));
  }

  function onSaveDraft(data: FormData) {
    console.log("Draft saved", data, bankAccounts);
    showToast("Member saved as draft");
  }

  function onSubmit(data: FormData) {
    console.log("Submitted", data, bankAccounts);
    showToast("Member saved successfully");
    setTimeout(() => navigate("/members"), 1000);
  }

  const values = getValues();

  return (
    <div className="p-6 space-y-6 relative">
      {/* Toast */}
      {toast && (
        <div className="fixed top-5 right-6 z-50 flex items-center gap-2 bg-[#073763] text-white text-sm px-4 py-2.5 rounded-lg shadow-lg">
          <Check size={14} />
          {toast}
        </div>
      )}

      {/* Header */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate("/members")}
          className="p-2 rounded-lg border border-[#E2E8F0] hover:bg-[#F1F3FF] transition-colors"
        >
          <ArrowLeft size={16} className="text-[#64748B]" />
        </button>
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">
            {isEdit ? "Edit Member" : "Add New Member"}
          </h2>
          <p className="text-sm text-[#64748B]">
            {isEdit ? `Editing member ID: ${memberId}` : "Create a new member profile"}
          </p>
        </div>
      </div>

      {/* Stepper */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
        <StepIndicator current={step} />

        {/* ── Step 0: Personal Info ── */}
        {step === 0 && (
          <div className="space-y-4">
            <h3 className="font-semibold text-[#041B3C] mb-4">Personal Information</h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="col-span-2">
                <label className={labelClass}>Full Name *</label>
                <input type="text" {...register("full_name")} className={inputClass} placeholder="e.g. Ravi Kumar" />
                {errors.full_name && <p className={errorClass}>{errors.full_name.message}</p>}
              </div>
              <div>
                <label className={labelClass}>Date of Birth</label>
                <input type="date" {...register("date_of_birth")} className={inputClass} />
              </div>
              <div>
                <label className={labelClass}>Gender</label>
                <select {...register("gender")} className={inputClass}>
                  <option value="">Select gender</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                  <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
                </select>
              </div>
            </div>
          </div>
        )}

        {/* ── Step 1: Contact ── */}
        {step === 1 && (
          <div className="space-y-4">
            <h3 className="font-semibold text-[#041B3C] mb-4">Contact Details</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Mobile Number *</label>
                <input type="tel" {...register("mobile")} className={inputClass} placeholder="9876543210" maxLength={10} />
                {errors.mobile && <p className={errorClass}>{errors.mobile.message}</p>}
              </div>
              <div>
                <label className={labelClass}>Email Address</label>
                <input type="email" {...register("email")} className={inputClass} placeholder="email@example.com" />
                {errors.email && <p className={errorClass}>{errors.email.message}</p>}
              </div>
              <div>
                <label className={labelClass}>Alternate Mobile</label>
                <input type="tel" {...register("alternate_mobile")} className={inputClass} placeholder="9876543210" maxLength={10} />
                {errors.alternate_mobile && <p className={errorClass}>{errors.alternate_mobile.message}</p>}
              </div>
              <div>
                <label className={labelClass}>Preferred Language</label>
                <select {...register("preferred_language")} className={inputClass}>
                  <option value="en">English</option>
                  <option value="hi">Hindi</option>
                  <option value="ta">Tamil</option>
                  <option value="te">Telugu</option>
                  <option value="kn">Kannada</option>
                  <option value="ml">Malayalam</option>
                </select>
              </div>
              <div className="col-span-2">
                <label className={labelClass}>Street Address</label>
                <input type="text" {...register("street")} className={inputClass} placeholder="House no, street name" />
              </div>
              <div>
                <label className={labelClass}>City</label>
                <input type="text" {...register("city")} className={inputClass} placeholder="Bengaluru" />
              </div>
              <div>
                <label className={labelClass}>State</label>
                <input type="text" {...register("state")} className={inputClass} placeholder="Karnataka" />
              </div>
              <div>
                <label className={labelClass}>Pincode</label>
                <input type="text" {...register("pincode")} className={inputClass} placeholder="560001" maxLength={6} />
                {errors.pincode && <p className={errorClass}>{errors.pincode.message}</p>}
              </div>
            </div>
          </div>
        )}

        {/* ── Step 2: Bank Accounts ── */}
        {step === 2 && (
          <div>
            <h3 className="font-semibold text-[#041B3C] mb-4">Bank Accounts</h3>
            {/* Existing accounts */}
            {bankAccounts.length === 0 && !showBankForm && (
              <p className="text-sm text-[#64748B] mb-4">No bank accounts added yet.</p>
            )}
            {bankAccounts.map((acc) => (
              <div key={acc.id} className="flex items-center justify-between bg-[#F7FAFC] rounded-lg border border-[#E2E8F0] p-4 mb-3">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-semibold text-[#041B3C]">{acc.bank_name}</span>
                    {acc.is_primary && (
                      <span className="text-xs bg-[#14B8A6]/10 text-[#14B8A6] px-2 py-0.5 rounded-full font-medium">Primary</span>
                    )}
                  </div>
                  <p className="text-xs text-[#64748B] mt-0.5">
                    {acc.account_number} · {acc.ifsc} · {acc.account_type}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  {!acc.is_primary && (
                    <button
                      onClick={() => setPrimary(acc.id)}
                      className="text-xs text-[#14B8A6] hover:underline"
                    >
                      Set Primary
                    </button>
                  )}
                  <button
                    onClick={() => removeBankAccount(acc.id)}
                    className="p-1.5 rounded text-red-400 hover:bg-red-50 transition-colors"
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>
            ))}

            {/* Inline add-bank form */}
            {showBankForm ? (
              <div className="bg-white rounded-lg border border-[#14B8A6] p-4 space-y-3">
                <h4 className="text-sm font-semibold text-[#041B3C]">New Bank Account</h4>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className={labelClass}>Bank Name</label>
                    <input
                      type="text"
                      value={newBank.bank_name}
                      onChange={(e) => setNewBank((b) => ({ ...b, bank_name: e.target.value }))}
                      className={inputClass}
                      placeholder="e.g. HDFC Bank"
                    />
                  </div>
                  <div>
                    <label className={labelClass}>Account Number</label>
                    <input
                      type="text"
                      value={newBank.account_number}
                      onChange={(e) => setNewBank((b) => ({ ...b, account_number: e.target.value }))}
                      className={inputClass}
                      placeholder="1234567890"
                    />
                  </div>
                  <div>
                    <label className={labelClass}>IFSC Code</label>
                    <input
                      type="text"
                      value={newBank.ifsc}
                      onChange={(e) => setNewBank((b) => ({ ...b, ifsc: e.target.value.toUpperCase() }))}
                      className={inputClass}
                      placeholder="HDFC0001234"
                    />
                  </div>
                  <div>
                    <label className={labelClass}>Account Type</label>
                    <select
                      value={newBank.account_type}
                      onChange={(e) => setNewBank((b) => ({ ...b, account_type: e.target.value }))}
                      className={inputClass}
                    >
                      <option value="SAVINGS">Savings</option>
                      <option value="CURRENT">Current</option>
                    </select>
                  </div>
                  <div>
                    <label className={labelClass}>UPI VPA (optional)</label>
                    <input
                      type="text"
                      value={newBank.upi_vpa}
                      onChange={(e) => setNewBank((b) => ({ ...b, upi_vpa: e.target.value }))}
                      className={inputClass}
                      placeholder="name@upi"
                    />
                  </div>
                </div>
                <div className="flex gap-2 justify-end pt-1">
                  <button
                    onClick={() => setShowBankForm(false)}
                    className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#64748B] hover:bg-[#F1F3FF]"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={addBankAccount}
                    className="px-3 py-1.5 rounded-lg bg-[#14B8A6] text-white text-sm hover:bg-[#00B3A1]"
                  >
                    Add Account
                  </button>
                </div>
              </div>
            ) : (
              <button
                onClick={() => setShowBankForm(true)}
                className="flex items-center gap-2 text-sm font-medium text-[#14B8A6] hover:text-[#00B3A1] mt-2"
              >
                <Plus size={14} />
                Add Bank Account
              </button>
            )}
          </div>
        )}

        {/* ── Step 3: Review ── */}
        {step === 3 && (
          <div>
            <h3 className="font-semibold text-[#041B3C] mb-4">Review Information</h3>
            <div className="grid grid-cols-2 gap-6">
              {/* Personal */}
              <div className="bg-[#F7FAFC] rounded-lg border border-[#E2E8F0] p-4">
                <h4 className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-3">Personal</h4>
                <dl className="space-y-2">
                  {[
                    { label: "Full Name", value: values.full_name },
                    { label: "Date of Birth", value: values.date_of_birth || "—" },
                    { label: "Gender", value: values.gender || "—" },
                  ].map(({ label, value }) => (
                    <div key={label} className="flex justify-between text-sm">
                      <dt className="text-[#64748B]">{label}</dt>
                      <dd className="font-medium text-[#041B3C]">{value}</dd>
                    </div>
                  ))}
                </dl>
              </div>
              {/* Contact */}
              <div className="bg-[#F7FAFC] rounded-lg border border-[#E2E8F0] p-4">
                <h4 className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-3">Contact</h4>
                <dl className="space-y-2">
                  {[
                    { label: "Mobile", value: values.mobile },
                    { label: "Email", value: values.email || "—" },
                    { label: "Language", value: values.preferred_language },
                    { label: "City", value: values.city || "—" },
                    { label: "State", value: values.state || "—" },
                    { label: "Pincode", value: values.pincode || "—" },
                  ].map(({ label, value }) => (
                    <div key={label} className="flex justify-between text-sm">
                      <dt className="text-[#64748B]">{label}</dt>
                      <dd className="font-medium text-[#041B3C]">{value}</dd>
                    </div>
                  ))}
                </dl>
              </div>
              {/* Bank accounts */}
              <div className="col-span-2 bg-[#F7FAFC] rounded-lg border border-[#E2E8F0] p-4">
                <h4 className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-3">
                  Bank Accounts ({bankAccounts.length})
                </h4>
                {bankAccounts.length === 0 ? (
                  <p className="text-sm text-[#64748B]">No bank accounts added.</p>
                ) : (
                  <div className="space-y-2">
                    {bankAccounts.map((acc) => (
                      <div key={acc.id} className="flex items-center justify-between text-sm">
                        <span className="text-[#041B3C]">
                          {acc.bank_name} — {acc.account_number}
                        </span>
                        {acc.is_primary && (
                          <span className="text-xs bg-[#14B8A6]/10 text-[#14B8A6] px-2 py-0.5 rounded-full font-medium">Primary</span>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Final CTAs */}
            <div className="flex items-center justify-end gap-3 mt-6 pt-4 border-t border-[#E2E8F0]">
              <button
                onClick={handleSubmit(onSaveDraft)}
                className="flex items-center gap-2 px-4 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
              >
                <Save size={14} />
                Save as Draft
              </button>
              <button
                onClick={handleSubmit(onSubmit)}
                className="flex items-center gap-2 px-4 py-2 rounded-lg bg-[#14B8A6] text-white text-sm font-medium hover:bg-[#00B3A1] transition-colors"
              >
                <Send size={14} />
                Submit Member
              </button>
            </div>
          </div>
        )}

        {/* Navigation */}
        {step < 3 && (
          <div className="flex items-center justify-between mt-8 pt-4 border-t border-[#E2E8F0]">
            <button
              onClick={() => setStep((s) => Math.max(0, s - 1))}
              disabled={step === 0}
              className="flex items-center gap-2 px-4 py-2 rounded-lg border border-[#E2E8F0] text-sm font-medium text-[#64748B] hover:bg-[#F1F3FF] disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              <ArrowLeft size={14} />
              Back
            </button>
            <button
              onClick={handleNext}
              className="flex items-center gap-2 px-4 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors"
            >
              Next
              <ArrowRight size={14} />
            </button>
          </div>
        )}
        {step === 3 && (
          <div className="flex items-center justify-start mt-6">
            <button
              onClick={() => setStep((s) => Math.max(0, s - 1))}
              className="flex items-center gap-2 px-4 py-2 rounded-lg border border-[#E2E8F0] text-sm font-medium text-[#64748B] hover:bg-[#F1F3FF] transition-colors"
            >
              <ArrowLeft size={14} />
              Back
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
