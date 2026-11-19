/**
 * SmartSpend - App Global Scripts
 * Header navigation, user profile rendering, mobile toggle
 */

document.addEventListener('DOMContentLoaded', () => {
  // Update active navigation link
  const currentPath = window.location.pathname;
  document.querySelectorAll('.nav-link').forEach(link => {
    const href = link.getAttribute('href');
    if (href && currentPath.includes(href) && href !== 'index.html') {
      link.classList.add('active');
    }
  });

  // Mobile menu toggle
  const toggleBtn = document.querySelector('.mobile-menu-toggle');
  const navMenu = document.querySelector('.header-nav');
  if (toggleBtn && navMenu) {
    toggleBtn.addEventListener('click', () => {
      navMenu.classList.toggle('active');
    });
  }

  // Auth state rendering in header
  if (window.Auth) {
    window.Auth.init((user) => {
      const userDisplay = document.getElementById('header-user-display');
      const authActions = document.getElementById('header-auth-actions');

      if (user) {
        if (userDisplay) {
          userDisplay.style.display = 'flex';
          const nameEl = userDisplay.querySelector('.user-name');
          const avatarEl = userDisplay.querySelector('.user-avatar');
          if (nameEl) nameEl.textContent = user.displayName || 'Student';
          if (avatarEl) avatarEl.textContent = (user.displayName || 'S')[0].toUpperCase();
        }
        if (authActions) authActions.style.display = 'none';
      } else {
        if (userDisplay) userDisplay.style.display = 'none';
        if (authActions) authActions.style.display = 'flex';
      }
    });
  }
});

// Global logout trigger
window.handleLogout = () => {
  UI.confirm('Sign Out', 'Are you sure you want to sign out of SmartSpend?', () => {
    window.Auth.logout();
  });
};
