import request from './request'

export function login(data) {
  return request.post('/api/auth/login', data)
}

export function getCaptcha() {
  return request.get('/api/auth/captcha')
}

export function logout() {
  return request.post('/api/auth/logout')
}

export function register(data) {
  return request.post('/api/auth/register', data)
}

export function getMe() {
  return request.get('/api/auth/me')
}
