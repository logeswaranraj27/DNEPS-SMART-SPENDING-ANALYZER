/**
 * SmartSpend - Module 2: Budget Planning & Where You Could Save
 */

let currentSelectedMonth = Utils.getCurrentMonth();
let budgetsList = [];

document.addEventListener('DOMContentLoaded', () => {
  if (window.Auth && !window.Auth.requireAuth()) return;

  initBudgetEvents();
  loadBudgets();
  loadWhereYouCouldSave();
});

function initBudgetEvents() {
  const monthPicker = document.getElementById('budget-month-picker');
  if (monthPicker) {
    monthPicker.value = currentSelectedMonth;
    monthPicker.addEventListener('change', (e) => {
      currentSelectedMonth = e.target.value;
      loadBudgets();
      loadWhereYouCouldSave();
    });
  }

  const budgetForm = document.getElementById('budget-form');
  if (budgetForm) {
    budgetForm.addEventListener('submit', handleBudgetSubmit);
  }
}

/**
 * Load Budgets from Backend
 */
async function loadBudgets() {
  const container = document.getElementById('budgets-cards-container');
  if (!container) return;

  container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 2rem;">Loading budgets...</div>`;

  try {
    const data = await API.getBudgets(currentSelectedMonth);
    budgetsList = Array.isArray(data) ? data : (data.budgets || []);
    renderBudgets(budgetsList);
  } catch (err) {
    console.error("Load budgets error:", err);
    container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--color-danger); padding: 2rem;">Failed to load budgets. Check backend connection.</div>`;
  }
}

/**
 * Render Budgets with 4-tier status thresholds
 */
function renderBudgets(budgets) {
  const container = document.getElementById('budgets-cards-container');
  if (!container) return;

  if (budgets.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1;">
        <div class="empty-state">
          <div class="empty-state-icon">🎯</div>
          <div class="empty-state-title">No budgets set for this month</div>
          <p class="empty-state-desc">Set spending limits for categories like Food, Books, or Entertainment to stay on top of your college finances.</p>
          <button class="btn btn-primary" onclick="openAddBudgetModal()">+ Create Budget</button>
        </div>
      </div>
    `;
    return;
  }

  container.innerHTML = budgets.map(b => {
    const spent = b.spentAmount || 0;
    const limit = b.limitAmount || 1;
    const percent = Math.min(Math.round((spent / limit) * 100), 200);
    const remaining = Math.max(0, limit - spent);

    // 4-tier status calculation
    let statusBadge = '';
    let statusClass = 'budget-status-ok';

    if (percent < 75) {
      statusBadge = '<span class="badge badge-income">Within budget</span>';
      statusClass = 'budget-status-ok';
    } else if (percent < 100) {
      statusBadge = '<span class="badge badge-warning">Approaching limit</span>';
      statusClass = 'budget-status-warning';
    } else if (percent === 100) {
      statusBadge = '<span class="badge badge-info">Budget fully used</span>';
      statusClass = 'budget-status-full';
    } else {
      statusBadge = '<span class="badge badge-expense">Budget exceeded</span>';
      statusClass = 'budget-status-exceeded';
    }

    const isExceeded = spent > limit;
    const overAmount = spent - limit;

    return `
      <div class="budget-item-card">
        <div class="budget-card-top">
          <div>
            <div class="budget-category-name">${Utils.escapeHtml(b.category)}</div>
            <div style="font-size: 0.8rem; color: var(--slate-500);">${b.month}</div>
          </div>
          <div>${statusBadge}</div>
        </div>

        <div>
          <div style="display: flex; justify-content: space-between; font-size: 0.9rem; margin-bottom: 0.35rem;">
            <span><strong>${Utils.formatINR(spent)}</strong> spent</span>
            <span>Limit: <strong>${Utils.formatINR(limit)}</strong></span>
          </div>
          <div class="progress-track">
            <div class="progress-fill ${statusClass}" style="width: ${Math.min(percent, 100)}%;"></div>
          </div>
        </div>

        <div class="budget-card-bottom">
          <div class="budget-remaining-text">
            ${isExceeded 
              ? `<span style="color: var(--color-danger); font-weight:700;">Exceeded by ${Utils.formatINR(overAmount)}</span>`
              : `Remaining: <strong>${Utils.formatINR(remaining)}</strong> (${100 - percent}%)`}
          </div>
          <div style="display: flex; gap: 0.4rem;">
            <button class="btn btn-sm btn-secondary" onclick="openEditBudgetModal('${b.id}')">Edit</button>
            <button class="btn btn-sm btn-secondary" style="color: var(--color-danger);" onclick="confirmDeleteBudget('${b.id}')">Delete</button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

/**
 * Handle Budget Form Submission
 */
async function handleBudgetSubmit(e) {
  e.preventDefault();

  const category = document.getElementById('budget-category').value;
  const limitAmount = parseFloat(document.getElementById('budget-limit').value);
  const month = document.getElementById('budget-month').value;
  const editingId = document.getElementById('budget-id').value;

  if (isNaN(limitAmount) || limitAmount <= 0) {
    UI.showToast("Please enter a valid positive budget limit.", "error");
    return;
  }

  const payload = {
    category,
    limitAmount,
    month
  };

  try {
    if (editingId) {
      await API.updateBudget(editingId, payload);
      UI.showToast("Budget updated successfully!", "success");
    } else {
      await API.createBudget(payload);
      UI.showToast("Budget created successfully!", "success");
    }

    UI.closeModal('budget-modal');
    loadBudgets();
  } catch (err) {
    UI.showToast(err.message || "Failed to save budget.", "error");
  }
}

/**
 * Load Where You Could Save (Avoidable Expense Analysis)
 */
async function loadWhereYouCouldSave(reductionPercent = 30) {
  const container = document.getElementById('where-you-could-save-container');
  if (!container) return;

  try {
    const data = await API.getAvoidableSavingsOpportunities(currentSelectedMonth, reductionPercent);
    renderWhereYouCouldSave(data.opportunities || []);
  } catch (err) {
    console.warn("Could not load savings opportunities:", err);
  }
}

function renderWhereYouCouldSave(opportunities) {
  const container = document.getElementById('where-you-could-save-container');
  if (!container) return;

  if (!opportunities || opportunities.length === 0) {
    container.innerHTML = `
      <div style="background-color: var(--teal-50); border: 1px dashed var(--teal-500); border-radius: var(--radius-lg); padding: 1.5rem; text-align: center;">
        <h4 style="color: var(--teal-700); font-size: 1.05rem; margin-bottom: 0.35rem;">🌱 No avoidable spending patterns flagged</h4>
        <p style="font-size: 0.88rem; color: var(--slate-600); max-width: 500px; margin: 0 auto;">
          You're doing great! Keep recording your daily expenses and itemized receipts. As more records accumulate, DNEPS AI will highlight personalized areas where minor adjustments can help you save.
        </p>
      </div>
    `;
    return;
  }

  container.innerHTML = opportunities.map((opp, idx) => {
    const defaultReduction = opp.reductionPercent || 30;
    const currentSpend = opp.currentSpending || 0;
    const potentialSavings = Math.round(currentSpend * (defaultReduction / 100));
    const targetSpend = Math.max(0, currentSpend - potentialSavings);

    return `
      <div class="savings-opportunity-card" id="opp-card-${idx}">
        <div class="opportunity-header">
          <div>
            <div class="opportunity-title">💡 ${Utils.escapeHtml(opp.itemOrCategory)}</div>
            <div style="font-size: 0.82rem; color: var(--slate-500); margin-top: 0.2rem;">
              Current recorded spend: <strong>${Utils.formatINR(currentSpend)}</strong>
            </div>
          </div>
          <span class="potential-savings-badge" id="opp-savings-${idx}">Save ~${Utils.formatINR(potentialSavings)}</span>
        </div>

        <p class="opportunity-explanation">
          ${Utils.escapeHtml(opp.explanation || "By choosing small target reductions on non-essential purchases, you can redirect funds toward your college savings goals without compromising daily enjoyment.")}
        </p>

        <div class="opportunity-slider-box">
          <div class="slider-label-row">
            <span>Reduction Target: <strong id="opp-pct-label-${idx}">${defaultReduction}%</strong></span>
            <span>Target Spend: <strong id="opp-target-${idx}">${Utils.formatINR(targetSpend)}</strong></span>
          </div>
          <input type="range" class="reduction-slider" min="5" max="60" step="5" value="${defaultReduction}"
            oninput="handleReductionSliderChange(${idx}, ${currentSpend}, this.value)">
        </div>

        <div class="opportunity-actions">
          <button class="btn btn-sm btn-primary" onclick="acceptSavingsSuggestion(${idx}, '${Utils.escapeHtml(opp.itemOrCategory)}')">Accept Target</button>
          <button class="btn btn-sm btn-secondary" onclick="dismissSavingsSuggestion(${idx})">Dismiss</button>
        </div>
      </div>
    `;
  }).join('');
}

window.handleReductionSliderChange = (idx, currentSpend, percent) => {
  const pctLabel = document.getElementById(`opp-pct-label-${idx}`);
  const targetLabel = document.getElementById(`opp-target-${idx}`);
  const savingsBadge = document.getElementById(`opp-savings-${idx}`);

  const potentialSavings = Math.round(currentSpend * (percent / 100));
  const targetSpend = Math.max(0, currentSpend - potentialSavings);

  if (pctLabel) pctLabel.textContent = `${percent}%`;
  if (targetLabel) targetLabel.textContent = Utils.formatINR(targetSpend);
  if (savingsBadge) savingsBadge.textContent = `Save ~${Utils.formatINR(potentialSavings)}`;
};

window.acceptSavingsSuggestion = (idx, itemOrCategory) => {
  UI.showToast(`Savings target for ${itemOrCategory} accepted! Added to your weekly review.`, "success");
  dismissSavingsSuggestion(idx);
};

window.dismissSavingsSuggestion = (idx) => {
  const card = document.getElementById(`opp-card-${idx}`);
  if (card) {
    card.style.opacity = '0';
    card.style.transform = 'translateY(-10px)';
    setTimeout(() => card.remove(), 250);
  }
};

window.openAddBudgetModal = () => {
  document.getElementById('budget-modal-title').textContent = 'Create Budget';
  document.getElementById('budget-form').reset();
  document.getElementById('budget-id').value = '';
  document.getElementById('budget-month').value = currentSelectedMonth;
  UI.openModal('budget-modal');
};

window.openEditBudgetModal = (id) => {
  const b = budgetsList.find(item => item.id === id);
  if (!b) return;

  document.getElementById('budget-modal-title').textContent = 'Edit Budget';
  document.getElementById('budget-id').value = b.id;
  document.getElementById('budget-category').value = b.category;
  document.getElementById('budget-limit').value = b.limitAmount;
  document.getElementById('budget-month').value = b.month;
  UI.openModal('budget-modal');
};

window.confirmDeleteBudget = (id) => {
  UI.confirm('Delete Budget', 'Are you sure you want to delete this budget?', async () => {
    try {
      await API.deleteBudget(id);
      UI.showToast("Budget deleted successfully.", "success");
      loadBudgets();
    } catch (e) {
      UI.showToast(e.message || "Failed to delete budget.", "error");
    }
  });
};
