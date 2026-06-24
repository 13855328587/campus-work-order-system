import request from './request'

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function uploadWorkOrderImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post('/api/files/work-order-images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
