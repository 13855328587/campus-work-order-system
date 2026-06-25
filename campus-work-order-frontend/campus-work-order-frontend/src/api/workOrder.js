import request from './request'

export function createWorkOrder(data, idempotencyKey) {
  return request.post('/api/work-orders', data, {
    headers: {
      'Idempotency-Key': idempotencyKey
    }
  })
}

export function getMyOrders(params) {
  return request.get('/api/work-orders/my', { params })
}

export function getAllOrders() {
  return request.get('/api/work-orders')
}

export function getWorkOrderDetail(id) {
  return request.get(`/api/work-orders/${id}`)
}

export function approveWorkOrder(id) {
  return request.post(`/api/work-orders/${id}/approve`)
}

export function batchApproveWorkOrder(ids) {
  return request.post('/api/work-orders/batch/approve', { ids })
}

export function rejectWorkOrder(id, data) {
  return request.post(`/api/work-orders/${id}/reject`, data)
}

export function assignWorkOrder(id, data) {
  return request.post(`/api/work-orders/${id}/assign`, data)
}

export function acceptWorkOrder(id) {
  return request.post(`/api/work-orders/${id}/accept`)
}

export function batchAcceptWorkOrder(ids) {
  return request.post('/api/work-orders/batch/accept', { ids })
}

export function workerRejectWorkOrder(id, data) {
  return request.post(`/api/work-orders/${id}/worker/reject`, data)
}

export function finishWorkOrder(id, data) {
  return request.post(`/api/work-orders/${id}/finish`, data)
}

export function cancelWorkOrder(id) {
  return request.post(`/api/work-orders/${id}/cancel`)
}

export function batchCancelWorkOrder(ids) {
  return request.post('/api/work-orders/batch/cancel', { ids })
}

export function getStatistics() {
  return request.get('/api/work-orders/statistics')
}

export function getWorkerTasks(params) {
  return request.get('/api/work-orders/worker/tasks', { params })
}

export function getWorkerHistory(params) {
  return request.get('/api/work-orders/worker/history', { params })
}

export function getOrderPage(params) {
  return request.get('/api/work-orders/page', { params })
}
