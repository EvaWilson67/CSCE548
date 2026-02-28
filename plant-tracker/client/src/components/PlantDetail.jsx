import React, { useEffect, useState } from "react";
import { getPlant, getCare, getInformation, getLocation } from "../ApiClient";
import "../App.css";

function friendlyDate(d) {
  if (!d) return "—";
  try {
    const dt = new Date(d);
    if (isNaN(dt)) return d;
    return dt.toLocaleDateString();
  } catch {
    return d;
  }
}

export default function PlantDetail({ plantId, onBack }) {
  const [plant, setPlant] = useState(null);
  const [care, setCare] = useState(null);
  const [info, setInfo] = useState(null);
  const [loc, setLoc] = useState(null);
  const [err, setErr] = useState(null);
  const [loading, setLoading] = useState(false);
  const [loadingCare, setLoadingCare] = useState(false);
  const [loadingInfo, setLoadingInfo] = useState(false);
  const [loadingLoc, setLoadingLoc] = useState(false);

  useEffect(() => {
    if (!plantId) {
      setPlant(null);
      setCare(null);
      setInfo(null);
      setLoc(null);
      return;
    }

    let cancelled = false;
    const fetchPlant = async () => {
      // clear previous detail sections immediately to avoid stale display
      setPlant(null);
      setCare(null);
      setInfo(null);
      setLoc(null);
      setLoading(true);
      setErr(null);

      try {
        const p = await getPlant(plantId);
        if (!cancelled) setPlant(p);
      } catch (e) {
        if (!cancelled) setErr(String(e));
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchPlant();
    return () => { cancelled = true; };
  }, [plantId]);

  const fetchCare = async () => {
    setLoadingCare(true);
    setCare(null);
    try {
      const c = await getCare(plantId);
      setCare(c);
    } catch (e) {
      setErr(String(e));
    } finally {
      setLoadingCare(false);
    }
  };
  const fetchInfo = async () => {
    setLoadingInfo(true);
    setInfo(null);
    try {
      const i = await getInformation(plantId);
      setInfo(i);
    } catch (e) {
      setErr(String(e));
    } finally {
      setLoadingInfo(false);
    }
  };
  const fetchLoc = async () => {
    setLoadingLoc(true);
    setLoc(null);
    try {
      const l = await getLocation(plantId);
      setLoc(l);
    } catch (e) {
      setErr(String(e));
    } finally {
      setLoadingLoc(false);
    }
  };

  if (!plantId) return <div className="card small">Select a plant to view details</div>;

  return (
    <div className="card detail">
      <div className="heading">
        <div>
          <h3 style={{ margin: 0 }}>Plant Detail</h3>
          <div className="small" style={{ color: "var(--muted)" }}>ID: {plantId}</div>
        </div>
        <div style={{ display: "flex", gap: 8 }}>
          <button className="btn ghost" onClick={onBack}>Back</button>
          <button className="btn" onClick={() => { /* reserved for Edit flow */ }}>Edit</button>
        </div>
      </div>

      {err && <div className="error">{err}</div>}
      {loading && <div className="loading">Loading plant…</div>}

      {plant && (
        <>
          <div className="fields grid-2">
            <div className="field">
              <label>Name</label>
              <div className="val">{plant.name ?? "—"}</div>
            </div>
            <div className="field">
              <label>Type</label>
              <div className="val">{plant.type ?? "—"}</div>
            </div>
            <div className="field">
              <label>Height</label>
              <div className="val">{plant.height ?? "—"}</div>
            </div>
            <div className="field">
              <label>Date Acquired</label>
              <div className="val">{friendlyDate(plant.dateAcquired ?? plant.DateAcquired)}</div>
            </div>
            <div className="field">
              <label>Location Name</label>
              <div className="val">{plant.locationName ?? plant.location_name ?? "—"}</div>
            </div>
            <div className="field">
              <label>Database ID</label>
              <div className="val">{plant.id ?? plant.plantId ?? plant.Plant_ID}</div>
            </div>
          </div>

          <div className="related">
            <div className="section">
              <div className="section-header">
                <h4 style={{ margin: 0 }}>Care</h4>
                <div>
                  <button className="btn ghost" onClick={fetchCare}>{loadingCare ? "Loading…" : "Get Care"}</button>
                </div>
              </div>
              <div style={{ marginTop: 8 }}>
                {loadingCare ? <div className="small">Loading…</div> : care ? (
                  <div className="info-grid">
                    <div><strong>Last Soil Change:</strong> {friendlyDate(care.lastSoilChange ?? care.LastSoilChange)}</div>
                    <div><strong>Last Watering:</strong> {friendlyDate(care.lastWatering ?? care.LastWatering)}</div>
                  </div>
                ) : <pre>Not fetched</pre>}
              </div>
            </div>

            <div className="section">
              <div className="section-header">
                <h4 style={{ margin: 0 }}>Information</h4>
                <div><button className="btn ghost" onClick={fetchInfo}>{loadingInfo ? "Loading…" : "Get Info"}</button></div>
              </div>
              <div style={{ marginTop: 8 }}>
                {loadingInfo ? <div className="small">Loading…</div> : info ? (
                  <div className="info-grid">
                    <div><strong>From Another Plant:</strong> {String(info.fromAnotherPlant ?? info.FromAnotherPlant)}</div>
                    <div><strong>Soil Type:</strong> {info.soilType ?? info.SoilType ?? "—"}</div>
                    <div><strong>Pot Size:</strong> {info.potSize ?? info.PotSize ?? "—"}</div>
                    <div><strong>Water Globe Required:</strong> {String(info.waterGlobeRequired ?? info.WaterGlobeRequired)}</div>
                  </div>
                ) : <pre>Not fetched</pre>}
              </div>
            </div>

            <div className="section">
              <div className="section-header">
                <h4 style={{ margin: 0 }}>Location</h4>
                <div><button className="btn ghost" onClick={fetchLoc}>{loadingLoc ? "Loading…" : "Get Location"}</button></div>
              </div>
              <div style={{ marginTop: 8 }}>
                {loadingLoc ? <div className="small">Loading…</div> : loc ? (
                  <div className="info-grid">
                    <div><strong>Location Name:</strong> {loc.locationName ?? loc.location_name ?? "—"}</div>
                    <div><strong>Light Level:</strong> {loc.lightLevel ?? loc.LightLevel ?? "—"}</div>
                  </div>
                ) : <pre>Not fetched</pre>}
              </div>
            </div>
          </div>
        </>
      )}

      <div className="footer-note small">Tip: Click "Get Care/Info/Location" to call the corresponding GET endpoints and show results. Use the browser Network tab to capture request/response for evidence.</div>
    </div>
  );
}