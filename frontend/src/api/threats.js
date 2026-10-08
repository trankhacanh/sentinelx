import client from './client';

export function getThreatStatistics(params = {}) {
  return client.get('/api/threats/statistics', { params });
}

export function listThreats(params = {}) {
  return client.get('/api/threats', { params });
}