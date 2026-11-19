/**
 * SmartSpend - Module 3: Savings Goals Management
 */

let goalsList = [];

document.addEventListener('DOMContentLoaded', () => {
  if (window.Auth && !window.Auth.requireAuth()) return;

  initSavingsEvents();
  loadSavingsGoals();
});

function initSavingsEvents() {
  const form = document.getElementById('savings-goal-form');
  if (form) {
    form.addEventListener('submit', handleGoalSubmit);
  }

  const contribForm = document.getElementById('goal-contribution-form');
  if (contribForm) {
    contribForm.addEventListener('submit', handleContributionSubmit);
  }
}

async function loadSavingsGoals() {
  const container = document.getElementById('savings-goals-container');
  if (!container) return;

  container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 2rem;">Loading savings goals...</div>`;

  try {
    const data = await API.getSavingsGoals();
    goalsList = Array.isArray(data) ? data : (data.goals || []);
    renderSavingsGoals(goalsList);
  } catch (err) {
    console.error("Load savings goals error:", err);
    container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--color-danger); padding: 2rem;">Failed to load goals. Check backend connection.</div>`;
  }
}

function renderSavingsGoals(goals) {
  const container = document.getElementById('savings-goals-container');
  if (!container) return;

  if (goals.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1;">
        <div class="empty-state">
          <div class="empty-state-icon">💰</div>
          <div class="empty-state-title">No savings goals yet</div>
          <p class="empty-state-desc">Plan for things like textbooks, tech gear, semester trips, or an emergency fund.</p>
          <button class="btn btn-primary" onclick="openAddGoalModal()">+ Create Savings Goal</button>
        </div>
      </div>
    `;
    return;
  }

  container.innerHTML = goals.map(g => {
    const target = g.targetAmount || 1;
    const saved = g.currentAmount || 0;
    const percent = Math.min(Math.round((saved / target) * 100), 100);
    const remaining = Math.max(0, target - saved);
    const isCompleted = saved >= target;

    // Remaining days calculation
    let daysLeft = g.targetDate ? Utils.daysBetween(Utils.getTodayDate(), g.targetDate) : null;
    let timingBadge = '';
    let recommendation = '';

    if (isCompleted) {
      timingBadge = '<span class="badge badge-income">🎉 Fully Funded</span>';
      recommendation = 'Congratulations! You reached this financial milestone!';
    } else if (daysLeft !== null) {
      if (daysLeft < 0) {
        timingBadge = '<span class="badge badge-expense">Overdue</span>';
        recommendation = `Target date passed ${Math.abs(daysLeft)} days ago. Keep saving!`;
      } else if (daysLeft === 0) {
        timingBadge = '<span class="badge badge-warning">Target is Today</span>';
        recommendation = `Save ${Utils.formatINR(remaining)} today to hit your goal.`;
      } else {
        const weeksLeft = Math.max(1, Math.ceil(daysLeft / 7));
        const weeklyRate = Math.ceil(remaining / weeksLeft);
        timingBadge = `<span class="badge badge-info">${daysLeft} days left</span>`;
        recommendation = `Save <strong>${Utils.formatINR(weeklyRate)}/week</strong> for ${weeksLeft} weeks to reach your target.`;
      }
    } else {
      recommendation = `Save as you can towards your ${Utils.formatINR(target)} target.`;
    }

    return `
      <div class="goal-card">
        <div class="goal-header">
          <div>
            <div class="goal-name">${Utils.escapeHtml(g.name)}</div>
            ${g.description ? `<div style="font-size: 0.82rem; color: var(--slate-500);">${Utils.escapeHtml(g.description)}</div>` : ''}
            <div class="goal-target-date">📅 Target: ${g.targetDate ? Utils.formatDate(g.targetDate) : 'No date'}</div>
          </div>
          <div>${timingBadge}</div>
        </div>

        <div>
          <div class="goal-amounts-row">
            <div>
              <span style="font-size: 0.78rem; color: var(--slate-500); text-transform: uppercase;">Saved</span>
              <div class="goal-current-amount">${Utils.formatINR(saved)}</div>
            </div>
            <div style="text-align: right;">
              <span style="font-size: 0.78rem; color: var(--slate-500); text-transform: uppercase;">Target</span>
              <div class="goal-target-amount">${Utils.formatINR(target)}</div>
            </div>
          </div>

          <div class="progress-track" style="height: 10px; margin-top: 0.5rem;">
            <div class="progress-fill" style="width: ${percent}%; background-color: ${isCompleted ? 'var(--color-success)' : 'var(--color-indigo)'};"></div>
          </div>
          <div style="text-align: right; font-size: 0.8rem; font-weight: 600; color: var(--slate-600); margin-top: 0.25rem;">
            ${percent}% completed
          </div>
        </div>

        <div class="goal-recommendation-box">
          <span>💡</span>
          <div>${recommendation}</div>
        </div>

        <div style="display: flex; gap: 0.5rem; justify-content: space-between; margin-top: auto; padding-top: 0.5rem; border-top: 1px solid var(--border-color);">
          <button class="btn btn-sm btn-primary" onclick="openContributeModal('${g.id}', '${Utils.escapeHtml(g.name)}', ${remaining})" ${isCompleted ? 'disabled' : ''}>+ Add Savings</button>
          <div style="display: flex; gap: 0.35rem;">
            <button class="btn btn-sm btn-secondary" onclick="openEditGoalModal('${g.id}')">Edit</button>
            <button class="btn btn-sm btn-secondary" style="color: var(--color-danger);" onclick="confirmDeleteGoal('${g.id}')">Delete</button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

async function handleGoalSubmit(e) {
  e.preventDefault();

  const name = document.getElementById('goal-name').value.trim();
  const description = document.getElementById('goal-desc').value.trim();
  const targetAmount = parseFloat(document.getElementById('goal-target').value);
  const currentAmount = parseFloat(document.getElementById('goal-initial-saved').value) || 0;
  const targetDate = document.getElementById('goal-target-date').value;
  const editingId = document.getElementById('goal-id').value;

  if (!name) {
    UI.showToast("Please enter a goal name.", "error");
    return;
  }
  if (isNaN(targetAmount) || targetAmount <= 0) {
    UI.showToast("Please enter a valid positive target amount.", "error");
    return;
  }

  const payload = {
    name,
    description,
    targetAmount,
    currentAmount,
    targetDate
  };

  try {
    if (editingId) {
      await API.updateSavingsGoal(editingId, payload);
      UI.showToast("Goal updated successfully!", "success");
    } else {
      await API.createSavingsGoal(payload);
      UI.showToast("Goal created successfully!", "success");
    }

    UI.closeModal('savings-goal-modal');
    loadSavingsGoals();
  } catch (err) {
    UI.showToast(err.message || "Failed to save goal.", "error");
  }
}

async function handleContributionSubmit(e) {
  e.preventDefault();
  const goalId = document.getElementById('contrib-goal-id').value;
  const amount = parseFloat(document.getElementById('contrib-amount').value);

  if (isNaN(amount) || amount <= 0) {
    UI.showToast("Please enter a valid contribution amount.", "error");
    return;
  }

  try {
    await API.contributeToGoal(goalId, amount);
    UI.showToast(`Added ${Utils.formatINR(amount)} to goal!`, "success");
    UI.closeModal('contribution-modal');
    loadSavingsGoals();
  } catch (err) {
    UI.showToast(err.message || "Failed to record contribution.", "error");
  }
}

window.openAddGoalModal = () => {
  document.getElementById('goal-modal-title').textContent = 'Create Savings Goal';
  document.getElementById('savings-goal-form').reset();
  document.getElementById('goal-id').value = '';
  document.getElementById('goal-initial-saved').disabled = false;
  UI.openModal('savings-goal-modal');
};

window.openEditGoalModal = (id) => {
  const g = goalsList.find(item => item.id === id);
  if (!g) return;

  document.getElementById('goal-modal-title').textContent = 'Edit Savings Goal';
  document.getElementById('goal-id').value = g.id;
  document.getElementById('goal-name').value = g.name;
  document.getElementById('goal-desc').value = g.description || '';
  document.getElementById('goal-target').value = g.targetAmount;
  document.getElementById('goal-initial-saved').value = g.currentAmount || 0;
  document.getElementById('goal-target-date').value = g.targetDate || '';
  UI.openModal('savings-goal-modal');
};

window.openContributeModal = (id, name, remaining) => {
  document.getElementById('contrib-goal-id').value = id;
  document.getElementById('contrib-goal-name').textContent = name;
  document.getElementById('contrib-remaining-text').textContent = `Remaining to target: ${Utils.formatINR(remaining)}`;
  document.getElementById('contrib-amount').value = '';
  UI.openModal('contribution-modal');
};

window.confirmDeleteGoal = (id) => {
  UI.confirm('Delete Savings Goal', 'Are you sure you want to delete this savings goal?', async () => {
    try {
      await API.deleteSavingsGoal(id);
      UI.showToast("Goal deleted.", "success");
      loadSavingsGoals();
    } catch (e) {
      UI.showToast(e.message || "Failed to delete goal.", "error");
    }
  });
};
