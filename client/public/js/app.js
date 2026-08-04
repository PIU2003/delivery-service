(() => {
  const $ = (sel) => document.querySelector(sel);
  const $$ = (sel) => [...document.querySelectorAll(sel)];

  let authMode = "login";
  let toastTimer = null;

  function showToast(message, type = "") {
    const el = $("#toast");
    el.textContent = message;
    el.className = `toast show${type ? ` ${type}` : ""}`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
      el.classList.remove("show");
    }, 3500);
  }

  function badge(status) {
    return `<span class="badge ${status || ""}">${status || "—"}</span>`;
  }

  function showAuth() {
    $("#auth-view").classList.remove("hidden");
    $("#app-view").classList.add("hidden");
  }

  function showApp() {
    $("#auth-view").classList.add("hidden");
    $("#app-view").classList.remove("hidden");
    $("#user-label").textContent = AuthStore.getUsername() || "user";
    loadParcels();
  }

  function switchPanel(name) {
    $$(".nav-tabs button").forEach((btn) => {
      btn.classList.toggle("active", btn.dataset.panel === name);
    });
    ["parcels", "couriers", "deliveries", "track"].forEach((id) => {
      $(`#panel-${id}`).classList.toggle("hidden", id !== name);
    });
    if (name === "parcels") loadParcels();
    if (name === "couriers") loadCouriers();
    if (name === "deliveries") loadDeliveries();
  }

  function setAuthMode(mode) {
    authMode = mode;
    $("#tab-login").classList.toggle("active", mode === "login");
    $("#tab-register").classList.toggle("active", mode === "register");
    $("#auth-submit").textContent = mode === "login" ? "Login" : "Register";
    $("#auth-password").autocomplete = mode === "login" ? "current-password" : "new-password";
  }

  async function handleAuth(event) {
    event.preventDefault();
    const username = $("#auth-username").value.trim();
    const password = $("#auth-password").value;
    const btn = $("#auth-submit");
    btn.disabled = true;
    try {
      const res =
        authMode === "login"
          ? await Api.login(username, password)
          : await Api.register(username, password);
      AuthStore.setSession(res.accessToken, res.username || username);
      showToast(`Welcome, ${res.username || username}`, "success");
      showApp();
    } catch (err) {
      showToast(err.message || "Auth failed", "error");
    } finally {
      btn.disabled = false;
    }
  }

  function logout() {
    AuthStore.clear();
    showAuth();
    showToast("Logged out");
  }

  // ——— Parcels ———
  async function loadParcels() {
    try {
      const parcels = await Api.listParcels();
      const tbody = $("#parcels-tbody");
      const empty = $("#parcels-empty");
      tbody.innerHTML = "";
      if (!parcels || parcels.length === 0) {
        empty.classList.remove("hidden");
        return;
      }
      empty.classList.add("hidden");
      parcels.forEach((p) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${p.id}</td>
          <td>${escapeHtml(p.senderName)} → ${escapeHtml(p.receiverName)}</td>
          <td>${p.weight}</td>
          <td>${badge(p.status)}</td>
          <td class="actions">
            <button type="button" class="ghost sm" data-status="${p.id}">Status</button>
            <button type="button" class="danger sm" data-del-parcel="${p.id}">Delete</button>
          </td>`;
        tbody.appendChild(tr);
      });
    } catch (err) {
      handleApiError(err);
    }
  }

  async function createParcel(event) {
    event.preventDefault();
    const body = {
      senderName: $("#p-sender").value.trim(),
      senderAddress: $("#p-sender-addr").value.trim(),
      receiverName: $("#p-receiver").value.trim(),
      receiverAddress: $("#p-receiver-addr").value.trim(),
      weight: Number($("#p-weight").value),
    };
    try {
      const created = await Api.createParcel(body);
      showToast(`Parcel #${created.id} created`, "success");
      event.target.reset();
      await loadParcels();
    } catch (err) {
      handleApiError(err);
    }
  }

  // ——— Couriers ———
  async function loadCouriers() {
    try {
      const couriers = await Api.listCouriers();
      const tbody = $("#couriers-tbody");
      const empty = $("#couriers-empty");
      tbody.innerHTML = "";
      if (!couriers || couriers.length === 0) {
        empty.classList.remove("hidden");
        return;
      }
      empty.classList.add("hidden");
      couriers.forEach((c) => {
        const avail = Boolean(c.isAvailable);
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${c.id}</td>
          <td>${escapeHtml(c.name)}</td>
          <td>${escapeHtml(c.currentArea)}</td>
          <td>${escapeHtml(c.vehicleType)}</td>
          <td><span class="badge avail-${avail}">${avail ? "Yes" : "No"}</span></td>
          <td class="actions">
            <button type="button" class="secondary sm" data-avail="${c.id}" data-next="${!avail}">
              Set ${avail ? "unavailable" : "available"}
            </button>
            <button type="button" class="danger sm" data-del-courier="${c.id}">Delete</button>
          </td>`;
        tbody.appendChild(tr);
      });
    } catch (err) {
      handleApiError(err);
    }
  }

  async function createCourier(event) {
    event.preventDefault();
    const body = {
      name: $("#c-name").value.trim(),
      phone: $("#c-phone").value.trim(),
      vehicleType: $("#c-vehicle").value.trim(),
      currentArea: $("#c-area").value.trim(),
    };
    try {
      const created = await Api.createCourier(body);
      showToast(`Courier #${created.id} created`, "success");
      event.target.reset();
      await loadCouriers();
    } catch (err) {
      handleApiError(err);
    }
  }

  // ——— Deliveries ———
  async function loadDeliveries() {
    try {
      const deliveries = await Api.listDeliveries();
      const tbody = $("#deliveries-tbody");
      const empty = $("#deliveries-empty");
      tbody.innerHTML = "";
      if (!deliveries || deliveries.length === 0) {
        empty.classList.remove("hidden");
        return;
      }
      empty.classList.add("hidden");
      deliveries.forEach((d) => {
        const tr = document.createElement("tr");
        let actions = "";
        if (d.status === "ASSIGNED") {
          actions = `<button type="button" class="sm" data-pickup="${d.id}">Pickup</button>`;
        } else if (d.status === "PICKED_UP") {
          actions = `<button type="button" class="sm" data-complete="${d.id}">Complete</button>`;
        }
        tr.innerHTML = `
          <td>${d.id}</td>
          <td>${d.parcelId}</td>
          <td>${d.courierId}</td>
          <td>${escapeHtml(d.area)}</td>
          <td>${badge(d.status)}</td>
          <td class="actions">${actions}</td>`;
        tbody.appendChild(tr);
      });
    } catch (err) {
      handleApiError(err);
    }
  }

  async function assignDelivery(event) {
    event.preventDefault();
    const parcelId = Number($("#d-parcel").value);
    const area = $("#d-area").value.trim();
    try {
      const created = await Api.assignDelivery(parcelId, area);
      showToast(`Delivery #${created.id} assigned to courier #${created.courierId}`, "success");
      event.target.reset();
      await loadDeliveries();
    } catch (err) {
      handleApiError(err);
    }
  }

  // ——— Track ———
  async function trackParcel(event) {
    event.preventDefault();
    const parcelId = Number($("#t-parcel").value);
    const box = $("#track-result");
    try {
      const [delivery, parcelStatus] = await Promise.all([
        Api.trackDelivery(parcelId).catch((e) => ({ error: e.message })),
        Api.getParcelStatus(parcelId).catch((e) => ({ error: e.message })),
      ]);

      let html = `<h3>Parcel #${parcelId}</h3><dl>`;
      if (parcelStatus.error) {
        html += `<dt>Parcel status</dt><dd>${escapeHtml(parcelStatus.error)}</dd>`;
      } else {
        html += `<dt>Parcel status</dt><dd>${badge(parcelStatus.status)}</dd>`;
      }
      if (delivery.error) {
        html += `<dt>Delivery</dt><dd>${escapeHtml(delivery.error)}</dd>`;
      } else {
        html += `
          <dt>Delivery ID</dt><dd>${delivery.id}</dd>
          <dt>Courier ID</dt><dd>${delivery.courierId}</dd>
          <dt>Area</dt><dd>${escapeHtml(delivery.area)}</dd>
          <dt>Delivery status</dt><dd>${badge(delivery.status)}</dd>
          <dt>Assigned</dt><dd>${fmtTime(delivery.assignedAt)}</dd>
          <dt>Picked up</dt><dd>${fmtTime(delivery.pickedUpAt)}</dd>
          <dt>Delivered</dt><dd>${fmtTime(delivery.deliveredAt)}</dd>`;
      }
      html += "</dl>";
      box.innerHTML = html;
      box.classList.remove("hidden");
    } catch (err) {
      handleApiError(err);
    }
  }

  function fmtTime(iso) {
    if (!iso) return "—";
    try {
      return new Date(iso).toLocaleString();
    } catch {
      return iso;
    }
  }

  function escapeHtml(value) {
    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function handleApiError(err) {
    if (err.status === 401) {
      AuthStore.clear();
      showAuth();
      showToast("Session expired — please log in again", "error");
      return;
    }
    showToast(err.message || "Request failed", "error");
  }

  // ——— Event wiring ———
  function bindEvents() {
    $("#tab-login").addEventListener("click", () => setAuthMode("login"));
    $("#tab-register").addEventListener("click", () => setAuthMode("register"));
    $("#auth-form").addEventListener("submit", handleAuth);
    $("#logout-btn").addEventListener("click", logout);

    $$(".nav-tabs button").forEach((btn) => {
      btn.addEventListener("click", () => switchPanel(btn.dataset.panel));
    });

    $("#parcel-form").addEventListener("submit", createParcel);
    $("#refresh-parcels").addEventListener("click", loadParcels);
    $("#parcels-tbody").addEventListener("click", async (e) => {
      const statusId = e.target.dataset.status;
      const delId = e.target.dataset.delParcel;
      if (statusId) {
        try {
          const s = await Api.getParcelStatus(statusId);
          showToast(`Parcel #${s.id}: ${s.status}`);
        } catch (err) {
          handleApiError(err);
        }
      }
      if (delId) {
        if (!confirm(`Delete parcel #${delId}?`)) return;
        try {
          await Api.deleteParcel(delId);
          showToast(`Parcel #${delId} deleted`, "success");
          await loadParcels();
        } catch (err) {
          handleApiError(err);
        }
      }
    });

    $("#courier-form").addEventListener("submit", createCourier);
    $("#refresh-couriers").addEventListener("click", loadCouriers);
    $("#couriers-tbody").addEventListener("click", async (e) => {
      const availId = e.target.dataset.avail;
      const delId = e.target.dataset.delCourier;
      if (availId) {
        const next = e.target.dataset.next === "true";
        try {
          await Api.setAvailability(availId, next);
          showToast(`Courier #${availId} availability → ${next}`, "success");
          await loadCouriers();
        } catch (err) {
          handleApiError(err);
        }
      }
      if (delId) {
        if (!confirm(`Delete courier #${delId}?`)) return;
        try {
          await Api.deleteCourier(delId);
          showToast(`Courier #${delId} deleted`, "success");
          await loadCouriers();
        } catch (err) {
          handleApiError(err);
        }
      }
    });

    $("#assign-form").addEventListener("submit", assignDelivery);
    $("#refresh-deliveries").addEventListener("click", loadDeliveries);
    $("#deliveries-tbody").addEventListener("click", async (e) => {
      const pickupId = e.target.dataset.pickup;
      const completeId = e.target.dataset.complete;
      if (pickupId) {
        try {
          await Api.pickupDelivery(pickupId);
          showToast(`Delivery #${pickupId} picked up`, "success");
          await loadDeliveries();
        } catch (err) {
          handleApiError(err);
        }
      }
      if (completeId) {
        try {
          await Api.completeDelivery(completeId);
          showToast(`Delivery #${completeId} completed`, "success");
          await loadDeliveries();
        } catch (err) {
          handleApiError(err);
        }
      }
    });

    $("#track-form").addEventListener("submit", trackParcel);
  }

  bindEvents();
  if (AuthStore.isLoggedIn()) {
    showApp();
  } else {
    showAuth();
  }
})();
