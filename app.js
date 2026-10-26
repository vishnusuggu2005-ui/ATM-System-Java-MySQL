const $ = (selector) => document.querySelector(selector);
const loginView = $("#login-view");
const dashboardView = $("#dashboard-view");
const loginMessage = $("#login-message");
const operationPanel = $("#operation-panel");
const operationTitle = $("#operation-title");
const amountForm = $("#amount-form");
const pinForm = $("#pin-form");
const historyList = $("#history-list");
const operationMessage = $("#operation-message");
let activeAction = "";

async function api(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    credentials: "same-origin",
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options
  });
  const data = response.status === 204 ? {} : await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.error || data.message || "Request failed.");
  return data;
}

function showMessage(element, message, success = false) {
  element.textContent = message;
  element.style.color = success ? "#16845b" : "#bd3345";
}
function money(value) {
  return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(Number(value));
}
async function refreshAccount() {
  const account = await api("/account");
  $("#holder-name").textContent = account.holderName;
  $("#balance").textContent = money(account.balance);
  $("#masked-account").textContent = `•••• ${String(account.accountNumber).slice(-4)}`;
}
function showDashboard() {
  loginView.classList.add("hidden");
  dashboardView.classList.remove("hidden");
}
function showLogin() {
  dashboardView.classList.add("hidden");
  loginView.classList.remove("hidden");
  closeOperation();
}
function closeOperation() {
  operationPanel.classList.add("hidden");
  amountForm.classList.add("hidden");
  pinForm.classList.add("hidden");
  historyList.classList.add("hidden");
  operationMessage.textContent = "";
  amountForm.reset();
  pinForm.reset();
  activeAction = "";
}
function openOperation(action) {
  closeOperation();
  activeAction = action;
  operationPanel.classList.remove("hidden");
  const titles = { deposit: "Deposit money", withdraw: "Withdraw money", history: "Recent transactions", pin: "Change PIN" };
  operationTitle.textContent = titles[action];
  if (action === "deposit" || action === "withdraw") {
    amountForm.classList.remove("hidden");
    $("#amount-submit").textContent = action === "deposit" ? "Confirm deposit" : "Confirm withdrawal";
  } else if (action === "pin") {
    pinForm.classList.remove("hidden");
  } else if (action === "history") {
    historyList.classList.remove("hidden");
    loadHistory();
  }
  operationPanel.scrollIntoView({ behavior: "smooth", block: "nearest" });
}
async function loadHistory() {
  historyList.innerHTML = "<p class='muted'>Loading transactions…</p>";
  try {
    const rows = await api("/transactions");
    if (!rows.length) {
      historyList.innerHTML = "<p class='muted'>No transactions yet.</p>";
      return;
    }
    historyList.innerHTML = rows.map(row => {
      const isDeposit = row.transaction_type === "DEPOSIT";
      const time = row.transaction_time ? new Date(row.transaction_time).toLocaleString() : "";
      return `<div class="history-item">
        <div><strong>${isDeposit ? "Deposit" : "Withdrawal"}</strong><small>${time}</small></div>
        <span class="${isDeposit ? "amount-positive" : "amount-negative"}">${isDeposit ? "+" : "−"}${money(row.amount)}</span>
      </div>`;
    }).join("");
  } catch (error) {
    historyList.textContent = error.message;
  }
}
$("#login-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  showMessage(loginMessage, "Signing in…");
  const accountNumber = $("#account-number").value.trim();
  const pin = $("#pin").value.trim();
  try {
    const result = await api("/login", { method: "POST", body: JSON.stringify({ accountNumber, pin }) });
    showMessage(loginMessage, result.message, true);
    await refreshAccount();
    showDashboard();
  } catch (error) {
    showMessage(loginMessage, error.message);
  }
});
document.querySelectorAll("[data-action]").forEach(button => {
  button.addEventListener("click", () => openOperation(button.dataset.action));
});
$("#close-operation").addEventListener("click", closeOperation);
$("#logout-button").addEventListener("click", async () => {
  try { await api("/logout", { method: "POST", body: "{}" }); } catch (_) {}
  showLogin();
});
amountForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const amount = Number($("#amount").value);
  if (!Number.isFinite(amount) || amount <= 0) {
    showMessage(operationMessage, "Enter a valid positive amount.");
    return;
  }
  try {
    const result = await api(activeAction === "deposit" ? "/deposit" : "/withdraw", {
      method: "POST", body: JSON.stringify({ amount: amount.toFixed(2) })
    });
    showMessage(operationMessage, result.message, true);
    await refreshAccount();
    amountForm.reset();
  } catch (error) {
    showMessage(operationMessage, error.message);
  }
});
pinForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const result = await api("/change-pin", {
      method: "POST",
      body: JSON.stringify({ currentPin: $("#current-pin").value, newPin: $("#new-pin").value })
    });
    showMessage(operationMessage, result.message, true);
    pinForm.reset();
  } catch (error) {
    showMessage(operationMessage, error.message);
  }
});
