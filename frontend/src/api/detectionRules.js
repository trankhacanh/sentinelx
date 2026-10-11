import client from './client';

export function listDetectionRules() {
  return client.get('/api/detection-rules');
}

export function setRuleEnabled(id, enabled) {
  return client.put(`/api/detection-rules/${id}/enabled`, { enabled });
}