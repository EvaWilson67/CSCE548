import React from "react";

export default function Header() {
  return (
    <div className="header">
      <div>
        <h1>🌿 Plant Tracker</h1>
        <div className="sub small">Client UI — calls the Plant service for CRUD & details</div>
      </div>
      <div style={{textAlign:"right"}}>
        <div className="small">API: <strong style={{color:"#0f172a"}}>{import.meta.env.VITE_API_BASE}</strong></div>
        <div className="small">Mode: <em>demo</em></div>
      </div>
    </div>
  );
}