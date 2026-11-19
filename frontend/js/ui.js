/**
 * SmartSpend - UI Component Helpers
 * Toasts, Modal Windows, Confirmations, Loading Spinners
 */

const UI = {
  /**
   * Display toast message
   * @param {string} message - Message text
   * @param {string} type - 'success' | 'error' | 'warning' | 'info'
   * @param {number} duration - Auto-dismiss milliseconds
   */
  showToast(message, type = 'info', duration = 3500) {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';
    if (type === 'warning') icon = '🔔';

    toast.innerHTML = `
      <span style="font-size: 1.1rem; line-height: 1;">${icon}</span>
      <span style="flex: 1;">${Utils.escapeHtml(message)}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(30px)';
      setTimeout(() => toast.remove(), 250);
    }, duration);
  },

  /**
   * Open a modal by ID
   */
  openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add('active');
      document.body.style.overflow = 'hidden';
    }
  },

  /**
   * Close a modal by ID
   */
  closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.remove('active');
      document.body.style.overflow = '';
    }
  },

  /**
   * Show confirmation dialog with a callback
   */
  confirm(title, message, onConfirm) {
    let confirmModal = document.getElementById('global-confirm-modal');
    if (!confirmModal) {
      confirmModal = document.createElement('div');
      confirmModal.id = 'global-confirm-modal';
      confirmModal.className = 'modal-backdrop';
      confirmModal.innerHTML = `
        <div class="modal-dialog" style="max-width: 440px;">
          <div class="modal-header">
            <h3 class="modal-title" id="confirm-title">Confirm Action</h3>
            <button class="modal-close" onclick="UI.closeModal('global-confirm-modal')">&times;</button>
          </div>
          <div class="modal-body">
            <p id="confirm-message" style="color: var(--slate-600); font-size: 0.95rem;"></p>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" onclick="UI.closeModal('global-confirm-modal')">Cancel</button>
            <button class="btn btn-danger" id="confirm-btn-action">Confirm</button>
          </div>
        </div>
      `;
      document.body.appendChild(confirmModal);
    }

    document.getElementById('confirm-title').textContent = title;
    document.getElementById('confirm-message').textContent = message;
    
    const actionBtn = document.getElementById('confirm-btn-action');
    actionBtn.onclick = () => {
      UI.closeModal('global-confirm-modal');
      if (typeof onConfirm === 'function') onConfirm();
    };

    UI.openModal('global-confirm-modal');
  }
};

window.UI = UI;
