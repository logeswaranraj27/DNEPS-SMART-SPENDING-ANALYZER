/**
 * SmartSpend - Module 3: DNEPS AI Assistant Client
 * Tagline: "Your Personal Smart Spending & Savings Buddy"
 */

const DnepsAI = {
  isOpen: false,
  spendingMode: localStorage.getItem('smartspend_ai_mode') || 'Balanced Mode',

  init() {
    this.bindEvents();
    this.renderSuggestions();
  },

  bindEvents() {
    const fab = document.getElementById('dneps-fab-btn');
    const panel = document.getElementById('dneps-chat-panel');
    const closeBtn = document.getElementById('dneps-close-btn');
    const form = document.getElementById('dneps-chat-form');
    const input = document.getElementById('dneps-chat-input');
    const newChatBtn = document.getElementById('dneps-new-chat-btn');

    if (fab) {
      fab.addEventListener('click', () => this.togglePanel());
    }

    if (closeBtn) {
      closeBtn.addEventListener('click', () => this.closePanel());
    }

    if (form) {
      form.addEventListener('submit', (e) => {
        e.preventDefault();
        const query = input.value.trim();
        if (query) {
          this.sendMessage(query);
          input.value = '';
        }
      });
    }

    if (newChatBtn) {
      newChatBtn.addEventListener('click', () => this.clearChat());
    }
  },

  togglePanel() {
    this.isOpen = !this.isOpen;
    const panel = document.getElementById('dneps-chat-panel');
    if (panel) {
      panel.classList.toggle('active', this.isOpen);
      if (this.isOpen) {
        document.getElementById('dneps-chat-input')?.focus();
      }
    }
  },

  closePanel() {
    this.isOpen = false;
    document.getElementById('dneps-chat-panel')?.classList.remove('active');
  },

  renderSuggestions() {
    const container = document.getElementById('dneps-suggestions');
    if (!container) return;

    const suggestions = [
      "How much did I spend this week?",
      "What was my biggest expense?",
      "Which category did I spend most on?",
      "Where could I reduce my spending?",
      "Give me my weekly spending review",
      "Which budgets have I exceeded?"
    ];

    container.innerHTML = suggestions.map(q => `
      <button type="button" class="suggestion-chip" onclick="DnepsAI.sendMessage('${Utils.escapeHtml(q)}')">${Utils.escapeHtml(q)}</button>
    `).join('');
  },

  async sendMessage(text) {
    if (!this.isOpen) this.togglePanel();

    const messagesContainer = document.getElementById('dneps-messages');
    if (!messagesContainer) return;

    // Render User Message
    const userMsgEl = document.createElement('div');
    userMsgEl.className = 'chat-message chat-message-user';
    userMsgEl.textContent = text;
    messagesContainer.appendChild(userMsgEl);

    // Render Bot Loading Skeleton
    const loadingEl = document.createElement('div');
    loadingEl.className = 'chat-message chat-message-bot';
    loadingEl.id = 'dneps-loading-bubble';
    loadingEl.innerHTML = `<span style="display:inline-block; animation: pulse 1.2s infinite;">Thinking... 💭 Analyzing your financial records</span>`;
    messagesContainer.appendChild(loadingEl);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    try {
      const mode = localStorage.getItem('smartspend_ai_mode') || 'Balanced Mode';
      const response = await API.askDnepsAI(text, { mode });
      loadingEl.remove();

      const botMsgEl = document.createElement('div');
      botMsgEl.className = 'chat-message chat-message-bot';

      let aiText = response.answer || response.message || "I'm reviewing your financial records.";
      
      // If demo mode or unconfigured message returned from Java backend
      if (response.isDemo) {
        aiText = `✨ <em>[DNEPS AI Demo Response]</em><br><br>${aiText}`;
      }

      botMsgEl.innerHTML = aiText.replace(/\n/g, '<br>');
      messagesContainer.appendChild(botMsgEl);
    } catch (err) {
      loadingEl.remove();
      const errorMsgEl = document.createElement('div');
      errorMsgEl.className = 'chat-message chat-message-bot';
      errorMsgEl.style.color = 'var(--color-danger)';
      errorMsgEl.textContent = "Sorry, I had trouble reaching the AI service. Please make sure the Java backend is running.";
      messagesContainer.appendChild(errorMsgEl);
    }

    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  },

  clearChat() {
    const messagesContainer = document.getElementById('dneps-messages');
    if (!messagesContainer) return;
    messagesContainer.innerHTML = `
      <div class="chat-message chat-message-bot">
        👋 Hi! I'm <strong>DNEPS AI</strong>, your Personal Smart Spending & Savings Buddy. 
        Ask me about your food purchases, weekly expenses, budget status, or where you can save!
      </div>
    `;
  },

  /**
   * Load dashboard Weekly Spending Review Card
   */
  async loadWeeklyReviewCard() {
    const container = document.getElementById('weekly-review-card-content');
    if (!container) return;

    try {
      const review = await API.getWeeklySpendingReview();
      
      const totalWeekly = review.totalWeeklyExpense || 0;
      const prevWeekly = review.previousWeekExpense || 0;
      const diff = totalWeekly - prevWeekly;
      const diffText = diff > 0 
        ? `<span style="color:var(--color-danger); font-weight:700;">+${Utils.formatINR(diff)}</span> vs last week`
        : `<span style="color:var(--color-success); font-weight:700;">${Utils.formatINR(Math.abs(diff))} less</span> vs last week`;

      container.innerHTML = `
        <div class="weekly-stats-strip">
          <div class="weekly-stat-item">
            <span class="weekly-stat-label">Last 7 Days Spend</span>
            <span class="weekly-stat-val">${Utils.formatINR(totalWeekly)}</span>
            <span style="font-size: 0.78rem; color: var(--slate-500); margin-top: 0.2rem;">${diffText}</span>
          </div>
          <div class="weekly-stat-item">
            <span class="weekly-stat-label">Top Category</span>
            <span class="weekly-stat-val" style="font-size: 1.15rem; color: var(--teal-700);">${Utils.escapeHtml(review.topCategory || 'None')}</span>
            <span style="font-size: 0.78rem; color: var(--slate-500);">${Utils.formatINR(review.topCategoryAmount || 0)}</span>
          </div>
          <div class="weekly-stat-item">
            <span class="weekly-stat-label">Highest Single Item</span>
            <span class="weekly-stat-val" style="font-size: 1.15rem; color: var(--primary-700);">${Utils.escapeHtml(review.highestCostItem || 'None')}</span>
            <span style="font-size: 0.78rem; color: var(--slate-500);">${Utils.formatINR(review.highestItemAmount || 0)}</span>
          </div>
        </div>

        <h4 style="font-size: 0.95rem; margin-top: 1rem; color: var(--slate-800);">💡 DNEPS AI Weekly Highlights & Plan</h4>
        <ul class="weekly-insights-list">
          ${(review.insights || [
            "Keep recording itemized purchases to uncover exact daily spending habits.",
            "Your spending is being tracked against your monthly budget limits."
          ]).map(i => `
            <li class="weekly-insight-item">
              <span class="weekly-insight-icon">✦</span>
              <span>${Utils.escapeHtml(i)}</span>
            </li>
          `).join('')}
        </ul>

        <div style="margin-top: 1.25rem; display: flex; justify-content: flex-end;">
          <button class="btn btn-sm btn-outline-primary" onclick="DnepsAI.sendMessage('Give me my weekly spending review and next-week target plan')">
            💬 Chat with DNEPS AI about this review
          </button>
        </div>
      `;
    } catch (e) {
      console.warn("Weekly review load failed:", e);
      container.innerHTML = `<p style="color:var(--slate-500); font-size:0.88rem;">Add your transactions to generate your first 7-day spending review.</p>`;
    }
  }
};

window.DnepsAI = DnepsAI;

document.addEventListener('DOMContentLoaded', () => {
  DnepsAI.init();
});
