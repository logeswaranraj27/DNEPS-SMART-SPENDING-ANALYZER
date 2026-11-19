/**
 * SmartSpend - Authentication Manager
 * Integrates Firebase Client Authentication with automatic local session fallback
 */

const Auth = {
  // Current user cache
  currentUser: null,

  /**
   * Initialize Auth state observer
   */
  init(callback) {
    const firebaseAuth = window.SmartSpendFirebase.auth();

    if (firebaseAuth) {
      firebaseAuth.onAuthStateChanged(async (user) => {
        if (user) {
          this.currentUser = {
            uid: user.uid,
            email: user.email,
            displayName: user.displayName || user.email.split('@')[0],
            isFirebase: true
          };
          sessionStorage.setItem('smartspend_user', JSON.stringify(this.currentUser));
        } else {
          this.currentUser = null;
          sessionStorage.removeItem('smartspend_user');
        }
        if (typeof callback === 'function') callback(this.currentUser);
      });
    } else {
      // Local demo mode check from sessionStorage
      const saved = sessionStorage.getItem('smartspend_user');
      if (saved) {
        try {
          this.currentUser = JSON.parse(saved);
        } catch (e) {
          this.currentUser = null;
        }
      }
      if (typeof callback === 'function') callback(this.currentUser);
    }
  },

  /**
   * Register with Email & Password
   */
  async register(email, password, displayName) {
    const firebaseAuth = window.SmartSpendFirebase.auth();

    if (firebaseAuth) {
      const cred = await firebaseAuth.createUserWithEmailAndPassword(email, password);
      if (displayName && cred.user) {
        await cred.user.updateProfile({ displayName });
      }
      this.currentUser = {
        uid: cred.user.uid,
        email: cred.user.email,
        displayName: displayName || cred.user.email.split('@')[0],
        isFirebase: true
      };
      sessionStorage.setItem('smartspend_user', JSON.stringify(this.currentUser));
      return this.currentUser;
    } else {
      // Local mode registration
      const mockUid = 'user_' + Math.random().toString(36).substring(2, 10);
      this.currentUser = {
        uid: mockUid,
        email: email,
        displayName: displayName || email.split('@')[0],
        isFirebase: false
      };
      sessionStorage.setItem('smartspend_user', JSON.stringify(this.currentUser));
      return this.currentUser;
    }
  },

  /**
   * Sign In with Email & Password
   */
  async login(email, password) {
    const firebaseAuth = window.SmartSpendFirebase.auth();

    if (firebaseAuth) {
      try {
        const cred = await firebaseAuth.signInWithEmailAndPassword(email, password);
        this.currentUser = {
          uid: cred.user.uid,
          email: cred.user.email,
          displayName: cred.user.displayName || cred.user.email.split('@')[0],
          isFirebase: true
        };
        sessionStorage.setItem('smartspend_user', JSON.stringify(this.currentUser));
        return this.currentUser;
      } catch (err) {
        // If demo credentials and user does not exist in Firebase yet, auto-create it
        if (email === 'student@smartspend.edu' && (err.code === 'auth/user-not-found' || err.code === 'auth/invalid-credential')) {
          try {
            const cred = await firebaseAuth.createUserWithEmailAndPassword(email, password);
            if (cred.user) {
              await cred.user.updateProfile({ displayName: 'Demo Student' });
            }
            this.currentUser = {
              uid: cred.user.uid,
              email: cred.user.email,
              displayName: 'Demo Student',
              isFirebase: true
            };
            sessionStorage.setItem('smartspend_user', JSON.stringify(this.currentUser));
            return this.currentUser;
          } catch (createErr) {
            throw err;
          }
        }
        throw err;
      }
    } else {
      // Local mode login
      const mockUid = 'demo_student_01';
      this.currentUser = {
        uid: mockUid,
        email: email,
        displayName: email.split('@')[0] || 'Demo Student',
        isFirebase: false
      };
      sessionStorage.setItem('smartspend_user', JSON.stringify(this.currentUser));
      return this.currentUser;
    }
  },

  /**
   * Logout
   */
  async logout() {
    const firebaseAuth = window.SmartSpendFirebase.auth();
    if (firebaseAuth) {
      try {
        await firebaseAuth.signOut();
      } catch (e) {
        console.error("Firebase signout error:", e);
      }
    }
    this.currentUser = null;
    sessionStorage.removeItem('smartspend_user');
    window.location.href = 'login.html';
  },

  /**
   * Get Firebase ID Token for Authorization Bearer Header
   */
  async getIdToken() {
    const firebaseAuth = window.SmartSpendFirebase.auth();
    if (firebaseAuth && firebaseAuth.currentUser) {
      return await firebaseAuth.currentUser.getIdToken();
    }
    // Return demo bearer token for local mode
    if (this.currentUser) {
      return `demo_token_${this.currentUser.uid}`;
    }
    return null;
  },

  /**
   * Route Guard: verify user is logged in, or redirect to login.html
   */
  requireAuth() {
    const saved = sessionStorage.getItem('smartspend_user');
    if (!saved && !this.currentUser) {
      window.location.href = 'login.html';
      return false;
    }
    if (!this.currentUser && saved) {
      this.currentUser = JSON.parse(saved);
    }
    return true;
  }
};

window.Auth = Auth;
