// ApiClient.js - centralized fetch wrappers with guard for undefined ids
const BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

function assertPathSafe(path) {
  // quick guard to avoid accidental /undefined requests
  if (path.includes("undefined") || path.match(/\/\s*$/)) {
    throw new Error(`Invalid API path detected: "${path}". This usually means a missing id was passed.`);
  }
}

async function request(path, opts = {}) {
  assertPathSafe(path);

  const res = await fetch(BASE + path, { credentials: "include", ...opts });
  if (res.status === 204) return null;
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status} ${res.statusText} - ${text}`);
  }
  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) return res.json();
  return null;
}

function jsonOpts(method, body) {
  return {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  };
}

// Plants
export const getAllPlants = () => request("/api/plants");
export const getPlant = (id) => {
  if (!id && id !== 0) throw new Error("getPlant called with empty id");
  return request(`/api/plants/${id}`);
};
export const createPlant = (plant) => request("/api/plants", jsonOpts("POST", plant));
export const updatePlant = (id, plant) => {
  if (!id && id !== 0) throw new Error("updatePlant called with empty id");
  return request(`/api/plants/${id}`, jsonOpts("PUT", plant));
};
export const deletePlant = (id) => {
  if (!id && id !== 0) throw new Error("deletePlant called with empty id");
  return request(`/api/plants/${id}`, { method: "DELETE" });
};

// Care (per-plant)
export const getCare = (plantId) => {
  if (!plantId && plantId !== 0) throw new Error("getCare called with empty plantId");
  return request(`/api/plants/${plantId}/care`);
};
export const createCare = (plantId, care) => {
  if (!plantId && plantId !== 0) throw new Error("createCare called with empty plantId");
  return request(`/api/plants/${plantId}/care`, jsonOpts("POST", care));
};
export const updateCare = (plantId, care) => {
  if (!plantId && plantId !== 0) throw new Error("updateCare called with empty plantId");
  return request(`/api/plants/${plantId}/care`, jsonOpts("PUT", care));
};
export const deleteCare = (plantId) => {
  if (!plantId && plantId !== 0) throw new Error("deleteCare called with empty plantId");
  return request(`/api/plants/${plantId}/care`, { method: "DELETE" });
};

// Information
export const getInformation = (plantId) => {
  if (!plantId && plantId !== 0) throw new Error("getInformation called with empty plantId");
  return request(`/api/plants/${plantId}/information`);
};
export const createInformation = (plantId, info) => {
  if (!plantId && plantId !== 0) throw new Error("createInformation called with empty plantId");
  return request(`/api/plants/${plantId}/information`, jsonOpts("POST", info));
};
export const updateInformation = (plantId, info) => {
  if (!plantId && plantId !== 0) throw new Error("updateInformation called with empty plantId");
  return request(`/api/plants/${plantId}/information`, jsonOpts("PUT", info));
};
export const deleteInformation = (plantId) => {
  if (!plantId && plantId !== 0) throw new Error("deleteInformation called with empty plantId");
  return request(`/api/plants/${plantId}/information`, { method: "DELETE" });
};

// Location
export const getLocation = (plantId) => {
  if (!plantId && plantId !== 0) throw new Error("getLocation called with empty plantId");
  return request(`/api/plants/${plantId}/location`);
};
export const createLocation = (plantId, loc) => {
  if (!plantId && plantId !== 0) throw new Error("createLocation called with empty plantId");
  return request(`/api/plants/${plantId}/location`, jsonOpts("POST", loc));
};
export const updateLocation = (plantId, loc) => {
  if (!plantId && plantId !== 0) throw new Error("updateLocation called with empty plantId");
  return request(`/api/plants/${plantId}/location`, jsonOpts("PUT", loc));
};
export const deleteLocation = (plantId) => {
  if (!plantId && plantId !== 0) throw new Error("deleteLocation called with empty plantId");
  return request(`/api/plants/${plantId}/location`, { method: "DELETE" });
};