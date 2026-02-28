// src/components/PlantList.modal.jsx
import React, { useEffect, useState } from "react";
import {
  getAllPlants,
  getCare,
  getInformation,
  getLocation,
} from "../ApiClient";
import "../App.css";

export default function PlantList() {
  const [plants, setPlants] = useState(null); // null = loading
  const [err, setErr] = useState(null);
  const [loadingAll, setLoadingAll] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalData, setModalData] = useState([]); // array of { id, plant, care, info, loc }
  const [modalTitle, setModalTitle] = useState("");

  // focused subtable viewer
  const [subOpen, setSubOpen] = useState(false);
  const [subTitle, setSubTitle] = useState("");
  const [subData, setSubData] = useState(null);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const list = await getAllPlants();
        if (!cancelled) setPlants(list || []);
      } catch (e) {
        if (!cancelled) setErr(String(e));
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const refresh = async () => {
    setErr(null);
    setPlants(null);
    try {
      const list = await getAllPlants();
      setPlants(list || []);
    } catch (e) {
      setErr(String(e));
    }
  };

  // -----------------------
  // Helpers: strip id keys
  // -----------------------
  // Recursively remove keys that look like identifiers (id, plantId, *_id, etc.)
  function stripIdKeys(value) {
    const idKeyRegex = /^(id|ID|Id|.*plant.*id.*|.*_id)$/i;

    if (value === null || value === undefined) return value;
    if (typeof value !== "object") return value;

    if (Array.isArray(value)) return value.map(v => stripIdKeys(v));

    const out = {};
    for (const [k, v] of Object.entries(value)) {
      if (idKeyRegex.test(k)) continue; // skip identifier-like keys
      out[k] = stripIdKeys(v);
    }
    return out;
  }

  // Optional: pretty key labels (camelCase / snake_case -> Title Case)
  const prettyKey = (k) => {
    if (!k || typeof k !== "string") return String(k);
    const spaced = k
      .replace(/_/g, " ")
      .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
      .replace(/\s+/g, " ")
      .trim();
    return spaced.split(" ").map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(" ");
  };

  // stylized renderer for innards (key/value) — uses prettyKey for labels
  const renderStylized = (obj, depth = 0) => {
    if (obj === null || obj === undefined) {
      return <div className="small" style={{ color: "var(--muted)" }}>—</div>;
    }
    if (typeof obj !== "object") {
      if (typeof obj === "boolean") {
        return <span className={`badge-${obj ? "true" : "false"}`}>{String(obj)}</span>;
      }
      return <div className="val">{String(obj)}</div>;
    }
    if (Array.isArray(obj)) {
      return (
        <div className="nested-array" style={{ marginLeft: depth * 10 }}>
          {obj.length === 0 && <div className="small">(empty)</div>}
          {obj.map((it, i) => (
            <div key={i} className="nested-card">
              <div className="small" style={{ fontWeight: 700 }}>#{i + 1}</div>
              <div style={{ marginTop: 6 }}>{renderStylized(it, depth + 1)}</div>
            </div>
          ))}
        </div>
      );
    }
    const entries = Object.entries(obj);
    if (entries.length === 0) return <div className="small">(empty object)</div>;
    return (
      <div className="kv-grid" style={{ marginLeft: depth * 6 }}>
        {entries.map(([k, v]) => (
          <div key={k} className="kv-row">
            <div className="kv-key">{prettyKey(k)}</div>
            <div className="kv-val">{renderStylized(v, depth + 1)}</div>
          </div>
        ))}
      </div>
    );
  };

  // -----------------------
  // Data fetching helpers
  // -----------------------
  const fetchDetailsFor = async (id) => {
    try {
      const [careRes, infoRes, locRes] = await Promise.allSettled([
        getCare(id),
        getInformation(id),
        getLocation(id),
      ]);
      const settle = (r) => (r.status === "fulfilled" ? r.value : { error: String(r.reason) });
      return {
        id,
        care: settle(careRes),
        info: settle(infoRes),
        loc: settle(locRes),
      };
    } catch (e) {
      return {
        id,
        care: { error: String(e) },
        info: { error: String(e) },
        loc: { error: String(e) },
      };
    }
  };

  const getAllDetails = async () => {
    if (!plants || plants.length === 0) return;
    setLoadingAll(true);
    setModalTitle("All plants — details");
    try {
      const ids = plants.map((p) => p.id ?? p.plantId ?? p.Plant_ID);
      const results = await Promise.all(ids.map(fetchDetailsFor));
      const resultsWithPlants = results.map((r) => {
        const plant =
          plants.find((p) => (p.id ?? p.plantId ?? p.Plant_ID) === r.id) ||
          null;
        // defensive: strip nested id keys from care/info/loc for modalData so previews don't leak ids
        return {
          ...r,
          plant,
          care: r.care && typeof r.care === "object" ? stripIdKeys(r.care) : r.care,
          info: r.info && typeof r.info === "object" ? stripIdKeys(r.info) : r.info,
          loc: r.loc && typeof r.loc === "object" ? stripIdKeys(r.loc) : r.loc,
        };
      });
      setModalData(resultsWithPlants);
      setModalOpen(true);
    } finally {
      setLoadingAll(false);
    }
  };

  const getOneAndOpen = async (id) => {
    setModalTitle(`Plant ${id} — details`);
    setModalOpen(true);
    setModalData([{ id, loading: true }]);
    try {
      const detail = await fetchDetailsFor(id);
      const plant =
        plants?.find((p) => (p.id ?? p.plantId ?? p.Plant_ID) === id) || null;
      const cleaned = {
        ...detail,
        care: detail.care && typeof detail.care === "object" ? stripIdKeys(detail.care) : detail.care,
        info: detail.info && typeof detail.info === "object" ? stripIdKeys(detail.info) : detail.info,
        loc: detail.loc && typeof detail.loc === "object" ? stripIdKeys(detail.loc) : detail.loc,
        plant,
      };
      setModalData([cleaned]);
    } catch (e) {
      setModalData([
        {
          id,
          care: { error: String(e) },
          info: { error: String(e) },
          loc: { error: String(e) },
        },
      ]);
    }
  };

  // open focused subtable viewer (title shown keeps the plant id for context,
  // but body uses cleaned data so IDs do not render)
  const openSubtableViewer = (title, data) => {
    setSubTitle(title);
    // defensive: handle null/undefined and arrays/objects
    const cleaned = data && typeof data === "object" ? stripIdKeys(data) : data;
    console.debug("[openSubtableViewer] raw:", data, "cleaned:", cleaned);
    setSubData(cleaned);
    setSubOpen(true);
  };

  if (plants === null) return <div className="card">Loading plants…</div>;
  if (err)
    return (
      <div className="card" style={{ color: "red" }}>
        Error: {err}
      </div>
    );

  return (
    <>
      <div className="card">
        <div className="list-header">
          <h3 style={{ margin: 0 }}>Plants</h3>
          <div className="header-actions">
            <button className="btn" onClick={refresh}>Refresh</button>
            <button className="btn accent" onClick={getAllDetails} disabled={loadingAll}>
              {loadingAll ? "Getting all…" : "Get All Details"}
            </button>
          </div>
        </div>

        <div className="plant-grid" style={{ marginTop: 8 }}>
          {plants.length === 0 && <div className="small">No plants found.</div>}
          {plants.map((p) => {
            const id = p.id ?? p.plantId ?? p.Plant_ID;
            return (
              <div
                key={id}
                className="plant-card"
                onClick={() => getOneAndOpen(id)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => { if (e.key === "Enter") getOneAndOpen(id); }}
              >
                <div className="plant-top">
                  <div>
                    <div className="plant-title">{p.name ?? "Unnamed"}</div>
                    <div className="plant-meta small">
                      <span className="chip">{p.type ?? "Unknown"}</span>
                      <span className="chip">{p.locationName ?? p.location_name ?? "No location"}</span>
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: 6, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <div className="small" style={{ color: "var(--muted)" }}>Height: {p.height ?? "—"}</div>
                  <div className="small" style={{ color: "var(--muted)" }}>ID: {id}</div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Main modal (shows care/info/loc summaries) */}
      {modalOpen && (
        <div className="modal-overlay" role="dialog" aria-modal="true" onClick={() => setModalOpen(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 style={{ margin: 0 }}>{modalTitle}</h3>
              <button className="btn ghost" onClick={() => setModalOpen(false)}>Close</button>
            </div>

            <div className="modal-body">
              {modalData.length === 0 && <div className="small">No details.</div>}
              {modalData.map((item) => {
                const id = item.id;
                const plant = item.plant;
                return (
                  <div key={id} className="modal-plant">
                    <div className="modal-plant-top">
                      <div>
                        <div style={{ fontWeight: 600 }}>{plant?.name ?? `Plant ${id}`}</div>
                        <div className="small" style={{ color: "var(--muted)" }}>{plant?.type ?? ""} • ID: {id}</div>
                      </div>
                    </div>

                    {/* modal-sections — only titles shown; click to open focused viewer */}
                    <div className="modal-sections">
                      {/* CARE */}
                      <div
                        role="button"
                        tabIndex={0}
                        className="modal-section clickable"
                        onClick={(e) => { e.stopPropagation(); openSubtableViewer(`Care — Plant ${id}`, item.care); }}
                        onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); openSubtableViewer(`Care — Plant ${id}`, item.care); } }}
                        aria-label={`View care details for plant ${id}`}
                      >
                        <div className="modal-section-title">Care</div>
                        <div className="small" style={{ color: "var(--muted)", marginTop: 6 }}>Click to view details</div>
                      </div>

                      {/* INFORMATION */}
                      <div
                        role="button"
                        tabIndex={0}
                        className="modal-section clickable"
                        onClick={(e) => { e.stopPropagation(); openSubtableViewer(`Information — Plant ${id}`, item.info); }}
                        onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); openSubtableViewer(`Information — Plant ${id}`, item.info); } }}
                        aria-label={`View information details for plant ${id}`}
                      >
                        <div className="modal-section-title">Information</div>
                        <div className="small" style={{ color: "var(--muted)", marginTop: 6 }}>Click to view details</div>
                      </div>

                      {/* LOCATION */}
                      <div
                        role="button"
                        tabIndex={0}
                        className="modal-section clickable"
                        onClick={(e) => { e.stopPropagation(); openSubtableViewer(`Location — Plant ${id}`, item.loc); }}
                        onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); openSubtableViewer(`Location — Plant ${id}`, item.loc); } }}
                        aria-label={`View location details for plant ${id}`}
                      >
                        <div className="modal-section-title">Location</div>
                        <div className="small" style={{ color: "var(--muted)", marginTop: 6 }}>Click to view details</div>
                      </div>
                    </div>

                    <hr />
                  </div>
                );
              })}
            </div>

            <div className="modal-footer small">Close to return to the plant list.</div>
          </div>
        </div>
      )}

      {/* Focused subtable modal */}
      {subOpen && (
        <div className="modal-overlay" role="dialog" aria-modal="true" onClick={() => setSubOpen(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 style={{ margin: 0 }}>{subTitle}</h3>
              <button className="btn ghost" onClick={() => setSubOpen(false)}>Close</button>
            </div>

            <div className="modal-body">
              {!subData && <div className="small">No data.</div>}
              {subData && (
                <>
                  <div style={{ fontWeight: 700, marginBottom: 8 }}>Structured</div>
                  <div className="info-inner">{renderStylized(subData)}</div>
                </>
              )}
            </div>

            <div className="modal-footer small">Click Close or outside to dismiss.</div>
          </div>
        </div>
      )}
    </>
  );
}