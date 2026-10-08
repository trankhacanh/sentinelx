import client from './client';

export function listEvents(params = {}) {
  return client.get('/api/events', { params });
}