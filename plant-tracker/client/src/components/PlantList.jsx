import React, { useEffect, useState, useCallback } from "react";
import { getAllPlants, getLocation } from "../ApiClient";
import PlantDetail from "./PlantDetail";
import AddPlantModal from "./AddPlantModal";

/**
 * PlantList - uses AddPlantModal for creating plants (with Care/Info/Location).
 * Replaces inline add row (which caused the modal/500 mismatch).
 */
export default function PlantList() {
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState(null);

  const [newOpen, setNewOpen] = useState(false);

  const [detailId, setDetailId] = useState(null);
  const [detailStartEditing, setDetailStartEditing] = useState(false);

  useEffect(() => {
    refresh();
  }, []);

  async function refresh() {
    setLoading(true);
    setErr(null);
    try {
      const list = await getAllPlants();
      const arr = Array.isArray(list) ? list.filter(Boolean) : [];

      // Normalize plant objects so we always have .location available
      const normalized = arr.map((p) => {
        if (!p) return p;
        const id = p?.id ?? p?.plantId ?? p?.Plant_ID;
        const locationFromPlant =
          p.location ?? p.locationName ?? p.location_name ?? "";
        return { ...p, id, plantId: id, location: locationFromPlant };
      });

      // fetch missing location subresources if needed
      const toFetch = normalized.filter(
        (p) => (p.id || p.id === 0) && (!p.location || p.location === ""),
      );

      if (toFetch.length > 0) {
        const promises = toFetch.map((t) =>
          getLocation(t.id)
            .then((res) => ({ id: t.id, res }))
            .catch(() => ({ id: t.id, res: null })),
        );
        const settled = await Promise.all(promises);
        const idToLocation = {};
        settled.forEach((s) => {
          if (s && s.res && s.res.locationName)
            idToLocation[s.id] = s.res.locationName;
        });

        const merged = normalized.map((p) => {
          if ((!p.location || p.location === "") && idToLocation[p.id]) {
            return { ...p, location: idToLocation[p.id] };
          }
          return p;
        });
        setPlants(merged);
      } else {
        setPlants(normalized);
      }
    } catch (e) {
      console.error("refresh plants failed", e);
      setErr(String(e));
      setPlants([]);
    } finally {
      setLoading(false);
    }
  }

  // callback invoked by PlantDetail when it fetches a location subresource
  // stable callback invoked by PlantDetail when it fetches a location subresource
  const onLocationLoaded = useCallback((plantId, locationName) => {
    if (!plantId && plantId !== 0) return;
    setPlants((prev) => {
      if (!prev) return prev;
      return prev.map((p) => {
        const id = p?.id ?? p?.plantId ?? p?.Plant_ID;
        if (id === plantId && (!p.location || p.location !== locationName)) {
          return { ...p, location: locationName };
        }
        return p;
      });
    });
  }, [setPlants]);

  // called by AddPlantModal after successful create (created may include .location / _location / _care / _information)
  const handleCreated = (created) => {
    if (!created) return;
    const id = created?.id ?? created?.plantId ?? created?.Plant_ID;
    const locationFromCreated =
      created.location ||
      (created._location && created._location.locationName) ||
      null;
    const createdNormalized = { ...created };
    if (locationFromCreated) createdNormalized.location = locationFromCreated;

    setPlants((prev) => (prev || []).concat(createdNormalized));
    // open detail for the new plant
    setDetailStartEditing(false);
    if (id !== undefined && id !== null) setDetailId(id);
    setNewOpen(false);
  };

  if (loading) return <div className="card">Loading plants…</div>;
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
          <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
            <div className="small">Total: {plants.length}</div>
            <div style={{ display: "flex", gap: 8 }}>
              <button className="btn" onClick={() => setNewOpen(true)}>
                New Plant
              </button>
              <button className="btn ghost" onClick={refresh}>
                Refresh
              </button>
            </div>
          </div>
        </div>

        <div style={{ marginTop: 12 }}>
          <div className="plant-grid">
            {plants.length === 0 && (
              <div className="small-muted">No plants yet.</div>
            )}
            {plants.map((p) => {
              const id = p?.id ?? p?.plantId ?? p?.Plant_ID;
              if (id === undefined || id === null) return null;
              return (
                <div
                  key={id}
                  className="plant-card"
                  onClick={() => {
                    setDetailStartEditing(false);
                    setDetailId(id);
                  }}
                >
                  <div>
                    <div className="plant-title">{p.name || "(no name)"}</div>
                    <div className="plant-meta">
                      {p.type || "—"} • {p.location || "no location"}
                    </div>
                  </div>
                  <div
                    style={{
                      marginTop: 8,
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                    }}
                  >
                    <div className="small-muted">Height: {p.height || "—"}</div>
                    <div className="small-muted">ID: {id}</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <AddPlantModal
        open={newOpen}
        onClose={() => setNewOpen(false)}
        onCreated={handleCreated}
      />

      {detailId && (
        <PlantDetail
          id={detailId}
          startEditing={detailStartEditing}
          // Close (overlay / programmatic): just close the modal without refreshing
          onClose={() => {
            setDetailId(null);
            setDetailStartEditing(false);
          }}
          // Explicit Close/OK button in the modal will call this to trigger a refresh
          onCloseConfirmed={() => {
            // refresh once when user explicitly closes (OK)
            refresh();
            // then close modal
            setDetailId(null);
            setDetailStartEditing(false);
          }}
          onSaved={(saved) => {
            const idVal = saved?.id ?? saved?.plantId ?? saved?.Plant_ID;
            const locationFromSaved =
              saved.location ??
              saved.locationName ??
              saved.location_name ??
              (saved._location && saved._location.locationName) ??
              "";
            const normalized = {
              ...saved,
              id: idVal,
              plantId: idVal,
              location: locationFromSaved,
            };

            setPlants((prev) => {
              const existing = (prev || []).findIndex(
                (x) => (x.id ?? x.plantId ?? x.Plant_ID) === idVal,
              );
              if (existing >= 0) {
                const cp = [...prev];
                cp[existing] = normalized;
                return cp;
              }
              return [...(prev || []), normalized];
            });
          }}
          onDeleted={(deletedId) => {
            setPlants((prev) =>
              (prev || []).filter(
                (p) => (p.id ?? p.plantId ?? p.Plant_ID) !== deletedId,
              ),
            );
            setDetailId(null);
          }}
          onLocationLoaded={onLocationLoaded}
        />
      )}
    </>
  );
}
