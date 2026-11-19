/**
 * SmartSpend - Utility Functions
 * Formatting currency (₹ INR), dates, validation, and math helpers
 */

const Utils = {
  /**
   * Format numbers into Indian Rupee currency string
   * e.g., 350 -> ₹350.00 or ₹350
   */
  formatINR(amount, decimals = 2) {
    if (amount === null || amount === undefined || isNaN(amount)) {
      amount = 0;
    }
    const num = Number(amount);
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    }).format(num);
  },

  /**
   * Format date strings (YYYY-MM-DD) into readable formats
   */
  formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    try {
      const d = new Date(dateStr + (dateStr.length === 10 ? 'T00:00:00' : ''));
      return d.toLocaleDateString('en-IN', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      });
    } catch (e) {
      return dateStr;
    }
  },

  /**
   * Get current month in YYYY-MM format
   */
  getCurrentMonth() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
  },

  /**
   * Get today's date in YYYY-MM-DD format
   */
  getTodayDate() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  },

  /**
   * Calculate difference in days between two dates
   */
  daysBetween(d1, d2) {
    const date1 = new Date(d1);
    const date2 = new Date(d2);
    const diffTime = date2.getTime() - date1.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  },

  /**
   * Safe numeric parser
   */
  parseAmount(val) {
    const num = parseFloat(val);
    return isNaN(num) ? 0 : Math.round(num * 100) / 100;
  },

  /**
   * Escape HTML to prevent XSS in user rendered content
   */
  escapeHtml(str) {
    if (!str) return '';
    return str
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }
};

window.Utils = Utils;
