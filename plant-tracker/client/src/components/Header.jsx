import React from "react";

export default function Header() {
  return (
    <div className="header">
      <div>
        <h1 style={{ margin: 0 }}>🌿 Plant Manager (Full CRUD)</h1>
        <div className="small">Plants + Care / Information / Location</div>
      </div>

      <div style={{ textAlign: "right" }}>
        <div className="small">API: <strong style={{ color: "#0b3b13" }}>{import.meta.env.VITE_API_BASE || "http://localhost:8080"}</strong></div>
      </div>
    </div>
  );
}