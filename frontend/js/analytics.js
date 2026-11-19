/**
 * SmartSpend - Module 2: Spending Analytics & Chart.js Visualizations
 */

let categoryChartInstance = null;
let incomeExpenseChartInstance = null;
let trendsChartInstance = null;
let monthlyComparisonChartInstance = null;

document.addEventListener('DOMContentLoaded', () => {
  if (window.Auth && !window.Auth.requireAuth()) return;

  loadAnalytics();
});

async function loadAnalytics() {
  try {
    const [categoriesData, summaryData, trendsData, monthlyData] = await Promise.all([
      API.getCategoryAnalytics().catch(() => ({ categories: [] })),
      API.getFinancialSummary('monthly').catch(() => ({ totalIncome: 0, totalExpense: 0 })),
      API.getSpendingTrends('daily', 14).catch(() => ({ points: [] })),
      API.getMonthlyComparison().catch(() => ({ months: [] }))
    ]);

    renderCategoryChart(categoriesData.categories || []);
    renderIncomeExpenseChart(summaryData);
    renderTrendsChart(trendsData.points || []);
    renderMonthlyComparisonChart(monthlyData.months || []);
  } catch (err) {
    console.error("Error loading analytics:", err);
  }
}

/**
 * 1. Category Doughnut Chart
 */
function renderCategoryChart(categories) {
  const ctx = document.getElementById('category-doughnut-chart');
  if (!ctx) return;

  if (categoryChartInstance) categoryChartInstance.destroy();

  const labels = categories.map(c => c.category);
  const data = categories.map(c => c.amount);

  if (data.length === 0) {
    labels.push('No Expenses');
    data.push(1);
  }

  const colors = [
    '#10B981', '#0D9488', '#3B82F6', '#8B5CF6', '#EC4899',
    '#F59E0B', '#F97316', '#64748B', '#14B8A6', '#6366F1'
  ];

  categoryChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: colors.slice(0, data.length),
        borderWidth: 2,
        borderColor: '#FFFFFF'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
          labels: { boxWidth: 12, font: { family: 'Inter', size: 12 } }
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              const val = context.raw || 0;
              return ` ${context.label}: ${Utils.formatINR(val)}`;
            }
          }
        }
      },
      cutout: '68%'
    }
  });
}

/**
 * 2. Income vs Expenses Bar Chart
 */
function renderIncomeExpenseChart(summary) {
  const ctx = document.getElementById('income-expense-bar-chart');
  if (!ctx) return;

  if (incomeExpenseChartInstance) incomeExpenseChartInstance.destroy();

  const income = summary.totalIncome || 0;
  const expense = summary.totalExpense || 0;

  incomeExpenseChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Income', 'Expenses'],
      datasets: [{
        label: 'Amount (₹)',
        data: [income, expense],
        backgroundColor: ['rgba(16, 185, 129, 0.85)', 'rgba(239, 68, 68, 0.85)'],
        borderColor: ['#059669', '#DC2626'],
        borderWidth: 1,
        borderRadius: 8,
        barThickness: 52
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: value => '₹' + value
          }
        }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: context => ` Amount: ${Utils.formatINR(context.raw)}`
          }
        }
      }
    }
  });
}

/**
 * 3. Daily / Weekly Spending Trends Line Chart
 */
function renderTrendsChart(points) {
  const ctx = document.getElementById('trends-line-chart');
  if (!ctx) return;

  if (trendsChartInstance) trendsChartInstance.destroy();

  const labels = points.map(p => Utils.formatDate(p.date));
  const values = points.map(p => p.amount);

  trendsChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels.length ? labels : ['No Data'],
      datasets: [{
        label: 'Daily Spending (₹)',
        data: values.length ? values : [0],
        borderColor: '#0D9488',
        backgroundColor: 'rgba(13, 148, 136, 0.08)',
        fill: true,
        tension: 0.35,
        borderWidth: 2.5,
        pointBackgroundColor: '#0D9488',
        pointRadius: 4,
        pointHoverRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          ticks: { callback: v => '₹' + v }
        }
      },
      plugins: {
        tooltip: {
          callbacks: {
            label: context => ` Spent: ${Utils.formatINR(context.raw)}`
          }
        }
      }
    }
  });
}

/**
 * 4. Monthly Comparison Chart
 */
function renderMonthlyComparisonChart(months) {
  const ctx = document.getElementById('monthly-comparison-chart');
  if (!ctx) return;

  if (monthlyComparisonChartInstance) monthlyComparisonChartInstance.destroy();

  const labels = months.map(m => m.monthName || m.month);
  const incomeData = months.map(m => m.income || 0);
  const expenseData = months.map(m => m.expense || 0);

  monthlyComparisonChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels.length ? labels : ['Current Month'],
      datasets: [
        {
          label: 'Income',
          data: incomeData.length ? incomeData : [0],
          backgroundColor: '#10B981',
          borderRadius: 6
        },
        {
          label: 'Expenses',
          data: expenseData.length ? expenseData : [0],
          backgroundColor: '#EF4444',
          borderRadius: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          ticks: { callback: v => '₹' + v }
        }
      },
      plugins: {
        tooltip: {
          callbacks: {
            label: context => ` ${context.dataset.label}: ${Utils.formatINR(context.raw)}`
          }
        }
      }
    }
  });
}

window.reloadAnalyticsCharts = loadAnalytics;
