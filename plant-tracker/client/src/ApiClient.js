const BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

// helper to fetch and parse JSON (throws on non-OK)
async function fetchJson(path) {
  const res = await fetch(BASE + path, { credentials: "include" });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status} ${res.statusText}: ${text}`);
  }
  return res.json();
}

// Plants
export function getAllPlants() { return fetchJson("/api/plants"); }
export function getPlant(id) { return fetchJson(`/api/plants/${id}`); }

// Care, Information, Location (per-plant)
export function getCare(plantId) { return fetchJson(`/api/plants/${plantId}/care`); }
export function getInformation(plantId) { return fetchJson(`/api/plants/${plantId}/information`); }
export function getLocation(plantId) { return fetchJson(`/api/plants/${plantId}/location`); }

// If you have any subset endpoints, add here, for example:
// export function getPlantsByType(type) { return fetchJson(`/api/plants?type=${encodeURIComponent(type)}`); }