/**
 * SmartSpend - Module 1: Transaction & Itemized Expense Management
 */

let transactionsList = [];
let editingTransactionId = null;

document.addEventListener('DOMContentLoaded', () => {
  if (window.Auth && !window.Auth.requireAuth()) return;

  initTransactionEvents();
  loadTransactions();
});

function initTransactionEvents() {
  const form = document.getElementById('transaction-form');
  if (form) {
    form.addEventListener('submit', handleTransactionSubmit);
  }

  // Type change (income vs expense)
  const typeSelect = document.getElementById('tx-type');
  if (typeSelect) {
    typeSelect.addEventListener('change', (e) => {
      const isExpense = e.target.value === 'expense';
      const itemizedSection = document.getElementById('itemized-section');
      const categorySelect = document.getElementById('tx-category');

      if (itemizedSection) {
        itemizedSection.style.display = isExpense ? 'block' : 'none';
      }

      // Update category choices based on type
      populateCategoryOptions(isExpense);
    });
  }

  // Set default date to today
  const dateInput = document.getElementById('tx-date');
  if (dateInput && !dateInput.value) {
    dateInput.value = Utils.getTodayDate();
  }

  // Add Item button
  const addItemBtn = document.getElementById('btn-add-item');
  if (addItemBtn) {
    addItemBtn.addEventListener('click', () => addItemRow());
  }

  // Filter & Search events
  const searchInput = document.getElementById('filter-search');
  if (searchInput) {
    searchInput.addEventListener('input', applyFilters);
  }

  const typeFilter = document.getElementById('filter-type');
  if (typeFilter) {
    typeFilter.addEventListener('change', applyFilters);
  }

  const categoryFilter = document.getElementById('filter-category');
  if (categoryFilter) {
    categoryFilter.addEventListener('change', applyFilters);
  }

  const sortSelect = document.getElementById('filter-sort');
  if (sortSelect) {
    sortSelect.addEventListener('change', applyFilters);
  }
}

function populateCategoryOptions(isExpense) {
  const categorySelect = document.getElementById('tx-category');
  if (!categorySelect) return;

  const expenseCategories = [
    'Food', 'Transportation', 'Education', 'Shopping',
    'Entertainment', 'Accommodation', 'Bills and subscriptions',
    'Health', 'Personal care', 'Other expenses'
  ];

  const incomeCategories = [
    'Allowance', 'Scholarship', 'Part-time job',
    'Freelance income', 'Gift', 'Other income'
  ];

  const categories = isExpense ? expenseCategories : incomeCategories;
  categorySelect.innerHTML = categories.map(c => `<option value="${c}">${c}</option>`).join('');
}

/**
 * Add an item row in the itemized expense section
 */
function addItemRow(name = '', amount = '') {
  const container = document.getElementById('itemized-rows-container');
  if (!container) return;

  const rowId = 'item-row-' + Math.random().toString(36).substring(2, 9);
  const row = document.createElement('div');
  row.className = 'item-row';
  row.id = rowId;
  row.innerHTML = `
    <input type="text" class="form-control item-name" placeholder="Item name (e.g., Lunch, Chips)" value="${Utils.escapeHtml(name)}" required>
    <div style="position: relative;">
      <span style="position: absolute; left: 0.6rem; top: 0.65rem; color: var(--slate-400); font-weight: 600;">₹</span>
      <input type="number" step="0.01" min="0.01" class="form-control item-amount" style="padding-left: 1.6rem;" placeholder="0.00" value="${amount}" required>
    </div>
    <button type="button" class="btn-remove-item" onclick="removeItemRow('${rowId}')" title="Remove item">&times;</button>
  `;

  container.appendChild(row);

  // Recalculate on input
  row.querySelector('.item-amount').addEventListener('input', recalculateItemizedTotal);
  recalculateItemizedTotal();
}

function removeItemRow(rowId) {
  const row = document.getElementById(rowId);
  if (row) {
    row.remove();
    recalculateItemizedTotal();
  }
}

/**
 * Recalculate itemized total and optionally sync with transaction amount
 */
function recalculateItemizedTotal() {
  const rows = document.querySelectorAll('.item-row');
  let sum = 0;
  rows.forEach(r => {
    const val = parseFloat(r.querySelector('.item-amount').value);
    if (!isNaN(val) && val > 0) sum += val;
  });

  const displayEl = document.getElementById('itemized-sum-display');
  const txAmountInput = document.getElementById('tx-amount');
  const syncBtn = document.getElementById('btn-sync-total');

  if (displayEl) {
    displayEl.textContent = Utils.formatINR(sum);
  }

  // If items exist, sync transaction amount field
  if (rows.length > 0 && txAmountInput) {
    const currentTxAmount = parseFloat(txAmountInput.value) || 0;
    const diff = Math.abs(currentTxAmount - sum);

    if (displayEl) {
      if (diff < 0.01) {
        displayEl.className = 'itemized-total-match';
      } else {
        displayEl.className = 'itemized-total-mismatch';
      }
    }
  }
}

function syncItemizedTotalToAmount() {
  const rows = document.querySelectorAll('.item-row');
  let sum = 0;
  rows.forEach(r => {
    const val = parseFloat(r.querySelector('.item-amount').value);
    if (!isNaN(val) && val > 0) sum += val;
  });

  const txAmountInput = document.getElementById('tx-amount');
  if (txAmountInput && sum > 0) {
    txAmountInput.value = sum.toFixed(2);
    recalculateItemizedTotal();
  }
}

/**
 * Submit Transaction (Create or Update)
 */
async function handleTransactionSubmit(e) {
  e.preventDefault();

  const type = document.getElementById('tx-type').value;
  const category = document.getElementById('tx-category').value;
  const amountVal = parseFloat(document.getElementById('tx-amount').value);
  const date = document.getElementById('tx-date').value;
  const description = document.getElementById('tx-desc').value;

  if (isNaN(amountVal) || amountVal <= 0) {
    UI.showToast("Please enter a valid positive transaction amount.", "error");
    return;
  }

  // Process Itemized details if expense
  let items = [];
  if (type === 'expense') {
    const itemRows = document.querySelectorAll('.item-row');
    if (itemRows.length > 0) {
      let itemSum = 0;
      for (const row of itemRows) {
        const iName = row.querySelector('.item-name').value.trim();
        const iAmount = parseFloat(row.querySelector('.item-amount').value);

        if (!iName) {
          UI.showToast("Item name cannot be empty.", "error");
          return;
        }
        if (isNaN(iAmount) || iAmount <= 0) {
          UI.showToast("Each item amount must be a positive number.", "error");
          return;
        }

        itemSum += iAmount;
        items.push({ name: iName, amount: iAmount });
      }

      // Validate itemized sum equals transaction amount
      if (Math.abs(itemSum - amountVal) > 0.01) {
        UI.showToast(`Itemized total (${Utils.formatINR(itemSum)}) must equal transaction amount (${Utils.formatINR(amountVal)}).`, "error");
        return;
      }
    }
  }

  const payload = {
    type,
    category,
    amount: amountVal,
    date,
    description,
    items
  };

  const submitBtn = document.getElementById('btn-submit-tx');
  const originalText = submitBtn.textContent;
  submitBtn.disabled = true;
  submitBtn.textContent = 'Saving...';

  try {
    if (editingTransactionId) {
      await API.updateTransaction(editingTransactionId, payload);
      UI.showToast("Transaction updated successfully!", "success");
    } else {
      await API.createTransaction(payload);
      UI.showToast("Transaction recorded successfully!", "success");
    }

    resetTransactionForm();
    UI.closeModal('transaction-modal');
    await loadTransactions();
  } catch (err) {
    UI.showToast(err.message || "Failed to save transaction.", "error");
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = originalText;
  }
}

/**
 * Load Transactions from Backend
 */
async function loadTransactions() {
  const listContainer = document.getElementById('transactions-table-body');
  if (!listContainer) return;

  listContainer.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 2rem;">Loading transactions...</td></tr>`;

  try {
    const res = await API.getTransactions();
    transactionsList = Array.isArray(res) ? res : (res.transactions || []);
    applyFilters();
    updateSummaryCards(res.summary || null);
  } catch (err) {
    console.error("Load transactions error:", err);
    listContainer.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 2rem; color: var(--color-danger);">Failed to load transactions. Check backend connection.</td></tr>`;
  }
}

/**
 * Apply search, filters, and sort to transaction table
 */
function applyFilters() {
  const searchQuery = (document.getElementById('filter-search')?.value || '').toLowerCase().trim();
  const typeFilter = document.getElementById('filter-type')?.value || 'all';
  const categoryFilter = document.getElementById('filter-category')?.value || 'all';
  const sort = document.getElementById('filter-sort')?.value || 'date-desc';

  let filtered = [...transactionsList];

  // Filter Type
  if (typeFilter !== 'all') {
    filtered = filtered.filter(tx => tx.type === typeFilter);
  }

  // Filter Category
  if (categoryFilter !== 'all') {
    filtered = filtered.filter(tx => tx.category === categoryFilter);
  }

  // Search by category, description, OR item name
  if (searchQuery) {
    filtered = filtered.filter(tx => {
      const matchCat = tx.category?.toLowerCase().includes(searchQuery);
      const matchDesc = tx.description?.toLowerCase().includes(searchQuery);
      const matchItems = tx.items && tx.items.some(i => i.name.toLowerCase().includes(searchQuery));
      return matchCat || matchDesc || matchItems;
    });
  }

  // Sort
  filtered.sort((a, b) => {
    if (sort === 'date-desc') return new Date(b.date) - new Date(a.date);
    if (sort === 'date-asc') return new Date(a.date) - new Date(b.date);
    if (sort === 'amount-desc') return b.amount - a.amount;
    if (sort === 'amount-asc') return a.amount - b.amount;
    return 0;
  });

  renderTransactionsTable(filtered);
}

/**
 * Render Transactions in HTML Table
 */
function renderTransactionsTable(transactions) {
  const tbody = document.getElementById('transactions-table-body');
  if (!tbody) return;

  if (transactions.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6">
          <div class="empty-state">
            <div class="empty-state-icon">💳</div>
            <div class="empty-state-title">No transactions found</div>
            <p class="empty-state-desc">Record your daily expenses or income to view detailed analytics and smart suggestions.</p>
            <button class="btn btn-primary" onclick="openAddTransactionModal()">+ Add Transaction</button>
          </div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = transactions.map(tx => {
    const isIncome = tx.type === 'income';
    const amountClass = isIncome ? 'color: var(--color-success); font-weight:700;' : 'color: var(--color-danger); font-weight:700;';
    const amountPrefix = isIncome ? '+' : '-';
    const hasItems = tx.items && tx.items.length > 0;

    let itemsSnippet = '';
    if (hasItems) {
      const itemsList = tx.items.map(i => `${Utils.escapeHtml(i.name)} (${Utils.formatINR(i.amount)})`).join(', ');
      itemsSnippet = `
        <div style="font-size: 0.78rem; color: var(--slate-500); margin-top: 0.25rem;">
          🛒 <strong>Items:</strong> ${itemsList}
        </div>
      `;
    }

    return `
      <tr>
        <td>${Utils.formatDate(tx.date)}</td>
        <td>
          <span class="badge ${isIncome ? 'badge-income' : 'badge-expense'}">
            ${isIncome ? 'Income' : 'Expense'}
          </span>
        </td>
        <td>
          <div style="font-weight: 600; color: var(--slate-900);">${Utils.escapeHtml(tx.category)}</div>
          ${tx.description ? `<div style="font-size: 0.8rem; color: var(--slate-500);">${Utils.escapeHtml(tx.description)}</div>` : ''}
          ${itemsSnippet}
        </td>
        <td style="${amountClass}">${amountPrefix} ${Utils.formatINR(tx.amount)}</td>
        <td>
          ${hasItems ? `<span class="badge badge-info">${tx.items.length} items</span>` : '<span style="color:var(--slate-400); font-size:0.8rem;">Single</span>'}
        </td>
        <td style="text-align: right;">
          <button class="btn btn-sm btn-secondary" onclick="openEditTransactionModal('${tx.id}')">Edit</button>
          <button class="btn btn-sm btn-secondary" style="color: var(--color-danger);" onclick="confirmDeleteTransaction('${tx.id}')">Delete</button>
        </td>
      </tr>
    `;
  }).join('');
}

function updateSummaryCards(summary) {
  if (!summary) return;
  const incEl = document.getElementById('summary-total-income');
  const expEl = document.getElementById('summary-total-expense');
  const netEl = document.getElementById('summary-net-amount');

  if (incEl) incEl.textContent = Utils.formatINR(summary.totalIncome || 0);
  if (expEl) expEl.textContent = Utils.formatINR(summary.totalExpense || 0);
  if (netEl) {
    netEl.textContent = Utils.formatINR(summary.netAmount || 0);
    netEl.style.color = (summary.netAmount >= 0) ? 'var(--teal-600)' : 'var(--color-danger)';
  }
}

/**
 * Open Modal to Add
 */
window.openAddTransactionModal = () => {
  editingTransactionId = null;
  resetTransactionForm();
  document.getElementById('modal-tx-title').textContent = 'Add Transaction';
  document.getElementById('btn-submit-tx').textContent = 'Save Transaction';
  UI.openModal('transaction-modal');
};

/**
 * Open Modal to Edit
 */
window.openEditTransactionModal = (id) => {
  const tx = transactionsList.find(t => t.id === id);
  if (!tx) return;

  editingTransactionId = id;
  document.getElementById('modal-tx-title').textContent = 'Edit Transaction';
  document.getElementById('btn-submit-tx').textContent = 'Update Transaction';

  document.getElementById('tx-type').value = tx.type;
  populateCategoryOptions(tx.type === 'expense');
  document.getElementById('tx-category').value = tx.category;
  document.getElementById('tx-amount').value = tx.amount;
  document.getElementById('tx-date').value = tx.date;
  document.getElementById('tx-desc').value = tx.description || '';

  // Itemized section
  const itemizedSection = document.getElementById('itemized-section');
  const container = document.getElementById('itemized-rows-container');
  container.innerHTML = '';

  if (tx.type === 'expense') {
    itemizedSection.style.display = 'block';
    if (tx.items && tx.items.length > 0) {
      tx.items.forEach(i => addItemRow(i.name, i.amount));
    }
  } else {
    itemizedSection.style.display = 'none';
  }

  recalculateItemizedTotal();
  UI.openModal('transaction-modal');
};

/**
 * Confirm Delete
 */
window.confirmDeleteTransaction = (id) => {
  UI.confirm('Delete Transaction', 'Are you sure you want to delete this transaction? This action cannot be undone.', async () => {
    try {
      await API.deleteTransaction(id);
      UI.showToast("Transaction deleted successfully.", "success");
      loadTransactions();
    } catch (e) {
      UI.showToast(e.message || "Failed to delete transaction.", "error");
    }
  });
};

function resetTransactionForm() {
  const form = document.getElementById('transaction-form');
  if (form) form.reset();
  const dateInput = document.getElementById('tx-date');
  if (dateInput) dateInput.value = Utils.getTodayDate();

  const container = document.getElementById('itemized-rows-container');
  if (container) container.innerHTML = '';

  const displayEl = document.getElementById('itemized-sum-display');
  if (displayEl) displayEl.textContent = '₹0.00';

  populateCategoryOptions(true);
}
