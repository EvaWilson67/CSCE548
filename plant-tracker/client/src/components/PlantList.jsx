// src/components/PlantList.modal.jsx
import React, { useEffect, useState } from "react";
import { getAllPlants, getCare, getInformation, getLocation } from "../ApiClient";
import "../App.css"; // or ensure App.css is imported in main.jsx

export default function PlantList({ onSelect }) {
  const [plants, setPlants] = useState(null); // null = loading
  const [err, setErr] = useState(null);
  const [loadingAll, setLoadingAll] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalData, setModalData] = useState([]); // array of { id, plant, care, info, loc }
  const [modalTitle, setModalTitle] = useState("");

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
    return () => { cancelled = true; };
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

  // helper that fetches care/info/loc for a given id
  const fetchDetailsFor = async (id) => {
    try {
      const [careRes, infoRes, locRes] = await Promise.allSettled([
        getCare(id),
        getInformation(id),
        getLocation(id),
      ]);
      const settle = (r) => (r.status === "fulfilled" ? r.value : { error: String(r.reason) });
      return { id, care: settle(careRes), info: settle(infoRes), loc: settle(locRes) };
    } catch (e) {
      return { id, care: { error: String(e) }, info: { error: String(e) }, loc: { error: String(e) } };
    }
  };

  // Get details for all plants and open modal
  const getAllDetails = async () => {
    if (!plants || plants.length === 0) return;
    setLoadingAll(true);
    setModalTitle("All plants — details");
    try {
      const ids = plants.map(p => p.id ?? p.plantId ?? p.Plant_ID);
      const results = await Promise.all(ids.map(fetchDetailsFor));
      const resultsWithPlants = results.map(r => {
        const plant = plants.find(p => (p.id ?? p.plantId ?? p.Plant_ID) === r.id) || null;
        return { ...r, plant };
      });
      setModalData(resultsWithPlants);
      setModalOpen(true);
    } finally {
      setLoadingAll(false);
    }
  };

  // Get & open details for single plant
  const getOneAndOpen = async (id) => {
    setModalTitle(`Plant ${id} — details`);
    setModalOpen(true); // open early for snappier UI
    setModalData([{ id, loading: true }]);
    try {
      const detail = await fetchDetailsFor(id);
      const plant = plants?.find(p => (p.id ?? p.plantId ?? p.Plant_ID) === id) || null;
      setModalData([{ ...detail, plant }]);
    } catch (e) {
      setModalData([{ id, care: { error: String(e) }, info: { error: String(e) }, loc: { error: String(e) } }]);
    }
  };

  if (plants === null) return <div className="card">Loading plants…</div>;
  if (err) return <div className="card" style={{ color: "red" }}>Error: {err}</div>;

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
          {plants.map(p => {
            const id = p.id ?? p.plantId ?? p.Plant_ID;
            return (
              <div
                key={id}
                className="plant-card"
                onClick={() => getOneAndOpen(id)} // <--- clicking the card opens modal with details
              >
                <div className="plant-top">
                  <div>
                    <div className="plant-title">{p.name ?? "Unnamed"}</div>
                    <div className="plant-meta small">
                      <span className="chip">{p.type ?? "Unknown"}</span>
                      <span className="chip">{p.locationName ?? p.location_name ?? "No location"}</span>
                    </div>
                  </div>
                  <div className="plant-actions">
                    {/* Open still navigates to detail pane, if you want that */}
                    <button
                      className="btn ghost"
                      onClick={(e) => { e.stopPropagation(); if (typeof onSelect === "function") onSelect(id); }}>
                      Open
                    </button>

                    {/* Get button also opens modal for just this plant */}
                    <button
                      className="btn tiny"
                      onClick={(e) => { e.stopPropagation(); getOneAndOpen(id); }}>
                      Get
                    </button>
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

      {/* Modal: reused for both Get All and single plant */}
      {modalOpen && (
        <div className="modal-overlay" role="dialog" aria-modal="true" onClick={() => setModalOpen(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 style={{ margin: 0 }}>{modalTitle}</h3>
              <button className="btn ghost" onClick={() => setModalOpen(false)}>Close</button>
            </div>

            <div className="modal-body">
              {modalData.length === 0 && <div className="small">No details.</div>}
              {modalData.map(item => {
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

                    <div className="modal-sections">
                      <div className="modal-section">
                        <div className="modal-section-title">Care</div>
                        {item.care?.error ? (
                          <div className="small error">{String(item.care.error)}</div>
                        ) : item.care?.loading ? (
                          <div className="small">Loading…</div>
                        ) : item.care ? (
                          <div className="small">
                            Last Soil Change: {(item.care.lastSoilChange ?? item.care.LastSoilChange) || "—"}<br />
                            Last Watering: {(item.care.lastWatering ?? item.care.LastWatering) || "—"}
                          </div>
                        ) : <div className="small">Not fetched</div>}
                      </div>

                      <div className="modal-section">
                        <div className="modal-section-title">Information</div>
                        {item.info?.error ? (
                          <div className="small error">{String(item.info.error)}</div>
                        ) : item.info ? (
                          <div className="small">
                            Soil Type: {item.info.soilType ?? item.info.SoilType ?? "—"}<br />
                            Pot Size: {item.info.potSize ?? item.info.PotSize ?? "—"}
                          </div>
                        ) : <div className="small">Not fetched</div>}
                      </div>

                      <div className="modal-section">
                        <div className="modal-section-title">Location</div>
                        {item.loc?.error ? (
                          <div className="small error">{String(item.loc.error)}</div>
                        ) : item.loc ? (
                          <div className="small">
                            Location Name: {item.loc.locationName ?? item.loc.location_name ?? "—"}<br />
                            Light Level: {item.loc.lightLevel ?? item.loc.LightLevel ?? "—"}
                          </div>
                        ) : <div className="small">Not fetched</div>}
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