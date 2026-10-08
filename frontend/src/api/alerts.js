import client from './client';

export function listAlerts(params = {}) {
  return client.get('/api/alerts', { params });
}

export function getAlert(id) {
  return client.get(`/api/alerts/${id}`);
}

export function updateAlertStatus(id, status) {
  return client.put(`/api/alerts/${id}/status`, { status });
}

export function assignAlert(id, assignedTo) {
  return client.put(`/api/alerts/${id}/assign`, { assignedTo });
}