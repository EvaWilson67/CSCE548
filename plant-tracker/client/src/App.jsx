import React, { useState } from "react";
import Header from "./components/Header";
import PlantList from "./components/PlantList";
import PlantDetail from "./components/PlantDetail";

export default function App() {
  const [selectedId, setSelectedId] = useState(null);

  return (
    <div className="app-shell">
      <Header />
      <div className="layout">
        <PlantList onSelect={(id) => setSelectedId(id)} />
        <div>
          <PlantDetail plantId={selectedId} onBack={() => setSelectedId(null)} />
        </div>
      </div>
    </div>
  );
}