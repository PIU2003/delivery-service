/**
 * Gateway API client. All browser traffic goes to localhost:8080.
 * JWT is stored in localStorage; API keys are injected by the gateway.
 */
const API_BASE = "http://localhost:8080";
const TOKEN_KEY = "parcel_jwt";
const USER_KEY = "parcel_user";

const AuthStore = {
  getToken() {
    return localStorage.getItem(TOKEN_KEY);
  },
  getUsername() {
    return localStorage.getItem(USER_KEY);
  },
  setSession(accessToken, username) {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(USER_KEY, username || "");
  },
  clear() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
  isLoggedIn() {
    return Boolean(this.getToken());
  },
};

async function apiRequest(path, options = {}) {
  const headers = {
    Accept: "application/json",
    ...(options.body ? { "Content-Type": "application/json" } : {}),
    ...(options.headers || {}),
  };

  if (options.auth !== false) {
    const token = AuthStore.getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text };
    }
  }

  if (!response.ok) {
    const message =
      (data && (data.message || data.error || data.title)) ||
      `Request failed (${response.status})`;
    const err = new Error(message);
    err.status = response.status;
    err.data = data;
    throw err;
  }

  return data;
}

const Api = {
  register(username, password) {
    return apiRequest("/auth/register", {
      method: "POST",
      auth: false,
      body: JSON.stringify({ username, password }),
    });
  },

  login(username, password) {
    return apiRequest("/auth/login", {
      method: "POST",
      auth: false,
      body: JSON.stringify({ username, password }),
    });
  },

  // Parcels
  listParcels() {
    return apiRequest("/api/parcels");
  },
  getParcel(id) {
    return apiRequest(`/api/parcels/${id}`);
  },
  createParcel(body) {
    return apiRequest("/api/parcels", { method: "POST", body: JSON.stringify(body) });
  },
  updateParcel(id, body) {
    return apiRequest(`/api/parcels/${id}`, { method: "PUT", body: JSON.stringify(body) });
  },
  getParcelStatus(id) {
    return apiRequest(`/api/parcels/${id}/status`);
  },
  deleteParcel(id) {
    return apiRequest(`/api/parcels/${id}`, { method: "DELETE" });
  },

  // Couriers
  listCouriers() {
    return apiRequest("/api/couriers");
  },
  listAvailableCouriers(area) {
    const q = area ? `?area=${encodeURIComponent(area)}` : "";
    return apiRequest(`/api/couriers/available${q}`);
  },
  createCourier(body) {
    return apiRequest("/api/couriers", { method: "POST", body: JSON.stringify(body) });
  },
  updateCourier(id, body) {
    return apiRequest(`/api/couriers/${id}`, { method: "PUT", body: JSON.stringify(body) });
  },
  setAvailability(id, isAvailable) {
    return apiRequest(`/api/couriers/${id}/availability`, {
      method: "PUT",
      body: JSON.stringify({ isAvailable }),
    });
  },
  deleteCourier(id) {
    return apiRequest(`/api/couriers/${id}`, { method: "DELETE" });
  },

  // Deliveries
  listDeliveries() {
    return apiRequest("/api/deliveries");
  },
  trackDelivery(parcelId) {
    return apiRequest(`/api/deliveries/track/${parcelId}`);
  },
  assignDelivery(parcelId, area) {
    return apiRequest("/api/deliveries/assign", {
      method: "POST",
      body: JSON.stringify({ parcelId, area }),
    });
  },
  pickupDelivery(id) {
    return apiRequest(`/api/deliveries/${id}/pickup`, { method: "PUT" });
  },
  completeDelivery(id) {
    return apiRequest(`/api/deliveries/${id}/complete`, { method: "PUT" });
  },
};
