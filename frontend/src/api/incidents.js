import client from './client';

export function listIncidents(params = {}) {
  return client.get('/api/incidents', { params });
}

export function getIncident(id) {
  return client.get(`/api/incidents/${id}`);
}

export function createIncident(payload) {
  return client.post('/api/incidents', payload);
}

export function updateIncidentStatus(id, status) {
  return client.put(`/api/incidents/${id}/status`, { status });
}

export function assignIncident(id, assignedTo) {
  return client.put(`/api/incidents/${id}/assign`, { assignedTo });
}

export function addIncidentNote(id, content) {
  return client.post(`/api/incidents/${id}/notes`, { content });
}