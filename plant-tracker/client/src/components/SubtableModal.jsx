// src/components/SubtableModal.jsx
import React, { useEffect, useState } from "react";
import PropTypes from "prop-types";
import { getCare, getInformation, getLocation } from "../ApiClient";
import "../App.css";

export default function SubtableModal({ open, onClose, plantId, type }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [data, setData] = useState(null);

  useEffect(() => {
    if (!open) return;
    if (!plantId) return;

    let cancelled = false;
    setLoading(true);
    setError(null);
    setData(null);

    const fetcher = async () => {
      try {
        let res;
        if (type === "care") res = await getCare(plantId);
        else if (type === "information") res = await getInformation(plantId);
        else if (type === "location") res = await getLocation(plantId);
        else throw new Error("Unknown type: " + type);

        if (!cancelled) setData(res);
      } catch (e) {
        if (!cancelled) setError(String(e));
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetcher();
    return () => { cancelled = true; };
  }, [open, plantId, type]);

  if (!open) return null;

  const title = `${type.charAt(0).toUpperCase() + type.slice(1)}`;

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 style={{ margin: 0 }}>{title}</h3>
          <button className="btn ghost" onClick={onClose}>Close</button>
        </div>

        <div className="modal-body">
          {loading && <div className="small">Loading…</div>}
          {error && <div className="small error">Error: {error}</div>}
          {!loading && !error && data && (
            <div className="modal-sections">
              {type === "care" && (
                <div className="modal-section">
                  <div className="modal-section-title">Care</div>
                  <div className="small">
                    Last Soil Change: {data.lastSoilChange ?? data.last_soil_change ?? "—"}<br />
                    Last Watering: {data.lastWatering ?? data.last_watering ?? "—"}<br />
                    Notes: {data.notes ?? data.note ?? "—"}
                  </div>
                </div>
              )}

              {type === "information" && (
                <div className="modal-section">
                  <div className="modal-section-title">Information</div>
                  <div className="small">
                    Soil Type: {data.soilType ?? data.soil_type ?? "—"}<br />
                    Pot Size: {data.potSize ?? data.pot_size ?? "—"}<br />
                    From Another Plant: {String(data.fromAnotherPlant ?? data.from_another_plant ?? false)}
                  </div>
                </div>
              )}

              {type === "location" && (
                <div className="modal-section">
                  <div className="modal-section-title">Location</div>
                  <div className="small">
                    Location Name: {data.locationName ?? data.location_name ?? "—"}<br />
                    Light Level: {data.lightLevel ?? data.light_level ?? "—"}<br />
                    Notes: {data.notes ?? "—"}
                  </div>
                </div>
              )}
            </div>
          )}

          {!loading && !error && !data && <div className="small">No data.</div>}
        </div>

        <div className="modal-footer small">Click Close or outside to dismiss.</div>
      </div>
    </div>
  );
}

SubtableModal.propTypes = {
  open: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  plantId: PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired,
  type: PropTypes.oneOf(["care", "information", "location"]).isRequired,
};