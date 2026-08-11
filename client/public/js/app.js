(() => {
  const $ = (sel) => document.querySelector(sel);
  const $$ = (sel) => [...document.querySelectorAll(sel)];

  const PANEL_TITLES = {
    overview: "Overview",
    parcels: "Parcels",
    couriers: "Couriers",
    dispatch: "Dispatch",
    track: "Track",
  };

  const STATUS_ORDER = { ASSIGNED: 0, PICKED_UP: 1, DELIVERED: 2 };

  let authMode = "login";
  let toastTimer = null;
  let useCustomArea = false;
  let parcelMap = new Map();
  let courierMap = new Map();
  let cacheParcels = [];
  let cacheCouriers = [];
  let cacheDeliveries = [];

  function showToast(message, type = "") {
    const el = $("#toast");
    el.textContent = message;
    el.className = `toast show${type ? ` ${type}` : ""}`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove("show"), 3500);
  }

  function badge(status) {
    return `<span class="badge ${status || ""}">${status || "—"}</span>`;
  }

  function shortId(id) {
    if (!id) return "—";
    const s = String(id);
    return s.length <= 8 ? s : `…${s.slice(-4)}`;
  }

  function idChip(id) {
    if (!id) return "—";
    return `<span class="id-chip" title="${escapeHtml(id)}">
      ${escapeHtml(shortId(id))}
      <button type="button" data-copy="${escapeHtml(id)}" aria-label="Copy full id">Copy</button>
    </span>`;
  }

  function escapeHtml(value) {
    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function fmtTime(iso) {
    if (!iso) return "—";
    try {
      return new Date(iso).toLocaleString();
    } catch {
      return iso;
    }
  }

  function parcelLabel(p) {
    return `${p.senderName} → ${p.receiverName} · ${p.weight} kg`;
  }

  function showAuth() {
    $("#auth-view").classList.remove("hidden");
    $("#app-view").classList.add("hidden");
    closeDrawers();
  }

  function showApp() {
    $("#auth-view").classList.add("hidden");
    $("#app-view").classList.remove("hidden");
    $("#user-label").textContent = AuthStore.getUsername() || "user";
    switchPanel("overview");
  }

  function switchPanel(name) {
    $$(".nav-side button").forEach((btn) => {
      btn.classList.toggle("active", btn.dataset.panel === name);
    });
    ["overview", "parcels", "couriers", "dispatch", "track"].forEach((id) => {
      const el = $(`#panel-${id}`);
      if (!el) return;
      const show = id === name;
      el.classList.toggle("hidden", !show);
      if (show) {
        el.classList.remove("panel");
        void el.offsetWidth;
        el.classList.add("panel");
      }
    });
    $("#topbar-title").textContent = PANEL_TITLES[name] || name;

    if (name === "overview") loadOverview();
    if (name === "parcels") loadParcels();
    if (name === "couriers") loadCouriers();
    if (name === "dispatch") loadDispatch();
    if (name === "track") fillTrackSelect();
  }

  function setAuthMode(mode) {
    authMode = mode;
    $("#tab-login").classList.toggle("active", mode === "login");
    $("#tab-register").classList.toggle("active", mode === "register");
    $("#auth-submit").textContent = mode === "login" ? "Login" : "Create account";
    $("#auth-heading").textContent = mode === "login" ? "Welcome back" : "Join the desk";
    $("#auth-sub").textContent =
      mode === "login" ? "Sign in to open the operations desk." : "Register a new operator account.";
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

  function handleApiError(err) {
    if (err.status === 401) {
      AuthStore.clear();
      showAuth();
      showToast("Session expired — please log in again", "error");
      return;
    }
    showToast(err.message || "Request failed", "error");
  }

  function openDrawer(id) {
    $("#drawer-backdrop").classList.add("open");
    const drawer = $(id);
    drawer.classList.add("open");
    drawer.setAttribute("aria-hidden", "false");
  }

  function closeDrawers() {
    $("#drawer-backdrop").classList.remove("open");
    $$(".drawer").forEach((d) => {
      d.classList.remove("open");
      d.setAttribute("aria-hidden", "true");
    });
  }

  function updateCaches(parcels, couriers, deliveries) {
    if (parcels) {
      cacheParcels = parcels;
      parcelMap = new Map(parcels.map((p) => [p.id, p]));
    }
    if (couriers) {
      cacheCouriers = couriers;
      courierMap = new Map(couriers.map((c) => [c.id, c]));
    }
    if (deliveries) {
      cacheDeliveries = deliveries;
    }
  }

  function fillPendingParcelSelect(selectedId) {
    const sel = $("#d-parcel");
    const pending = cacheParcels.filter((p) => p.status === "PENDING");
    const current = selectedId || sel.value;
    sel.innerHTML = `<option value="">Select pending parcel…</option>`;
    pending.forEach((p) => {
      const opt = document.createElement("option");
      opt.value = p.id;
      opt.textContent = parcelLabel(p);
      sel.appendChild(opt);
    });
    if (current && [...sel.options].some((o) => o.value === current)) {
      sel.value = current;
    }
  }

  function fillAreaSelect() {
    const sel = $("#d-area");
    const areas = [
      ...new Set(
        cacheCouriers
          .filter((c) => c.isAvailable)
          .map((c) => c.currentArea)
          .filter(Boolean)
      ),
    ].sort((a, b) => a.localeCompare(b));
    const current = sel.value;
    sel.innerHTML = `<option value="">Select area…</option>`;
    areas.forEach((area) => {
      const opt = document.createElement("option");
      opt.value = area;
      opt.textContent = area;
      sel.appendChild(opt);
    });
    if (current && areas.includes(current)) sel.value = current;
  }

  async function fillTrackSelect(selectedId) {
    const sel = $("#t-parcel");
    const current = selectedId || sel.value;
    if (!cacheParcels.length) {
      try {
        const parcels = await Api.listParcels();
        updateCaches(parcels, null, null);
      } catch (err) {
        handleApiError(err);
        return;
      }
    }
    sel.innerHTML = `<option value="">Select parcel…</option>`;
    cacheParcels.forEach((p) => {
      const opt = document.createElement("option");
      opt.value = p.id;
      opt.textContent = `${parcelLabel(p)} (${p.status})`;
      sel.appendChild(opt);
    });
    if (current && [...sel.options].some((o) => o.value === current)) {
      sel.value = current;
    }
  }

  async function loadOverview() {
    try {
      const [parcels, couriers, deliveries] = await Promise.all([
        Api.listParcels(),
        Api.listCouriers(),
        Api.listDeliveries(),
      ]);
      updateCaches(parcels, couriers, deliveries);

      const pending = parcels.filter((p) => p.status === "PENDING");
      const free = couriers.filter((c) => c.isAvailable);
      const active = deliveries.filter((d) => d.status === "ASSIGNED" || d.status === "PICKED_UP");

      $("#stat-parcels").textContent = String(parcels.length);
      $("#stat-pending").textContent = String(pending.length);
      $("#stat-couriers").textContent = String(free.length);
      $("#stat-active").textContent = String(active.length);

      const list = $("#attention-list");
      const empty = $("#attention-empty");
      list.innerHTML = "";
      const items = [];

      pending.slice(0, 5).forEach((p) => {
        items.push({
          text: `Pending: ${parcelLabel(p)}`,
          action: () => goAssignParcel(p.id),
          label: "Assign",
        });
      });
      active.slice(0, 5).forEach((d) => {
        const p = parcelMap.get(d.parcelId);
        const name = p ? parcelLabel(p) : shortId(d.parcelId);
        items.push({
          text: `${d.status.replace("_", " ")}: ${name}`,
          action: () => switchPanel("dispatch"),
          label: "Open",
        });
      });

      if (!items.length) {
        empty.classList.remove("hidden");
      } else {
        empty.classList.add("hidden");
        items.forEach((item) => {
          const li = document.createElement("li");
          li.innerHTML = `<span>${escapeHtml(item.text)}</span>`;
          const btn = document.createElement("button");
          btn.type = "button";
          btn.className = "secondary sm";
          btn.textContent = item.label;
          btn.addEventListener("click", item.action);
          li.appendChild(btn);
          list.appendChild(li);
        });
      }
    } catch (err) {
      handleApiError(err);
    }
  }

  async function loadParcels() {
    const loading = $("#parcels-loading");
    const empty = $("#parcels-empty");
    const tbody = $("#parcels-tbody");
    loading.classList.remove("hidden");
    empty.classList.add("hidden");
    try {
      const parcels = await Api.listParcels();
      updateCaches(parcels, null, null);
      tbody.innerHTML = "";
      if (!parcels.length) {
        empty.classList.remove("hidden");
        return;
      }
      parcels.forEach((p) => {
        const tr = document.createElement("tr");
        const assignBtn =
          p.status === "PENDING"
            ? `<button type="button" class="sm" data-assign-parcel="${p.id}">Assign</button>`
            : `<button type="button" class="ghost sm" data-track-parcel="${p.id}">Track</button>`;
        tr.innerHTML = `
          <td>${idChip(p.id)}</td>
          <td>
            <strong>${escapeHtml(p.senderName)} → ${escapeHtml(p.receiverName)}</strong><br />
            <span style="color:var(--muted);font-size:0.85rem">${escapeHtml(p.senderAddress)} → ${escapeHtml(p.receiverAddress)}</span>
          </td>
          <td>${p.weight} kg</td>
          <td>${badge(p.status)}</td>
          <td class="actions">
            ${assignBtn}
            <button type="button" class="danger sm" data-del-parcel="${p.id}">Delete</button>
          </td>`;
        tbody.appendChild(tr);
      });
    } catch (err) {
      handleApiError(err);
    } finally {
      loading.classList.add("hidden");
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
      await Api.createParcel(body);
      showToast("Parcel created", "success");
      event.target.reset();
      closeDrawers();
      await loadParcels();
      fillPendingParcelSelect();
    } catch (err) {
      handleApiError(err);
    }
  }

  async function loadCouriers() {
    const loading = $("#couriers-loading");
    const empty = $("#couriers-empty");
    const tbody = $("#couriers-tbody");
    loading.classList.remove("hidden");
    empty.classList.add("hidden");
    try {
      const couriers = await Api.listCouriers();
      updateCaches(null, couriers, null);
      tbody.innerHTML = "";
      if (!couriers.length) {
        empty.classList.remove("hidden");
        return;
      }
      couriers.forEach((c) => {
        const avail = Boolean(c.isAvailable);
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${idChip(c.id)}</td>
          <td>
            <strong>${escapeHtml(c.name)}</strong><br />
            <span style="color:var(--muted);font-size:0.85rem">${escapeHtml(c.phone)}</span>
          </td>
          <td>${escapeHtml(c.currentArea)}</td>
          <td>${escapeHtml(c.vehicleType)}</td>
          <td><span class="badge avail-${avail}">${avail ? "Available" : "Busy"}</span></td>
          <td class="actions">
            <button type="button" class="secondary sm" data-avail="${c.id}" data-next="${!avail}">
              Mark ${avail ? "busy" : "available"}
            </button>
            <button type="button" class="danger sm" data-del-courier="${c.id}">Delete</button>
          </td>`;
        tbody.appendChild(tr);
      });
      fillAreaSelect();
    } catch (err) {
      handleApiError(err);
    } finally {
      loading.classList.add("hidden");
    }
  }

  async function createCourier(event) {
    event.preventDefault();
    const body = {
      name: $("#c-name").value.trim(),
      phone: $("#c-phone").value.trim(),
      vehicleType: $("#c-vehicle").value.trim(),
      currentArea: $("#c-area").value.trim(),
      isAvailable: true,
    };
    try {
      await Api.createCourier(body);
      showToast("Courier added", "success");
      event.target.reset();
      closeDrawers();
      await loadCouriers();
    } catch (err) {
      handleApiError(err);
    }
  }

  async function loadDispatch() {
    const loading = $("#deliveries-loading");
    const empty = $("#deliveries-empty");
    const tbody = $("#deliveries-tbody");
    loading.classList.remove("hidden");
    empty.classList.add("hidden");
    try {
      const [parcels, couriers, deliveries] = await Promise.all([
        Api.listParcels(),
        Api.listCouriers(),
        Api.listDeliveries(),
      ]);
      updateCaches(parcels, couriers, deliveries);
      fillPendingParcelSelect();
      fillAreaSelect();

      const sorted = [...deliveries].sort((a, b) => {
        const oa = STATUS_ORDER[a.status] ?? 9;
        const ob = STATUS_ORDER[b.status] ?? 9;
        if (oa !== ob) return oa - ob;
        return String(b.assignedAt || "").localeCompare(String(a.assignedAt || ""));
      });

      tbody.innerHTML = "";
      if (!sorted.length) {
        empty.classList.remove("hidden");
        return;
      }

      sorted.forEach((d) => {
        const p = parcelMap.get(d.parcelId);
        const c = courierMap.get(d.courierId);
        const parcelCell = p
          ? `<strong>${escapeHtml(p.senderName)} → ${escapeHtml(p.receiverName)}</strong><br />${idChip(d.parcelId)}`
          : idChip(d.parcelId);
        const courierCell = c
          ? `<strong>${escapeHtml(c.name)}</strong><br />${idChip(d.courierId)}`
          : idChip(d.courierId);

        let actions = "";
        if (d.status === "ASSIGNED") {
          actions = `<button type="button" class="sm" data-pickup="${d.id}">Pickup</button>`;
        } else if (d.status === "PICKED_UP") {
          actions = `<button type="button" class="sm" data-complete="${d.id}">Complete</button>`;
        } else {
          actions = `<button type="button" class="ghost sm" data-track-parcel="${d.parcelId}">Track</button>`;
        }

        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${idChip(d.id)}</td>
          <td>${parcelCell}</td>
          <td>${courierCell}</td>
          <td>${escapeHtml(d.area)}</td>
          <td>${badge(d.status)}</td>
          <td class="actions">${actions}</td>`;
        tbody.appendChild(tr);
      });
    } catch (err) {
      handleApiError(err);
    } finally {
      loading.classList.add("hidden");
    }
  }

  function goAssignParcel(parcelId) {
    switchPanel("dispatch");
    setTimeout(() => {
      fillPendingParcelSelect(parcelId);
      const sel = $("#d-parcel");
      if (![...sel.options].some((o) => o.value === parcelId)) {
        const p = parcelMap.get(parcelId);
        if (p) {
          const opt = document.createElement("option");
          opt.value = p.id;
          opt.textContent = `${parcelLabel(p)} (${p.status})`;
          sel.appendChild(opt);
        }
      }
      sel.value = parcelId;
    }, 50);
  }

  async function assignDelivery(event) {
    event.preventDefault();
    const parcelId = $("#d-parcel").value.trim();
    let area = useCustomArea
      ? $("#d-area-custom").value.trim()
      : $("#d-area").value.trim();
    if (!parcelId || !area) {
      showToast("Choose a parcel and area", "error");
      return;
    }
    try {
      const created = await Api.assignDelivery(parcelId, area);
      showToast(`Assigned run ${shortId(created.id)}`, "success");
      $("#assign-form").reset();
      useCustomArea = false;
      syncAreaMode();
      await loadDispatch();
    } catch (err) {
      handleApiError(err);
    }
  }

  async function trackParcel(event) {
    event.preventDefault();
    const parcelId = $("#t-parcel").value.trim();
    if (!parcelId) return;
    const box = $("#track-result");
    try {
      const [delivery, parcelStatus, parcel] = await Promise.all([
        Api.trackDelivery(parcelId).catch((e) => ({ error: e.message })),
        Api.getParcelStatus(parcelId).catch((e) => ({ error: e.message })),
        Api.getParcel(parcelId).catch(() => parcelMap.get(parcelId) || null),
      ]);

      const status = parcelStatus.status || (parcel && parcel.status) || null;
      const steps = [
        { key: "PENDING", title: "Booked", meta: "Parcel created" },
        { key: "ASSIGNED", title: "Assigned", meta: delivery.assignedAt ? fmtTime(delivery.assignedAt) : "Waiting" },
        { key: "IN_TRANSIT", title: "In transit", meta: delivery.pickedUpAt ? fmtTime(delivery.pickedUpAt) : "Waiting" },
        { key: "DELIVERED", title: "Delivered", meta: delivery.deliveredAt ? fmtTime(delivery.deliveredAt) : "Waiting" },
      ];

      const order = ["PENDING", "ASSIGNED", "IN_TRANSIT", "DELIVERED"];
      let currentIdx = order.indexOf(status);
      if (status === "PICKED_UP") currentIdx = order.indexOf("IN_TRANSIT");

      const timeline = steps
        .map((step, idx) => {
          let cls = "";
          if (currentIdx > idx) cls = "done";
          if (currentIdx === idx) cls = "current done";
          return `<li class="${cls}"><div class="step-title">${step.title}</div><div class="step-meta">${escapeHtml(step.meta)}</div></li>`;
        })
        .join("");

      const courier = delivery.courierId ? courierMap.get(delivery.courierId) : null;
      const route =
        parcel && parcel.senderName
          ? `${escapeHtml(parcel.senderName)} → ${escapeHtml(parcel.receiverName)}`
          : idChip(parcelId);

      box.innerHTML = `
        <h3 style="margin-bottom:0.75rem">Tracking</h3>
        <dl class="track-summary">
          <dt>Parcel</dt><dd>${route}</dd>
          <dt>Parcel status</dt><dd>${parcelStatus.error ? escapeHtml(parcelStatus.error) : badge(status)}</dd>
          <dt>Delivery</dt><dd>${
            delivery.error
              ? escapeHtml(delivery.error)
              : `${badge(delivery.status)} · area ${escapeHtml(delivery.area || "—")}`
          }</dd>
          <dt>Courier</dt><dd>${
            delivery.error
              ? "—"
              : courier
                ? escapeHtml(courier.name)
                : idChip(delivery.courierId)
          }</dd>
        </dl>
        <ul class="timeline">${timeline}</ul>`;
      box.classList.remove("hidden");
    } catch (err) {
      handleApiError(err);
    }
  }

  function syncAreaMode() {
    $("#d-area-custom-wrap").classList.toggle("hidden", !useCustomArea);
    $("#d-area").disabled = useCustomArea;
    $("#d-area").required = !useCustomArea;
    $("#d-area-custom").required = useCustomArea;
    $("#toggle-area-custom").textContent = useCustomArea ? "Use area list" : "Use custom area";
  }

  function bindEvents() {
    $("#tab-login").addEventListener("click", () => setAuthMode("login"));
    $("#tab-register").addEventListener("click", () => setAuthMode("register"));
    $("#auth-form").addEventListener("submit", handleAuth);
    $("#logout-btn").addEventListener("click", logout);

    $$(".nav-side button").forEach((btn) => {
      btn.addEventListener("click", () => switchPanel(btn.dataset.panel));
    });

    $("#refresh-overview").addEventListener("click", loadOverview);
    $("#goto-dispatch").addEventListener("click", () => switchPanel("dispatch"));

    $("#open-parcel-drawer").addEventListener("click", () => openDrawer("#parcel-drawer"));
    $("#open-courier-drawer").addEventListener("click", () => openDrawer("#courier-drawer"));
    $("#drawer-backdrop").addEventListener("click", closeDrawers);
    $$("[data-close-drawer]").forEach((btn) => btn.addEventListener("click", closeDrawers));

    $("#parcel-form").addEventListener("submit", createParcel);
    $("#refresh-parcels").addEventListener("click", loadParcels);
    $("#parcels-tbody").addEventListener("click", async (e) => {
      const t = e.target;
      if (t.dataset.copy) {
        await navigator.clipboard.writeText(t.dataset.copy);
        showToast("ID copied", "success");
        return;
      }
      if (t.dataset.assignParcel) {
        goAssignParcel(t.dataset.assignParcel);
        return;
      }
      if (t.dataset.trackParcel) {
        switchPanel("track");
        setTimeout(() => {
          fillTrackSelect(t.dataset.trackParcel);
          $("#t-parcel").value = t.dataset.trackParcel;
        }, 50);
        return;
      }
      if (t.dataset.delParcel) {
        if (!confirm("Delete this parcel?")) return;
        try {
          await Api.deleteParcel(t.dataset.delParcel);
          showToast("Parcel deleted", "success");
          await loadParcels();
        } catch (err) {
          handleApiError(err);
        }
      }
    });

    $("#courier-form").addEventListener("submit", createCourier);
    $("#refresh-couriers").addEventListener("click", loadCouriers);
    $("#couriers-tbody").addEventListener("click", async (e) => {
      const t = e.target;
      if (t.dataset.copy) {
        await navigator.clipboard.writeText(t.dataset.copy);
        showToast("ID copied", "success");
        return;
      }
      if (t.dataset.avail) {
        const next = t.dataset.next === "true";
        try {
          await Api.setAvailability(t.dataset.avail, next);
          showToast(next ? "Courier available" : "Courier marked busy", "success");
          await loadCouriers();
        } catch (err) {
          handleApiError(err);
        }
      }
      if (t.dataset.delCourier) {
        if (!confirm("Delete this courier?")) return;
        try {
          await Api.deleteCourier(t.dataset.delCourier);
          showToast("Courier deleted", "success");
          await loadCouriers();
        } catch (err) {
          handleApiError(err);
        }
      }
    });

    $("#assign-form").addEventListener("submit", assignDelivery);
    $("#refresh-deliveries").addEventListener("click", loadDispatch);
    $("#toggle-area-custom").addEventListener("click", () => {
      useCustomArea = !useCustomArea;
      syncAreaMode();
    });
    $("#deliveries-tbody").addEventListener("click", async (e) => {
      const t = e.target;
      if (t.dataset.copy) {
        await navigator.clipboard.writeText(t.dataset.copy);
        showToast("ID copied", "success");
        return;
      }
      if (t.dataset.pickup) {
        try {
          await Api.pickupDelivery(t.dataset.pickup);
          showToast("Marked picked up", "success");
          await loadDispatch();
        } catch (err) {
          handleApiError(err);
        }
      }
      if (t.dataset.complete) {
        try {
          await Api.completeDelivery(t.dataset.complete);
          showToast("Delivery completed", "success");
          await loadDispatch();
        } catch (err) {
          handleApiError(err);
        }
      }
      if (t.dataset.trackParcel) {
        switchPanel("track");
        setTimeout(() => {
          fillTrackSelect(t.dataset.trackParcel);
          $("#t-parcel").value = t.dataset.trackParcel;
          $("#track-form").requestSubmit();
        }, 50);
      }
    });

    $("#track-form").addEventListener("submit", trackParcel);
    syncAreaMode();
  }

  bindEvents();
  if (AuthStore.isLoggedIn()) {
    showApp();
  } else {
    showAuth();
  }
})();
