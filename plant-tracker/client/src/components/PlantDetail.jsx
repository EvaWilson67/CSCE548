// src/components/PlantDetail.jsx
import React, { useState } from "react";
import { getCare, getInformation, getLocation } from "../ApiClient";
import "../App.css";

/**
 * PlantDetail component — shows plant summary and three subtable titles.
 * Now: clicking a section fetches and expands that section inline
 * (no second modal on top of a modal).
 */
export default function PlantDetail({ plant, onBack }) {
  const [sections, setSections] = useState({
    care: { open: false, loading: false, data: null, error: null },
    information: { open: false, loading: false, data: null, error: null },
    location: { open: false, loading: false, data: null, error: null },
  });

  if (!plant) {
    return <div className="card">No plant selected.</div>;
  }

  const plantId = plant.id ?? plant.plantId ?? plant.Plant_ID ?? plant.PlantId ?? plant.plant_id;

  const fetchSection = async (type) => {
    // toggle semantics: if already open, simply close it
    setSections((prev) => ({ ...prev, [type]: { ...prev[type], open: !prev[type].open } }));
    // if we're opening and have data already, do nothing
    if (sections[type].open === false && (sections[type].data || sections[type].error)) {
      // data already present, just open — above toggle already flipped open, nothing else
      return;
    }
    // if we're opening (previously closed) and no data, fetch
    if (sections[type].open === false) {
      setSections((prev) => ({ ...prev, [type]: { ...prev[type], loading: true, error: null } }));
      try {
        let res;
        if (type === "care") res = await getCare(plantId);
        else if (type === "information") res = await getInformation(plantId);
        else if (type === "location") res = await getLocation(plantId);
        else throw new Error("Unknown type: " + type);

        if (Array.isArray(res) && res.length === 1) res = res[0];
        setSections((prev) => ({ ...prev, [type]: { ...prev[type], data: res, loading: false, error: null, open: true } }));
      } catch (err) {
        console.error("[PlantDetail] fetchSection error", err);
        setSections((prev) => ({ ...prev, [type]: { ...prev[type], error: String(err), loading: false, open: true } }));
      }
    }
  };

  // Keys to hide everywhere
  const forbiddenKeys = new Set([
    "plantId", "plant_id", "Plant_ID", "PlantId", "plantid",
    "id", "ID", "Id"
  ]);

  // Pretty-print keys: camelCase / snake_case -> Title Case
  const prettyKey = (k) => {
    if (!k || typeof k !== "string") return String(k);
    const spaced = k
      .replace(/_/g, " ")
      .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
      .replace(/\s+/g, " ")
      .trim();
    return spaced.split(" ").map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(" ");
  };

  // Stylized renderer for key/value UI (handles nested objects/arrays)
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
      if (obj.length === 0) return <div className="small" style={{ color: "var(--muted)" }}>(empty)</div>;
      return (
        <div className="nested-array" style={{ marginLeft: depth * 8 }}>
          {obj.map((it, i) => (
            <div key={i} className="nested-card">
              <div className="small" style={{ fontWeight: 700, marginBottom: 6 }}>#{i + 1}</div>
              <div>{renderStylized(it, depth + 1)}</div>
            </div>
          ))}
        </div>
      );
    }

    const entries = Object.entries(obj).filter(([k]) => !forbiddenKeys.has(k));
    if (entries.length === 0) {
      return <div className="small" style={{ color: "var(--muted)" }}>(no visible fields)</div>;
    }

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

  const makeSectionProps = (type) => ({
    role: "button",
    tabIndex: 0,
    className: "modal-section clickable section-card",
    onClick: (e) => {
      e.stopPropagation();
      fetchSection(type);
    },
    onKeyDown: (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        fetchSection(type);
      }
    },
  });

  return (
    <>
      <div className="card">
        <div className="list-header">
          <h3 style={{ margin: 0 }}>{plant.name ?? "Unnamed Plant"}</h3>
          {onBack && <button className="btn ghost" onClick={onBack}>Back</button>}
        </div>

        <div style={{ marginTop: 10 }}>
          <div className="small"><strong>Type:</strong> {plant.type ?? "—"}</div>
          <div className="small"><strong>Height:</strong> {plant.height ?? "—"}</div>
        </div>

        <hr />

        <h4 style={{ marginBottom: 12 }}>Details</h4>

        <div className="subtable-grid">
          <div {...makeSectionProps("care")}>
            <h4 className="subtable-title">Care</h4>
            <div className="small muted">Click to expand</div>
          </div>

          <div {...makeSectionProps("information")}>
            <h4 className="subtable-title">Information</h4>
            <div className="small muted">Click to expand</div>
          </div>

          <div {...makeSectionProps("location")}>
            <h4 className="subtable-title">Location</h4>
            <div className="small muted">Click to expand</div>
          </div>
        </div>

        {/* Inline expanded content area */}
        <div style={{ marginTop: 12 }}>
          {/* CARE */}
          {sections.care.open && (
            <div className="card" style={{ marginBottom: 10 }}>
              <div className="list-header">
                <h4 style={{ margin: 0 }}>Care</h4>
                <div className="small" style={{ color: "var(--muted)" }}>{plant.name}</div>
              </div>
              <div className="modal-body" style={{ marginTop: 8 }}>
                {sections.care.loading && <div className="small">Loading…</div>}
                {sections.care.error && <div className="small error">Error: {sections.care.error}</div>}
                {!sections.care.loading && !sections.care.error && sections.care.data && (
                  <div className="info-inner">{renderStylized(sections.care.data)}</div>
                )}
                {!sections.care.loading && !sections.care.error && !sections.care.data && (
                  <div className="small" style={{ color: "var(--muted)" }}>No data.</div>
                )}
              </div>
            </div>
          )}

          {/* INFORMATION */}
          {sections.information.open && (
            <div className="card" style={{ marginBottom: 10 }}>
              <div className="list-header">
                <h4 style={{ margin: 0 }}>Information</h4>
                <div className="small" style={{ color: "var(--muted)" }}>{plant.name}</div>
              </div>
              <div className="modal-body" style={{ marginTop: 8 }}>
                {sections.information.loading && <div className="small">Loading…</div>}
                {sections.information.error && <div className="small error">Error: {sections.information.error}</div>}
                {!sections.information.loading && !sections.information.error && sections.information.data && (
                  <div className="info-inner">{renderStylized(sections.information.data)}</div>
                )}
                {!sections.information.loading && !sections.information.error && !sections.information.data && (
                  <div className="small" style={{ color: "var(--muted)" }}>No data.</div>
                )}
              </div>
            </div>
          )}

          {/* LOCATION */}
          {sections.location.open && (
            <div className="card" style={{ marginBottom: 10 }}>
              <div className="list-header">
                <h4 style={{ margin: 0 }}>Location</h4>
                <div className="small" style={{ color: "var(--muted)" }}>{plant.name}</div>
              </div>
              <div className="modal-body" style={{ marginTop: 8 }}>
                {sections.location.loading && <div className="small">Loading…</div>}
                {sections.location.error && <div className="small error">Error: {sections.location.error}</div>}
                {!sections.location.loading && !sections.location.error && sections.location.data && (
                  <div className="info-inner">{renderStylized(sections.location.data)}</div>
                )}
                {!sections.location.loading && !sections.location.error && !sections.location.data && (
                  <div className="small" style={{ color: "var(--muted)" }}>No data.</div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
}