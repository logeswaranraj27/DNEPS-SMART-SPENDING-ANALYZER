/**
 * SmartSpend - Backend API Client
 * Manages HTTP communication with the Java Spring Boot REST backend,
 * injecting Firebase ID tokens for authorization.
 */

const API = {
  // Default Spring Boot backend URL
  baseUrl: window.BACKEND_API_URL || 'https://dneps-smartspend-backend.onrender.com/api',

  /**
   * Internal request handler with bearer token
   */
  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
    const headers = options.headers || {};

    if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }

    // Attach Firebase ID token if authenticated
    try {
      const token = await window.Auth.getIdToken();
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    } catch (e) {
      console.warn("Could not retrieve Firebase token:", e);
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);

      if (response.status === 401) {
        console.warn("Unauthorized API call. Session may have expired.");
        // Redirect to login if on protected page
        if (!window.location.pathname.includes('login.html') && !window.location.pathname.includes('register.html') && !window.location.pathname.endsWith('/')) {
          window.Auth.logout();
        }
        throw new Error("Authentication session expired. Please sign in again.");
      }

      const contentType = response.headers.get('content-type');
      let data = null;
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      if (!response.ok) {
        const errorMsg = (data && data.message) || (typeof data === 'string' ? data : `Error ${response.status}`);
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      console.error(`API Error [${options.method || 'GET'} ${endpoint}]:`, err);
      throw err;
    }
  },

  /* --- Transactions Endpoints --- */
  getTransactions(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request(`/transactions${query ? '?' + query : ''}`);
  },

  getTransaction(id) {
    return this.request(`/transactions/${id}`);
  },

  createTransaction(data) {
    return this.request('/transactions', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateTransaction(id, data) {
    return this.request(`/transactions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteTransaction(id) {
    return this.request(`/transactions/${id}`, {
      method: 'DELETE'
    });
  },

  /* --- Financial Summaries & Analytics Endpoints --- */
  getFinancialSummary(period = 'monthly', month = null) {
    const params = new URLSearchParams();
    if (period) params.append('period', period);
    if (month) params.append('month', month);
    return this.request(`/analytics/summary?${params.toString()}`);
  },

  getCategoryAnalytics(month = null) {
    const params = new URLSearchParams();
    if (month) params.append('month', month);
    return this.request(`/analytics/categories?${params.toString()}`);
  },

  getSpendingTrends(period = 'daily', days = 30) {
    return this.request(`/analytics/trends?period=${period}&days=${days}`);
  },

  getMonthlyComparison(year = null) {
    return this.request(`/analytics/monthly-comparison${year ? '?year=' + year : ''}`);
  },

  getAvoidableSavingsOpportunities(month = null, reductionPercent = 30) {
    return this.request(`/analytics/where-you-could-save?month=${month || ''}&reductionPercent=${reductionPercent}`);
  },

  /* --- Budgets Endpoints --- */
  getBudgets(month = null) {
    return this.request(`/budgets${month ? '?month=' + month : ''}`);
  },

  createBudget(data) {
    return this.request('/budgets', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateBudget(id, data) {
    return this.request(`/budgets/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteBudget(id) {
    return this.request(`/budgets/${id}`, {
      method: 'DELETE'
    });
  },

  /* --- Savings Goals Endpoints --- */
  getSavingsGoals() {
    return this.request('/savings-goals');
  },

  createSavingsGoal(data) {
    return this.request('/savings-goals', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateSavingsGoal(id, data) {
    return this.request(`/savings-goals/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  contributeToGoal(id, amount) {
    return this.request(`/savings-goals/${id}/contribute`, {
      method: 'POST',
      body: JSON.stringify({ amount })
    });
  },

  deleteSavingsGoal(id) {
    return this.request(`/savings-goals/${id}`, {
      method: 'DELETE'
    });
  },

  /* --- DNEPS AI Endpoints --- */
  askDnepsAI(question, context = {}) {
    return this.request('/ai/ask', {
      method: 'POST',
      body: JSON.stringify({ question, context })
    });
  },

  getWeeklySpendingReview() {
    return this.request('/ai/weekly-review');
  },

  /* --- User Profile & Preferences --- */
  getUserProfile() {
    return this.request('/user/profile');
  },

  updateUserPreferences(preferences) {
    return this.request('/user/preferences', {
      method: 'PUT',
      body: JSON.stringify(preferences)
    });
  }
};

window.API = API;
