// src/App.jsx
import React from "react";
import PlantList from "./components/PlantList";
import "./index.css";

export default function App() {
  return (
    <div className="app-shell">
      <div className="header">
        <h2>My Plant Manager</h2>
        <div className="small-muted">Plants • Care • Info • Locations</div>
      </div>

      <PlantList />
    </div>
  );
}