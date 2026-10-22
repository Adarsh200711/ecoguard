/**
 * EcoGuard API Client
 * Facilitates REST API communication with Spring Boot backend
 */

const API_BASE_URL = ''; // Relative URL since frontend is served directly by Spring Boot

const EcoGuardAPI = {
  // Session Management
  getUser() {
    try {
      const user = localStorage.getItem('ecoguard_user');
      return user ? JSON.parse(user) : null;
    } catch (e) {
      return null;
    }
  },

  setUser(userData) {
    localStorage.setItem('ecoguard_user', JSON.stringify(userData));
  },

  clearUser() {
    localStorage.removeItem('ecoguard_user');
  },

  // Auth Endpoints
  async login(email, password) {
    try {
      const res = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      return await res.json();
    } catch (err) {
      console.error('Login error', err);
      return { success: false, message: 'Backend service unavailable. Please ensure Spring Boot is running.' };
    }
  },

  async register(name, email, password, role = 'CITIZEN') {
    try {
      const res = await fetch(`${API_BASE_URL}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password, role })
      });
      return await res.json();
    } catch (err) {
      console.error('Register error', err);
      return { success: false, message: 'Backend service unavailable. Please ensure Spring Boot is running.' };
    }
  },

  // Reports Endpoints
  async getReports(params = {}) {
    try {
      const query = new URLSearchParams();
      if (params.status && params.status !== 'ALL') query.append('status', params.status);
      if (params.category && params.category !== 'ALL') query.append('category', params.category);
      if (params.zone && params.zone !== 'ALL') query.append('zone', params.zone);
      if (params.search && params.search.trim() !== '') query.append('search', params.search.trim());

      const url = `${API_BASE_URL}/api/reports${query.toString() ? '?' + query.toString() : ''}`;
      const res = await fetch(url);
      return await res.json();
    } catch (err) {
      console.error('Fetch reports error', err);
      return { success: false, message: 'Backend service or MySQL database unavailable.', data: [] };
    }
  },

  async getReportById(id) {
    try {
      const res = await fetch(`${API_BASE_URL}/api/reports/${id}`);
      return await res.json();
    } catch (err) {
      return { success: false, message: 'Failed to connect to backend server.' };
    }
  },

  async getReportHistory(id) {
    try {
      const res = await fetch(`${API_BASE_URL}/api/reports/${id}/history`);
      return await res.json();
    } catch (err) {
      return { success: false, message: 'Failed to retrieve report history.' };
    }
  },

  // Supports both FormData (with real photo file) and JSON payload
  async submitReport(data) {
    try {
      let options = { method: 'POST' };

      if (data instanceof FormData) {
        options.body = data;
        // Browser automatically sets Content-Type to multipart/form-data with boundary
      } else {
        options.headers = { 'Content-Type': 'application/json' };
        options.body = JSON.stringify(data);
      }

      const res = await fetch(`${API_BASE_URL}/api/reports`, options);
      return await res.json();
    } catch (err) {
      console.error('Submit report error', err);
      return { success: false, message: 'Cannot connect to EcoGuard backend server. Please check your connection.' };
    }
  },

  async updateReportStatus(id, status, updatedBy, comment, userRole) {
    try {
      const res = await fetch(`${API_BASE_URL}/api/reports/${id}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status, updatedBy, comment, userRole })
      });
      return await res.json();
    } catch (err) {
      return { success: false, message: 'Error updating report status in backend.' };
    }
  },

  async getCitizenNotifications(email) {
    try {
      const url = email 
        ? `${API_BASE_URL}/api/reports/notifications?email=${encodeURIComponent(email)}`
        : `${API_BASE_URL}/api/reports/notifications`;
      const res = await fetch(url);
      return await res.json();
    } catch (err) {
      return { success: false, data: [] };
    }
  },

  async deleteReport(id) {
    try {
      const res = await fetch(`${API_BASE_URL}/api/reports/${id}`, {
        method: 'DELETE'
      });
      return await res.json();
    } catch (err) {
      return { success: false, message: 'Error deleting report.' };
    }
  },

  // Dashboard Endpoints
  async getDashboardSummary() {
    try {
      const res = await fetch(`${API_BASE_URL}/api/dashboard/summary`);
      return await res.json();
    } catch (err) {
      console.error('Dashboard summary error', err);
      return { success: false, message: 'Backend or database is currently unavailable.' };
    }
  },

  // Categories Endpoints
  async getCategories() {
    try {
      const res = await fetch(`${API_BASE_URL}/api/categories`);
      return await res.json();
    } catch (err) {
      return { success: false, message: 'Failed to retrieve categories.' };
    }
  }
};

// Global Toast Notification Helper
function showToast(message, type = 'success') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <i class="fa-solid ${type === 'success' ? 'fa-circle-check' : 'fa-triangle-exclamation'}"></i>
    <span>${message}</span>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4500);
}

// Global Nav & Session Setup Helper
function setupGlobalNav() {
  const user = EcoGuardAPI.getUser();
  const navActions = document.getElementById('nav-user-actions');
  if (!navActions) return;

  if (user && user.name) {
    navActions.innerHTML = `
      <div class="user-badge">
        <i class="fa-solid fa-user-shield"></i>
        <span>${user.name.split(' ')[0]}</span>
        <span class="role-tag">${user.role}</span>
      </div>
      <button onclick="handleLogout()" class="btn btn-secondary btn-sm" title="Log Out">
        <i class="fa-solid fa-arrow-right-from-bracket"></i>
      </button>
    `;
  } else {
    navActions.innerHTML = `
      <a href="login.html" class="btn btn-secondary btn-sm">
        <i class="fa-solid fa-arrow-right-to-bracket"></i> Login
      </a>
      <a href="report.html" class="btn btn-primary btn-sm">
        <i class="fa-solid fa-bullhorn"></i> Report Issue
      </a>
    `;
  }
}

function handleLogout() {
  EcoGuardAPI.clearUser();
  showToast('Logged out successfully');
  setTimeout(() => {
    window.location.reload();
  }, 600);
}

document.addEventListener('DOMContentLoaded', setupGlobalNav);
