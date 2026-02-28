// src/components/PlantDetail.jsx
import React, { useState } from "react";
import { getCare, getInformation, getLocation } from "../ApiClient";
import "../App.css";

/**
 * PlantDetail component — shows plant summary and three subtable titles.
 * Clicking a section opens a focused stylized modal. The focused modal
 * header includes "ModalType — Plant {id}" while the body hides any ID
 * fields (plantId, id, plant_id, etc.) at any nesting level.
 */
export default function PlantDetail({ plant, onBack }) {
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState("care");
  const [modalData, setModalData] = useState(null);
  const [modalLoading, setModalLoading] = useState(false);
  const [modalError, setModalError] = useState(null);

  if (!plant) {
    return <div className="card">No plant selected.</div>;
  }

  const plantId = plant.id ?? plant.plantId ?? plant.Plant_ID ?? plant.PlantId ?? plant.plant_id;

  const openSubtable = async (type) => {
    const pid = plantId;
    if (!pid) {
      setModalError("Missing plant id");
      setModalOpen(true);
      return;
    }

    setModalType(type);
    setModalOpen(true);
    setModalLoading(true);
    setModalError(null);
    setModalData(null);

    try {
      let res;
      if (type === "care") res = await getCare(pid);
      else if (type === "information") res = await getInformation(pid);
      else if (type === "location") res = await getLocation(pid);
      else throw new Error("Unknown type: " + type);

      if (Array.isArray(res) && res.length === 1) res = res[0];
      setModalData(res);
    } catch (err) {
      console.error("[PlantDetail] openSubtable error", err);
      setModalError(String(err));
    } finally {
      setModalLoading(false);
    }
  };

  const closeModal = () => {
    setModalOpen(false);
    setModalData(null);
    setModalError(null);
  };

  // Keys to hide everywhere
  const forbiddenKeys = new Set([
    "plantId", "plant_id", "Plant_ID", "PlantId", "plantid",
    "id", "ID", "Id"
  ]);

  // Pretty-print keys: camelCase / snake_case -> Title Case
  const prettyKey = (k) => {
    if (!k || typeof k !== "string") return String(k);
    // replace underscores with spaces, insert spaces before caps, then title-case
    const spaced = k
      .replace(/_/g, " ")
      .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
      .replace(/\s+/g, " ")
      .trim();
    return spaced.split(" ").map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(" ");
  };

  // Stylized renderer for key/value UI (handles nested objects/arrays)
  const renderStylized = (obj, depth = 0) => {
    // primitives
    if (obj === null || obj === undefined) {
      return <div className="small" style={{ color: "var(--muted)" }}>—</div>;
    }
    if (typeof obj !== "object") {
      if (typeof obj === "boolean") {
        return <span className={`badge-${obj ? "true" : "false"}`}>{String(obj)}</span>;
      }
      return <div className="val">{String(obj)}</div>;
    }

    // arrays
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

    // plain object: filter out forbidden keys
    const entries = Object.entries(obj).filter(([k, v]) => !forbiddenKeys.has(k));

    // if nothing remains (all fields were IDs or filtered) show muted note
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
      openSubtable(type);
    },
    onKeyDown: (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        openSubtable(type);
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
            <div className="small muted">Click to view details</div>
          </div>

          <div {...makeSectionProps("information")}>
            <h4 className="subtable-title">Information</h4>
            <div className="small muted">Click to view details</div>
          </div>

          <div {...makeSectionProps("location")}>
            <h4 className="subtable-title">Location</h4>
            <div className="small muted">Click to view details</div>
          </div>
        </div>
      </div>

      {/* Focused modal: header shows "Type — Plant {id}" but body hides ID keys */}
      {modalOpen && (
        <div className="modal-overlay" role="dialog" aria-modal="true" onClick={closeModal}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3 style={{ margin: 0 }}>
                  {modalType.charAt(0).toUpperCase() + modalType.slice(1)}{plantId ? ` — Plant ${plantId}` : ""}
                </h3>
                {/* optionally show plant.name as subtitle */}
                <div className="small" style={{ color: "var(--muted)" }}>{plant.name}</div>
              </div>

              <button className="btn ghost" onClick={closeModal}>Close</button>
            </div>

            <div className="modal-body">
              {modalLoading && <div className="small">Loading...</div>}
              {modalError && <div className="small error">{modalError}</div>}

              {!modalLoading && !modalError && modalData && (
                <div className="info-inner">
                  {renderStylized(modalData)}
                </div>
              )}

              {!modalLoading && !modalError && !modalData && (
                <div className="small" style={{ color: "var(--muted)" }}>No data found.</div>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
}