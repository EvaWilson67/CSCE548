const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')

function _fetchJson(path, opts = {}) {
  const url = API_BASE + path
  return fetch(url, opts).then(async res => {
    if (!res.ok) {
      const text = await res.text().catch(() => '')
      const err = new Error(`${res.status} ${res.statusText} ${text}`)
      err.status = res.status
      throw err
    }
    // may be empty on 204
    const ct = res.headers.get('content-type') || ''
    if (ct.includes('application/json')) return res.json()
    return null
  })
}

export const api = {
  base: API_BASE,

  // Plants
  getAllPlants() { return _fetchJson('/plants') },
  getPlant(id) { return _fetchJson(`/plants/${id}`) },

  // Care (single per plant in your backend)
  getCare(plantId) { return _fetchJson(`/plants/${plantId}/care`) },

  // Information
  getInformation(plantId) { return _fetchJson(`/plants/${plantId}/information`) },

  // Location
  getLocation(plantId) { return _fetchJson(`/plants/${plantId}/location`) }
}