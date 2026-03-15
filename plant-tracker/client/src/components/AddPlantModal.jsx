import React, { useEffect, useRef, useState } from "react";
import {
  createPlant,
  createLocation,
  createCare,
  createInformation
} from "../ApiClient";

/**
 * Simple ControlledInput / ControlledTextarea helpers.
 * These do NOT try to intercept composition — they mirror the working
 * pattern (call onValueChange on every onChange). ControlledInput
 * forwards ref so focus(nameRef) still works.
 */
const ControlledInput = React.forwardRef(function ControlledInput(
  { value, onValueChange, ...props },
  ref
) {
  return (
    <input
      {...props}
      ref={ref}
      value={value}
      onChange={(e) => onValueChange?.(e.target.value)}
    />
  );
});

function ControlledTextarea({ value, onValueChange, ...props }) {
  return (
    <textarea
      {...props}
      value={value}
      onChange={(e) => onValueChange?.(e.target.value)}
    />
  );
}

export default function AddPlantModal({ open, onClose, onCreated }) {
  // form state
  const [plantForm, setPlantForm] = useState({ name: "", type: "", height: "", location: "", notes: "" });
  const [careForm, setCareForm] = useState({ lastSoilChange: "", lastWatering: "", notes: "" });
  const [infoForm, setInfoForm] = useState({ soilType: "", potSize: "", fromAnotherPlant: false });
  const [locForm, setLocForm] = useState({ locationName: "", lightLevel: "", notes: "" });

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const nameRef = useRef(null);
  const formRef = useRef(null);

  // Reset forms only when the dialog opens (open transitions)
  useEffect(() => {
    if (!open) return;
    setPlantForm({ name: "", type: "", height: "", location: "", notes: "" });
    setCareForm({ lastSoilChange: "", lastWatering: "", notes: "" });
    setInfoForm({ soilType: "", potSize: "", fromAnotherPlant: false });
    setLocForm({ locationName: "", lightLevel: "", notes: "" });
    setSaving(false);
    setError("");

    // Prevent background scroll while open. Focus AFTER paint (single time).
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    // small deferred focus — avoids interfering with IME on some browsers
    const t = setTimeout(() => {
      try { nameRef.current?.focus(); } catch (err) { console.debug("focus fail", err); }
    }, 0);
    return () => {
      clearTimeout(t);
      document.body.style.overflow = prev || "";
    };
  }, [open]);

  // Escape closes modal; no Enter global handler (avoid double submit / interference)
  useEffect(() => {
    if (!open) return;
    const onKey = (e) => {
      if (e.key === "Escape") {
        e.preventDefault();
        handleCancel();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  // helpers to update state
  const changePlant = (k, v) => setPlantForm(prev => ({ ...prev, [k]: v }));
  const changeCare = (k, v) => setCareForm(prev => ({ ...prev, [k]: v }));
  const changeInfo = (k, v) => setInfoForm(prev => ({ ...prev, [k]: v }));
  const changeLoc = (k, v) => setLocForm(prev => ({ ...prev, [k]: v }));

  const hasCareData = () =>
    (careForm.lastSoilChange && String(careForm.lastSoilChange).trim() !== "") ||
    (careForm.lastWatering && String(careForm.lastWatering).trim() !== "") ||
    (careForm.notes && careForm.notes.trim() !== "");

  const hasInfoData = () =>
    (infoForm.soilType && infoForm.soilType.trim() !== "") ||
    (infoForm.potSize && infoForm.potSize.trim() !== "") ||
    infoForm.fromAnotherPlant === true;

  const hasLocData = () =>
    (locForm.locationName && locForm.locationName.trim() !== "") ||
    (locForm.lightLevel && locForm.lightLevel.trim() !== "") ||
    (locForm.notes && locForm.notes.trim() !== "");

  const validate = () => {
    if (!plantForm.name || plantForm.name.trim() === "") {
      setError("Please enter a name for this plant.");
      return false;
    }
    if (!plantForm.type || plantForm.type.trim() === "") {
      setError("Please enter a plant type.");
      return false;
    }
    setError("");
    return true;
  };

  const extractPlantId = (created) => created?.id ?? created?.plantId ?? created?.Plant_ID ?? null;

  const handleSubmit = async (e) => {
    if (e && e.preventDefault) e.preventDefault();
    if (saving) return;
    if (!validate()) return;

    setSaving(true);
    setError("");
    try {
      const payload = {
        name: plantForm.name.trim(),
        type: plantForm.type.trim(),
        height: plantForm.height ? plantForm.height.trim() : "",
        notes: plantForm.notes ? plantForm.notes.trim() : ""
      };

      const created = await createPlant(payload);
      const plantId = extractPlantId(created);
      const createdResult = { ...created };

      // location (prefer locForm)
      try {
        if (hasLocData()) {
          const locPayload = {
            locationName: locForm.locationName ? locForm.locationName.trim() : "",
            lightLevel: locForm.lightLevel ? locForm.lightLevel.trim() : "",
            notes: locForm.notes ? locForm.notes.trim() : ""
          };
          if (plantId != null) {
            const createdLoc = await createLocation(plantId, locPayload);
            createdResult.location = createdLoc.locationName || locPayload.locationName;
            createdResult._location = createdLoc;
          }
        } else if (plantForm.location && plantForm.location.trim() !== "") {
          if (plantId != null) {
            const createdLoc = await createLocation(plantId, { locationName: plantForm.location.trim() });
            createdResult.location = createdLoc.locationName || plantForm.location.trim();
            createdResult._location = createdLoc;
          }
        }
      } catch (locErr) {
        console.warn("createLocation failed", locErr);
        setError(prev => prev ? prev + " | Location create failed" : "Location create failed");
      }

      // care
      try {
        if (hasCareData() && plantId != null) {
          const carePayload = {
            lastSoilChange: careForm.lastSoilChange ? careForm.lastSoilChange : "",
            lastWatering: careForm.lastWatering ? careForm.lastWatering : "",
            notes: careForm.notes ? careForm.notes.trim() : ""
          };
          const createdCare = await createCare(plantId, carePayload);
          createdResult._care = createdCare;
        }
      } catch (careErr) {
        console.warn("createCare failed", careErr);
        setError(prev => prev ? prev + " | Care create failed" : "Care create failed");
      }

      // information
      try {
        if (hasInfoData() && plantId != null) {
          const infoPayload = {
            soilType: infoForm.soilType ? infoForm.soilType.trim() : "",
            potSize: infoForm.potSize ? infoForm.potSize.trim() : "",
            fromAnotherPlant: !!infoForm.fromAnotherPlant
          };
          const createdInfo = await createInformation(plantId, infoPayload);
          createdResult._information = createdInfo;
        }
      } catch (infoErr) {
        console.warn("createInformation failed", infoErr);
        setError(prev => prev ? prev + " | Information create failed" : "Information create failed");
      }

      if (typeof onCreated === "function") onCreated(createdResult);
      onClose();
    } catch (e) {
      console.error("AddPlantModal create error", e);
      const server = e && e.body ? (e.body.message || JSON.stringify(e.body)) : e.message || String(e);
      setError(`Create failed${e.status ? ` (${e.status})` : ""}: ${server}`);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setError("");
    onClose();
  };

  const plantRowItem = (basis = "30%") => ({ flex: `1 1 ${basis}`, minWidth: 140 });

  if (!open) return null;

  return (
    <div className="modal-overlay" onClick={handleCancel} role="presentation" aria-hidden={!open}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true" aria-labelledby="add-plant-title">
        <div className="modal-header">
          <div>
            <h3 id="add-plant-title" style={{ margin: 0 }}>Add new plant</h3>
            <div className="small-muted" style={{ marginTop: 4 }}>Give this plant a name and basic details</div>
          </div>

          <div className="controls">
            <button className="btn ghost" onClick={handleCancel} disabled={saving} aria-label="Close">Close</button>
          </div>
        </div>

        <form ref={formRef} onSubmit={handleSubmit} style={{ display: "grid", gap: 12 }}>
          <fieldset style={{ border: "none", padding: 0, margin: 0 }}>
            <div className="form-row" style={{ alignItems: "flex-start", gap: 12 }}>
              <div style={plantRowItem("36%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="plant-name" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Name <span aria-hidden style={{ color: "#b91c1c" }}>*</span>
                  </label>
                  <ControlledInput
                    id="plant-name"
                    ref={nameRef}
                    className="input padded"
                    value={plantForm.name}
                    onValueChange={(v) => changePlant("name", v)}
                    placeholder="Plant name"
                    aria-required
                  />
                </div>
              </div>

              <div style={plantRowItem("22%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="plant-type" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Type <span aria-hidden style={{ color: "#b91c1c" }}>*</span>
                  </label>
                  <ControlledInput
                    id="plant-type"
                    className="input small padded"
                    value={plantForm.type}
                    onValueChange={(v) => changePlant("type", v)}
                    placeholder="e.g. Ficus"
                    aria-required
                  />
                </div>
              </div>

              <div style={plantRowItem("14%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="plant-height" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Height
                  </label>
                  <ControlledInput
                    id="plant-height"
                    className="input small padded"
                    value={plantForm.height}
                    onValueChange={(v) => changePlant("height", v)}
                    placeholder="24in"
                  />
                </div>
              </div>

              <div style={plantRowItem("28%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="plant-location-inline" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Location
                  </label>
                  <div className="small-muted" style={{ fontSize: 12, marginBottom: 6 }}>Quick location, or add detailed below</div>
                  <ControlledInput
                    id="plant-location-inline"
                    className="input padded"
                    value={plantForm.location}
                    onValueChange={(v) => changePlant("location", v)}
                    placeholder="e.g. Hallway"
                  />
                </div>
              </div>
            </div>
          </fieldset>

          <div>
            {/* <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
              <label htmlFor="plant-notes" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                Notes
              </label>
              <div className="small-muted" style={{ fontSize: 12 }}>Optional notes about this plant</div>
              <ControlledTextarea
                id="plant-notes"
                className="input padded"
                value={plantForm.notes}
                onValueChange={(v) => changePlant("notes", v)}
                placeholder="Optional notes"
              />
            </div> */}
          </div>

          <hr />

          {/* Care */}
          <div className="section" aria-labelledby="care-legend">
            <div className="title" style={{ marginBottom: 8 }}>
              <div>
                <strong id="care-legend">Care</strong>
                <div className="small-muted">Watering, soil change, notes</div>
              </div>
            </div>

            <div className="form-row" style={{ marginTop: 8, gap: 12 }}>
              <div style={plantRowItem("45%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="last-soil" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Last soil change
                  </label>
                  <ControlledInput
                    id="last-soil"
                    className="input padded"
                    type="date"
                    value={careForm.lastSoilChange ? careForm.lastSoilChange.slice(0, 10) : ""}
                    onValueChange={(v) => changeCare("lastSoilChange", v)}
                  />
                </div>
              </div>

              <div style={plantRowItem("45%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="last-water" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Last watering
                  </label>
                  <ControlledInput
                    id="last-water"
                    className="input padded"
                    type="date"
                    value={careForm.lastWatering ? careForm.lastWatering.slice(0, 10) : ""}
                    onValueChange={(v) => changeCare("lastWatering", v)}
                  />
                </div>
              </div>
            </div>

            {/* <div style={{ marginTop: 10 }}>
              <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                <label htmlFor="care-notes" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                  Care notes
                </label>
                <ControlledTextarea
                  id="care-notes"
                  className="input padded"
                  value={careForm.notes}
                  onValueChange={(v) => changeCare("notes", v)}
                  placeholder="e.g. prefers slightly dry soil"
                />
              </div>
            </div> */}
          </div>

          {/* Information */}
          <div className="section" aria-labelledby="info-legend">
            <div className="title" style={{ marginBottom: 8 }}>
              <div>
                <strong id="info-legend">Information</strong>
                <div className="small-muted">Soil type, pot size, from another plant</div>
              </div>
            </div>

            <div className="form-row" style={{ marginTop: 8, alignItems: "center", gap: 12 }}>
              <div style={plantRowItem("60%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="soil-type" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Soil type
                  </label>
                  <ControlledInput
                    id="soil-type"
                    className="input padded"
                    value={infoForm.soilType}
                    onValueChange={(v) => changeInfo("soilType", v)}
                    placeholder="Soil type"
                  />
                </div>
              </div>

              <div style={plantRowItem("25%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="pot-size" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Pot size
                  </label>
                  <ControlledInput
                    id="pot-size"
                    className="input small padded"
                    value={infoForm.potSize}
                    onValueChange={(v) => changeInfo("potSize", v)}
                    placeholder="e.g. 6in"
                  />
                </div>
              </div>

              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <input
                  id="fromAnother"
                  type="checkbox"
                  checked={!!infoForm.fromAnotherPlant}
                  onChange={(e) => changeInfo("fromAnotherPlant", e.target.checked)}
                />
                <label htmlFor="fromAnother" className="small-muted" style={{ margin: 0 }}>From another plant</label>
              </div>
            </div>
          </div>

          {/* Location detail */}
          <div className="section" aria-labelledby="location-legend">
            <div className="title" style={{ marginBottom: 8 }}>
              <div>
                <strong id="location-legend">Location</strong>
                <div className="small-muted">Where the plant lives — light level, notes</div>
              </div>
            </div>

            <div className="form-row" style={{ marginTop: 8, gap: 12 }}>
              <div style={plantRowItem("65%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="location-name" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Location name
                  </label>
                  <ControlledInput
                    id="location-name"
                    className="input padded"
                    value={locForm.locationName}
                    onValueChange={(v) => changeLoc("locationName", v)}
                    placeholder="e.g. South-facing shelf"
                  />
                </div>
              </div>

              <div style={plantRowItem("30%")}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <label htmlFor="light-level" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                    Light level
                  </label>
                  <ControlledInput
                    id="light-level"
                    className="input small padded"
                    value={locForm.lightLevel}
                    onValueChange={(v) => changeLoc("lightLevel", v)}
                  />
                </div>
              </div>
            </div>
{/* 
            <div style={{ marginTop: 10 }}>
              <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                <label htmlFor="location-notes" className="small-muted" style={{ fontSize: 13, marginBottom: 4 }}>
                  Location notes
                </label>
                <ControlledTextarea
                  id="location-notes"
                  className="input padded"
                  value={locForm.notes}
                  onValueChange={(v) => changeLoc("notes", v)}
                  placeholder="Optional notes about the spot"
                />
              </div>
            </div> */}
          </div>

          {error && <div role="alert" style={{ color: "crimson", fontSize: 13 }}>{error}</div>}

          <div style={{ display: "flex", gap: 8, justifyContent: "flex-end", marginTop: 6 }}>
            <button type="button" className="btn ghost" onClick={handleCancel} disabled={saving}>Cancel</button>

            <button
              type="submit"
              className="btn accent"
              disabled={saving || !plantForm.name || plantForm.name.trim() === "" || !plantForm.type || plantForm.type.trim() === ""}
              aria-disabled={saving || !plantForm.name || plantForm.name.trim() === "" || !plantForm.type || plantForm.type.trim() === ""}
            >
              {saving ? "Adding…" : "Add plant"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}