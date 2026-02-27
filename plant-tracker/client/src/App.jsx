import React, { useEffect, useState } from 'react'
import { api } from './api'

export default function App() {
  const [plants, setPlants] = useState([])
  const [loadingPlants, setLoadingPlants] = useState(false)
  const [selectedPlantId, setSelectedPlantId] = useState(null)
  const [selectedPlant, setSelectedPlant] = useState(null)
  const [care, setCare] = useState(null)
  const [information, setInformation] = useState(null)
  const [location, setLocation] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    loadPlants()
  }, [])

  async function loadPlants() {
    setLoadingPlants(true)
    setError(null)
    try {
      const all = await api.getAllPlants()
      setPlants(all)
    } catch (e) {
      setError('Failed to load plants: ' + (e.message || e))
    } finally {
      setLoadingPlants(false)
    }
  }

  // When user selects a plant: call all the single-record get endpoints
  async function selectPlant(id) {
    setSelectedPlantId(id)
    setSelectedPlant(null)
    setCare(null)
    setInformation(null)
    setLocation(null)
    setError(null)

    try {
      // 1) GET single plant
      const p = await api.getPlant(id)
      setSelectedPlant(p)

      // 2) GET care for plant (single record)
      const c = await api.getCare(id)
      setCare(c)

      // 3) GET information for plant (single record)
      const info = await api.getInformation(id)
      setInformation(info)

      // 4) GET location for plant (single record)
      const loc = await api.getLocation(id)
      setLocation(loc)
    } catch (e) {
      setError('Failed to fetch plant details: ' + (e.message || e))
    }
  }

  return (
    <div className="app">
      <header>
        <h1>Plant Tracker — Client</h1>
        <p className="muted">API base: <code>{api.base}</code></p>
      </header>

      <main>
        <section className="panel">
          <h2>All plants</h2>
          {loadingPlants ? <p>Loading...</p> : null}
          {error ? <div className="error">{error}</div> : null}
          <div className="list">
            {plants.length === 0 && !loadingPlants ? <p>No plants found.</p> : null}
            {plants.map(p => (
              <div key={p.plantId || p.id} className="list-item">
                <div>
                  <strong>{p.name || p.commonName || 'Unnamed'}</strong>
                  <div className="muted small">id: {p.plantId || p.id}</div>
                </div>
                <div>
                  <button onClick={() => selectPlant(p.plantId || p.id)}>
                    View
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="panel">
          <h2>Selected plant</h2>
          {selectedPlantId == null ? <p>Select a plant to view details</p> : null}

          {selectedPlant && (
            <div className="details">
              <h3>{selectedPlant.name || selectedPlant.commonName || 'Plant'}</h3>
              <pre>{JSON.stringify(selectedPlant, null, 2)}</pre>
            </div>
          )}

          <div>
            <h4>Care</h4>
            {care ? <pre>{JSON.stringify(care, null, 2)}</pre> : <p>No care record (or not fetched).</p>}
          </div>

          <div>
            <h4>Information</h4>
            {information ? <pre>{JSON.stringify(information, null, 2)}</pre> : <p>No information record (or not fetched).</p>}
          </div>

          <div>
            <h4>Location</h4>
            {location ? <pre>{JSON.stringify(location, null, 2)}</pre> : <p>No location record (or not fetched).</p>}
          </div>
        </section>
      </main>

      <footer>
        <small className="muted">Client for demo — calls all GET endpoints (list & single) for Plants, and related care/information/location records.</small>
      </footer>
    </div>
  )
}