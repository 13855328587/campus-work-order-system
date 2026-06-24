import request from './request'

export function getUsers(params) {
  return request.get('/api/users/page', { params })
}

export function updateUserStatus(id, status) {
  return request.put(`/api/users/${id}/status?status=${status}`)
}

export function updateUserRole(id, role) {
  return request.put(`/api/users/${id}/role?role=${role}`)
}
export function getWorkers() {
  return request.get('/api/users/workers')
}
