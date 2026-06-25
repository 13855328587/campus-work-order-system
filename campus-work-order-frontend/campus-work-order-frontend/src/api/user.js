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

export function updateUser(id, data) {
  return request.put(`/api/users/${id}`, data)
}

export function updateProfile(data) {
  return request.put('/api/profile', data)
}

export function changePassword(data) {
  return request.put('/api/profile/password', data)
}

export function resetUserPassword(id) {
  return request.put(`/api/users/${id}/reset-password`)
}

export function batchResetUserPassword(ids) {
  return request.put('/api/users/batch/reset-password', { ids })
}

export function deleteUser(id) {
  return request.delete(`/api/users/${id}`)
}

export function batchDeleteUser(ids) {
  return request.delete('/api/users/batch/delete', { data: { ids } })
}

export function getWorkers() {
  return request.get('/api/users/workers')
}
