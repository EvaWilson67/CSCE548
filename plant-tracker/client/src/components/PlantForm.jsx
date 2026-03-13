import React from "react";

/**
 * Reusable plant form used for create & edit.
 * props:
 *  - value: { name, type, height, notes, location }
 *  - onChange(field, value)
 *  - onSubmit()
 *  - onCancel()
 *  - submitting: bool
 */
export default function PlantForm({ value = {}, onChange, onSubmit, onCancel, submitting = false }) {
  const change = (field) => (e) => onChange(field, e.target.value);
  return (
    <form onSubmit={(e)=>{ e.preventDefault(); onSubmit(); }}>
      <div className="form-row">
        <input className="input" placeholder="Name" value={value.name||""} onChange={change("name")} />
        <input className="input" placeholder="Type" value={value.type||""} onChange={change("type")} />
        <input className="input small" placeholder="Height" value={value.height||""} onChange={change("height")} />
      </div>

      <div style={{marginTop:8}}>
        <input className="input" placeholder="Location (optional)" value={value.location||""} onChange={change("location")} />
      </div>

      <div style={{marginTop:8}}>
        <textarea className="input" placeholder="Notes (optional)" value={value.notes||""} onChange={change("notes")} style={{width:"100%", minHeight:80}} />
      </div>

      <div className="actions">
        <button className="btn accent" type="submit" disabled={submitting}>{submitting ? "Saving…" : "Save"}</button>
        <button type="button" className="btn ghost" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}