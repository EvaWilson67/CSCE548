import React, { useEffect, useState } from "react";
import PlantForm from "./PlantForm";
import {
  getPlant,
  updatePlant,
  deletePlant,
  getCare,
  createCare,
  updateCare,
  deleteCare,
  getInformation,
  createInformation,
  updateInformation,
  deleteInformation,
  getLocation,
  createLocation,
  updateLocation,
  deleteLocation,
} from "../ApiClient";

/**
 * PlantDetail: modal with plant header + CRUD for Care / Information / Location
 */
export default function PlantDetail({
  id,
  onClose,
  startEditing = false,
  onSaved,
  onDeleted,
}) {
  const [plant, setPlant] = useState(null);
  const [loadingPlant, setLoadingPlant] = useState(true);
  const [editingPlant, setEditingPlant] = useState(Boolean(startEditing));
  const [plantForm, setPlantForm] = useState({});
  const [savingPlant, setSavingPlant] = useState(false);

  // subresources
  const [care, setCare] = useState({
    loading: true,
    data: null,
    editing: false,
    draft: {},
    saving: false,
  });
  const [info, setInfo] = useState({
    loading: true,
    data: null,
    editing: false,
    draft: {},
    saving: false,
  });
  const [loc, setLoc] = useState({
    loading: true,
    data: null,
    editing: false,
    draft: {},
    saving: false,
  });

  useEffect(() => {
    if (!id && id !== 0) {
      setPlant(null);
      setLoadingPlant(false);
      return;
    }

    let cancelled = false;
    async function loadAll() {
      setLoadingPlant(true);
      try {
        const p = await getPlant(id);
        if (cancelled) return;
        setPlant(p);
        setPlantForm({
          name: p?.name || "",
          type: p?.type || "",
          height: p?.height || "",
          // prefer locationName from plant or fallback underscored key if backend uses snake_case
          location: p?.locationName ?? p?.location_name ?? p?.location ?? "",
          notes: p?.notes || "",
        });
        setEditingPlant(Boolean(startEditing));
      } catch (e) {
        console.error("load plant", e);
        setPlant(null);
      } finally {
        if (!cancelled) setLoadingPlant(false);
      }

      // load subresources
      setCare((c) => ({ ...c, loading: true }));
      setInfo((i) => ({ ...i, loading: true }));
      setLoc((l) => ({ ...l, loading: true }));

      try {
        const [careRes, infoRes, locRes] = await Promise.allSettled([
          getCare(id),
          getInformation(id),
          getLocation(id),
        ]);
        if (cancelled) return;
        const settle = (r) => (r.status === "fulfilled" ? r.value : null);
        const careVal = settle(careRes);
        const infoVal = settle(infoRes);
        const locVal = settle(locRes);

        setCare({
          loading: false,
          data: careVal,
          editing: false,
          draft: careVal
            ? {
                lastSoilChange: careVal.lastSoilChange || "",
                lastWatering: careVal.lastWatering || "",
                notes: careVal.notes || "",
              }
            : { lastSoilChange: "", lastWatering: "", notes: "" },
          saving: false,
        });

        setInfo({
          loading: false,
          data: infoVal,
          editing: false,
          draft: infoVal
            ? {
                soilType: infoVal.soilType || "",
                potSize: infoVal.potSize || "",
                fromAnotherPlant: !!infoVal.fromAnotherPlant,
              }
            : { soilType: "", potSize: "", fromAnotherPlant: false },
          saving: false,
        });

        setLoc({
          loading: false,
          data: locVal,
          editing: false,
          draft: locVal
            ? {
                locationName: locVal.locationName || "",
                lightLevel: locVal.lightLevel || "",
                notes: locVal.notes || "",
              }
            : { locationName: "", lightLevel: "", notes: "" },
          saving: false,
        });

        // prefer locationName from subresource for plant edit form if present
        if (locVal && locVal.locationName) {
          setPlantForm((prev) => ({ ...prev, location: locVal.locationName }));
          // also keep plant.state in sync so header shows it immediately
          setPlant((prev) =>
            prev ? { ...prev, location: locVal.locationName } : prev,
          );
        }
      } catch (e) {
        console.error("load subs", e);
        setCare((c) => ({ ...c, loading: false }));
        setInfo((i) => ({ ...i, loading: false }));
        setLoc((l) => ({ ...l, loading: false }));
      }
    }

    loadAll();
    return () => {
      cancelled = true;
    };
  }, [id, startEditing]);

  // plant header handlers
  const onPlantChange = (field, value) =>
    setPlantForm((prev) => ({ ...prev, [field]: value }));

  const savePlant = async () => {
    setSavingPlant(true);
    try {
      // Simple plant payload — do NOT include locationName here
      const payload = {
        name: plantForm.name,
        type: plantForm.type,
        height: plantForm.height,
        notes: plantForm.notes,
        // intentionally NOT sending locationName here to avoid touching locations table
      };

      console.log("updatePlant payload (no location):", payload);
      const savedPlant = await updatePlant(id, payload);
      console.log("savedPlant response:", savedPlant);
      setPlant(savedPlant);
      setEditingPlant(false);

      if (onSaved) onSaved(savedPlant);
    } catch (e) {
      alert("Save plant failed: " + (e.message || e));
    } finally {
      setSavingPlant(false);
    }
  };

  const handleDeletePlant = async () => {
    if (!confirm("Delete this plant? This will remove associated records."))
      return;
    try {
      await deletePlant(id);
      if (onDeleted) onDeleted(id);
      onClose();
    } catch (e) {
      alert("Delete failed: " + (e.message || e));
    }
  };

  // helpers
  const formatDateDisplay = (val) => {
    if (!val) return "(none)";
    try {
      const d = new Date(val);
      if (isNaN(d)) return val;
      return d.toLocaleDateString();
    } catch {
      return val;
    }
  };

  // ---------- Care handlers ----------
  const startEditingCare = () => {
    setCare((c) => ({
      ...c,
      editing: true,
      draft: c.data
        ? {
            lastSoilChange: c.data.lastSoilChange || "",
            lastWatering: c.data.lastWatering || "",
            notes: c.data.notes || "",
          }
        : { lastSoilChange: "", lastWatering: "", notes: "" },
    }));
  };
  const cancelCareEdit = () =>
    setCare((c) => ({
      ...c,
      editing: false,
      draft: c.data
        ? {
            lastSoilChange: c.data.lastSoilChange || "",
            lastWatering: c.data.lastWatering || "",
            notes: c.data.notes || "",
          }
        : { lastSoilChange: "", lastWatering: "", notes: "" },
    }));
  const changeCareDraft = (field, value) =>
    setCare((c) => ({ ...c, draft: { ...(c.draft || {}), [field]: value } }));

  const saveCare = async () => {
    setCare((c) => ({ ...c, saving: true }));
    try {
      let saved;
      if (care.data) saved = await updateCare(id, care.draft);
      else saved = await createCare(id, care.draft);
      setCare({
        loading: false,
        data: saved,
        editing: false,
        draft: {
          lastSoilChange: saved.lastSoilChange || "",
          lastWatering: saved.lastWatering || "",
          notes: saved.notes || "",
        },
        saving: false,
      });
    } catch (e) {
      alert("Save care failed: " + (e.message || e));
      setCare((c) => ({ ...c, saving: false }));
    }
  };

  const deleteCareHandler = async () => {
    if (!confirm("Delete care record?")) return;
    try {
      await deleteCare(id);
      setCare({
        loading: false,
        data: null,
        editing: false,
        draft: { lastSoilChange: "", lastWatering: "", notes: "" },
        saving: false,
      });
    } catch (e) {
      alert("Delete care failed: " + (e.message || e));
    }
  };

  // ---------- Information handlers ----------
  const startEditingInfo = () => {
    setInfo((i) => ({
      ...i,
      editing: true,
      draft: i.data
        ? {
            soilType: i.data.soilType || "",
            potSize: i.data.potSize || "",
            fromAnotherPlant: !!i.data.fromAnotherPlant,
          }
        : { soilType: "", potSize: "", fromAnotherPlant: false },
    }));
  };
  const cancelInfoEdit = () =>
    setInfo((i) => ({
      ...i,
      editing: false,
      draft: i.data
        ? {
            soilType: i.data.soilType || "",
            potSize: i.data.potSize || "",
            fromAnotherPlant: !!i.data.fromAnotherPlant,
          }
        : { soilType: "", potSize: "", fromAnotherPlant: false },
    }));
  const changeInfoDraft = (field, value) =>
    setInfo((i) => ({ ...i, draft: { ...(i.draft || {}), [field]: value } }));

  const saveInfo = async () => {
    setInfo((i) => ({ ...i, saving: true }));
    try {
      let saved;
      if (info.data) saved = await updateInformation(id, info.draft);
      else saved = await createInformation(id, info.draft);
      setInfo({
        loading: false,
        data: saved,
        editing: false,
        draft: {
          soilType: saved.soilType || "",
          potSize: saved.potSize || "",
          fromAnotherPlant: !!saved.fromAnotherPlant,
        },
        saving: false,
      });
    } catch (e) {
      alert("Save information failed: " + (e.message || e));
      setInfo((i) => ({ ...i, saving: false }));
    }
  };

  const deleteInfoHandler = async () => {
    if (!confirm("Delete information record?")) return;
    try {
      await deleteInformation(id);
      setInfo({
        loading: false,
        data: null,
        editing: false,
        draft: { soilType: "", potSize: "", fromAnotherPlant: false },
        saving: false,
      });
    } catch (e) {
      alert("Delete information failed: " + (e.message || e));
    }
  };

  // ---------- Location handlers ----------
  const startEditingLoc = () => {
    setLoc((l) => ({
      ...l,
      editing: true,
      draft: l.data
        ? {
            locationName: l.data.locationName || "",
            lightLevel: l.data.lightLevel || "",
            notes: l.data.notes || "",
          }
        : { locationName: "", lightLevel: "", notes: "" },
    }));
  };
  const cancelLocEdit = () =>
    setLoc((l) => ({
      ...l,
      editing: false,
      draft: l.data
        ? {
            locationName: l.data.locationName || "",
            lightLevel: l.data.lightLevel || "",
            notes: l.data.notes || "",
          }
        : { locationName: "", lightLevel: "", notes: "" },
    }));
  const changeLocDraft = (field, value) =>
    setLoc((l) => ({ ...l, draft: { ...(l.draft || {}), [field]: value } }));

  const saveLoc = async () => {
    setLoc((l) => ({ ...l, saving: true }));
    try {
      let saved;
      if (loc.data) saved = await updateLocation(id, loc.draft);
      else saved = await createLocation(id, loc.draft);
      setLoc({
        loading: false,
        data: saved,
        editing: false,
        draft: {
          locationName: saved.locationName || "",
          lightLevel: saved.lightLevel || "",
          notes: saved.notes || "",
        },
        saving: false,
      });

      // Sync plantForm.location so header edit shows the value
      setPlantForm((prev) => ({ ...prev, location: saved.locationName || "" }));
      // ALSO sync plant state so other UI reads the new location immediately
      setPlant((prev) =>
        prev ? { ...prev, location: saved.locationName || "" } : prev,
      );
    } catch (e) {
      alert("Save location failed: " + (e.message || e));
      setLoc((l) => ({ ...l, saving: false }));
    }
  };

  const deleteLocHandler = async () => {
    if (!confirm("Delete location record?")) return;
    try {
      await deleteLocation(id);
      setLoc({
        loading: false,
        data: null,
        editing: false,
        draft: { locationName: "", lightLevel: "", notes: "" },
        saving: false,
      });
      setPlantForm((prev) => ({ ...prev, location: "" }));
      setPlant((prev) => (prev ? { ...prev, location: "" } : prev));
    } catch (e) {
      alert("Delete location failed: " + (e.message || e));
    }
  };

  if (loadingPlant)
    return (
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal-card center">Loading…</div>
      </div>
    );

  if (!plant)
    return (
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal-card">
          <div className="modal-header">
            <h3 style={{ margin: 0 }}>Plant not found</h3>
            <div className="controls">
              <button className="btn ghost" onClick={onClose}>
                Close
              </button>
            </div>
          </div>
          <div className="small">Could not load plant.</div>
        </div>
      </div>
    );

  // Choose display location: prefer subresource, then current form (most recent), then plant record
  const displayLocation =
    loc.data && loc.data.locationName
      ? loc.data.locationName
      : plantForm.location || plant.location || "(no location)";

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="title">
          <div>
            <h3 style={{ margin: 0 }}>
              {editingPlant ? "Edit plant" : plant.name || "Plant detail"}
            </h3>
            <div className="small-muted">{displayLocation}</div>
          </div>

          <div className="controls">
            <button className="btn ghost" onClick={onClose}>
              Close
            </button>
          </div>
        </div>

        <div>
          {!editingPlant && (
            <div className="section">
              <div className="title">
                <div>
                  <div style={{ fontWeight: 700 }}>{plant.name}</div>
                  <div className="small-muted">
                    {plant.type} • Height: {plant.height || "—"}
                  </div>
                </div>
                <div className="controls">
                  <button
                    className="btn"
                    onClick={() => {
                      setEditingPlant(true);
                      setPlantForm({
                        name: plant.name || "",
                        type: plant.type || "",
                        height: plant.height || "",
                        // purposely DO NOT include location here — edit location only in the Location section
                        location: plant.location || "",
                        notes: plant.notes || "",
                      });
                    }}
                  >
                    Edit
                  </button>
                  <button className="btn danger" onClick={handleDeletePlant}>
                    Delete
                  </button>
                </div>
              </div>
            </div>
          )}

          {editingPlant && (
            <PlantForm
              value={plantForm}
              onChange={(f, v) => setPlantForm((prev) => ({ ...prev, [f]: v }))}
              onSubmit={savePlant}
              onCancel={() => setEditingPlant(false)}
              submitting={savingPlant}
            />
          )}
        </div>

        <hr style={{ margin: "14px 0" }} />

        {/* CARE SECTION */}
        <div className="section">
          <div className="title">
            <div>
              <strong>Care</strong>
              <div className="small-muted">Watering, soil change, notes</div>
            </div>
            <div className="controls">
              {care.loading ? null : care.editing ? (
                <button className="btn ghost" onClick={cancelCareEdit}>
                  Cancel
                </button>
              ) : (
                <>
                  <button className="btn" onClick={startEditingCare}>
                    {care.data ? "Edit" : "Create"}
                  </button>
                  {care.data && (
                    <button className="btn danger" onClick={deleteCareHandler}>
                      Delete
                    </button>
                  )}
                </>
              )}
            </div>
          </div>

          <div>
            {care.loading && <div className="small">Loading…</div>}

            {!care.loading && !care.editing && care.data && (
              <div>
                <div className="small-muted">
                  Last watering:{" "}
                  {care.data.lastWatering
                    ? formatDateDisplay(care.data.lastWatering)
                    : "(none)"}
                </div>
                <div className="small-muted">
                  Last soil change:{" "}
                  {care.data.lastSoilChange
                    ? formatDateDisplay(care.data.lastSoilChange)
                    : "(none)"}
                </div>
                <div style={{ marginTop: 8 }}>
                  {care.data.notes || "(no notes)"}
                </div>
              </div>
            )}

            {!care.loading && !care.editing && !care.data && (
              <div className="small-muted">(no care record)</div>
            )}

            {care.editing && (
              <div style={{ marginTop: 8 }}>
                <div className="form-row">
                  <input
                    className="input"
                    type="date"
                    placeholder="Last soil change"
                    value={
                      care.draft.lastSoilChange
                        ? care.draft.lastSoilChange.length >= 10
                          ? care.draft.lastSoilChange.slice(0, 10)
                          : care.draft.lastSoilChange
                        : ""
                    }
                    onChange={(e) =>
                      changeCareDraft("lastSoilChange", e.target.value)
                    }
                  />
                  <input
                    className="input"
                    type="date"
                    placeholder="Last watering"
                    value={
                      care.draft.lastWatering
                        ? care.draft.lastWatering.length >= 10
                          ? care.draft.lastWatering.slice(0, 10)
                          : care.draft.lastWatering
                        : ""
                    }
                    onChange={(e) =>
                      changeCareDraft("lastWatering", e.target.value)
                    }
                  />
                </div>
                <div style={{ marginTop: 8 }}>
                  <textarea
                    className="input"
                    placeholder="Notes"
                    value={care.draft.notes || ""}
                    onChange={(e) => changeCareDraft("notes", e.target.value)}
                  />
                </div>
                <div className="actions" style={{ marginTop: 8 }}>
                  <button
                    className="btn accent"
                    onClick={saveCare}
                    disabled={care.saving}
                  >
                    {care.saving ? "Saving…" : "Save"}
                  </button>
                  <button className="btn ghost" onClick={cancelCareEdit}>
                    Cancel
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* INFORMATION SECTION */}
        <div className="section">
          <div className="title">
            <div>
              <strong>Information</strong>
              <div className="small-muted">
                Soil type, pot size, from another plant
              </div>
            </div>
            <div className="controls">
              {info.loading ? null : info.editing ? (
                <button className="btn ghost" onClick={cancelInfoEdit}>
                  Cancel
                </button>
              ) : (
                <>
                  <button className="btn" onClick={startEditingInfo}>
                    {info.data ? "Edit" : "Create"}
                  </button>
                  {info.data && (
                    <button className="btn danger" onClick={deleteInfoHandler}>
                      Delete
                    </button>
                  )}
                </>
              )}
            </div>
          </div>

          <div>
            {info.loading && <div className="small">Loading…</div>}

            {!info.loading && !info.editing && info.data && (
              <div>
                <div className="small-muted">
                  Soil type: {info.data.soilType || "(none)"}
                </div>
                <div className="small-muted">
                  Pot size: {info.data.potSize || "(none)"}
                </div>
                <div className="small-muted">
                  From another plant:{" "}
                  {info.data.fromAnotherPlant ? "Yes" : "No"}
                </div>
              </div>
            )}

            {!info.loading && !info.editing && !info.data && (
              <div className="small-muted">(no information record)</div>
            )}

            {info.editing && (
              <div style={{ marginTop: 8 }}>
                <div className="form-row">
                  <input
                    className="input"
                    placeholder="Soil type"
                    value={info.draft.soilType || ""}
                    onChange={(e) =>
                      changeInfoDraft("soilType", e.target.value)
                    }
                  />
                  <input
                    className="input"
                    placeholder="Pot size"
                    value={info.draft.potSize || ""}
                    onChange={(e) => changeInfoDraft("potSize", e.target.value)}
                  />
                </div>
                <label
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 8,
                    marginTop: 8,
                  }}
                >
                  <input
                    type="checkbox"
                    checked={!!info.draft.fromAnotherPlant}
                    onChange={(e) =>
                      changeInfoDraft("fromAnotherPlant", e.target.checked)
                    }
                  />
                  <span className="small-muted">From another plant</span>
                </label>

                <div className="actions" style={{ marginTop: 8 }}>
                  <button
                    className="btn accent"
                    onClick={saveInfo}
                    disabled={info.saving}
                  >
                    {info.saving ? "Saving…" : "Save"}
                  </button>
                  <button className="btn ghost" onClick={cancelInfoEdit}>
                    Cancel
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* LOCATION SECTION */}
        <div className="section">
          <div className="title">
            <div>
              <strong>Location</strong>
              <div className="small-muted">
                Where the plant lives — light level, notes
              </div>
            </div>
            <div className="controls">
              {loc.loading ? null : loc.editing ? (
                <button className="btn ghost" onClick={cancelLocEdit}>
                  Cancel
                </button>
              ) : (
                <>
                  <button className="btn" onClick={startEditingLoc}>
                    {loc.data ? "Edit" : "Create"}
                  </button>
                  {loc.data && (
                    <button className="btn danger" onClick={deleteLocHandler}>
                      Delete
                    </button>
                  )}
                </>
              )}
            </div>
          </div>

          <div>
            {loc.loading && <div className="small">Loading…</div>}

            {!loc.loading && !loc.editing && loc.data && (
              <div>
                <div className="small-muted">
                  Location name: {loc.data.locationName || "(none)"}
                </div>
                <div className="small-muted">
                  Light level: {loc.data.lightLevel || "(none)"}
                </div>
                <div style={{ marginTop: 8 }}>
                  {loc.data.notes || "(no notes)"}
                </div>
              </div>
            )}

            {!loc.loading && !loc.editing && !loc.data && (
              <div className="small-muted">(no location record)</div>
            )}

            {loc.editing && (
              <div style={{ marginTop: 8 }}>
                <div className="form-row">
                  <input
                    className="input"
                    placeholder="Location name"
                    value={loc.draft.locationName || ""}
                    onChange={(e) =>
                      changeLocDraft("locationName", e.target.value)
                    }
                  />
                  <input
                    className="input"
                    placeholder="Light level"
                    value={loc.draft.lightLevel || ""}
                    onChange={(e) =>
                      changeLocDraft("lightLevel", e.target.value)
                    }
                  />
                </div>
                <div style={{ marginTop: 8 }}>
                  <textarea
                    className="input"
                    placeholder="Notes"
                    value={loc.draft.notes || ""}
                    onChange={(e) => changeLocDraft("notes", e.target.value)}
                  />
                </div>
                <div className="actions" style={{ marginTop: 8 }}>
                  <button
                    className="btn accent"
                    onClick={saveLoc}
                    disabled={loc.saving}
                  >
                    {loc.saving ? "Saving…" : "Save"}
                  </button>
                  <button className="btn ghost" onClick={cancelLocEdit}>
                    Cancel
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
