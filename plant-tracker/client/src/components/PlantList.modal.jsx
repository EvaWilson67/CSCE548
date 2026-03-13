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

  // expanded inline sections map: { [id]: { care: bool, information: bool, location: bool } }
  const [expanded, setExpanded] = useState({});

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

  const prettyKey = (k) => {
    if (!k || typeof k !== "string") return String(k);
    const spaced = k
      .replace(/_/g, " ")
      .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
      .replace(/\s+/g, " ")
      .trim();
    return spaced.split(" ").map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(" ");
  };

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
        return {
          ...r,
          plant,
          care: r.care && typeof r.care === "object" ? stripIdKeys(r.care) : r.care,
          info: r.info && typeof r.info === "object" ? stripIdKeys(r.info) : r.info,
          loc: r.loc && typeof r.loc === "object" ? stripIdKeys(r.loc) : r.loc,
        };
      });
      setModalData(resultsWithPlants);
      // initialize expanded map for these ids (collapsed by default)
      const exp = {};
      resultsWithPlants.forEach((it) => {
        exp[it.id] = { care: false, information: false, location: false };
      });
      setExpanded(exp);
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
      // init expansion for this id
      setExpanded({ [id]: { care: false, information: false, location: false } });
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

  const toggleExpand = (id, key) => {
    setExpanded((prev) => {
      const copy = { ...(prev || {}) };
      copy[id] = { ...(copy[id] || { care: false, information: false, location: false }) };
      copy[id][key] = !copy[id][key];
      return copy;
    });
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

      {/* Main modal (shows care/info/loc summaries inline expandable) */}
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
                const expandedForId = expanded[id] || { care: false, information: false, location: false };
                return (
                  <div key={id} className="modal-plant">
                    <div className="modal-plant-top">
                      <div>
                        <div style={{ fontWeight: 600 }}>{plant?.name ?? `Plant ${id}`}</div>
                        <div className="small" style={{ color: "var(--muted)" }}>{plant?.type ?? ""} • ID: {id}</div>
                      </div>
                    </div>

                    {/* modal-sections — expand inline instead of opening a sub-modal */}
                    <div className="modal-sections">
                      {/* CARE */}
                      <div className="modal-section">
                        <div
                          role="button"
                          tabIndex={0}
                          className="modal-section clickable"
                          onClick={(e) => { e.stopPropagation(); toggleExpand(id, "care"); }}
                          onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); toggleExpand(id, "care"); } }}
                          aria-label={`Toggle care details for plant ${id}`}
                          style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}
                        >
                          <div>
                            <div className="modal-section-title">Care</div>
                            <div className="small" style={{ color: "var(--muted)", marginTop: 6 }}>Click to expand</div>
                          </div>
                          <div className="small">{expandedForId.care ? "▲" : "▼"}</div>
                        </div>

                        {expandedForId.care && (
                          <div style={{ marginTop: 8 }} className="info-inner">
                            {item.care && item.care.error
                              ? <div className="small error">Error: {item.care.error}</div>
                              : renderStylized(item.care)}
                          </div>
                        )}
                      </div>

                      {/* INFORMATION */}
                      <div className="modal-section">
                        <div
                          role="button"
                          tabIndex={0}
                          className="modal-section clickable"
                          onClick={(e) => { e.stopPropagation(); toggleExpand(id, "information"); }}
                          onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); toggleExpand(id, "information"); } }}
                          aria-label={`Toggle information details for plant ${id}`}
                          style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}
                        >
                          <div>
                            <div className="modal-section-title">Information</div>
                            <div className="small" style={{ color: "var(--muted)", marginTop: 6 }}>Click to expand</div>
                          </div>
                          <div className="small">{expandedForId.information ? "▲" : "▼"}</div>
                        </div>

                        {expandedForId.information && (
                          <div style={{ marginTop: 8 }} className="info-inner">
                            {item.info && item.info.error
                              ? <div className="small error">Error: {item.info.error}</div>
                              : renderStylized(item.info)}
                          </div>
                        )}
                      </div>

                      {/* LOCATION */}
                      <div className="modal-section">
                        <div
                          role="button"
                          tabIndex={0}
                          className="modal-section clickable"
                          onClick={(e) => { e.stopPropagation(); toggleExpand(id, "location"); }}
                          onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); toggleExpand(id, "location"); } }}
                          aria-label={`Toggle location details for plant ${id}`}
                          style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}
                        >
                          <div>
                            <div className="modal-section-title">Location</div>
                            <div className="small" style={{ color: "var(--muted)", marginTop: 6 }}>Click to expand</div>
                          </div>
                          <div className="small">{expandedForId.location ? "▲" : "▼"}</div>
                        </div>

                        {expandedForId.location && (
                          <div style={{ marginTop: 8 }} className="info-inner">
                            {item.loc && item.loc.error
                              ? <div className="small error">Error: {item.loc.error}</div>
                              : renderStylized(item.loc)}
                          </div>
                        )}
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
    </>
  );
}