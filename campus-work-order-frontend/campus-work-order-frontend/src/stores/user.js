import { defineStore } from 'pinia'

const authKeys = ['token', 'userId', 'username', 'realName', 'role', 'avatarUrl', 'phone']

function readAuth(key) {
  return sessionStorage.getItem(key) || ''
}

function clearAuth(storage) {
  authKeys.forEach(key => storage.removeItem(key))
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: readAuth('token'),
    userId: readAuth('userId'),
    username: readAuth('username'),
    realName: readAuth('realName'),
    role: readAuth('role'),

    // ✅ 新增
    avatarUrl: readAuth('avatarUrl'),
    phone: readAuth('phone')
  }),

  actions: {
    setUser(data) {
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      this.role = data.role

      // ✅ 新增
      this.avatarUrl = data.avatarUrl || ''
      this.phone = data.phone || ''

      clearAuth(localStorage)
      clearAuth(sessionStorage)
      const storage = sessionStorage

      storage.setItem('token', data.token)
      storage.setItem('userId', data.userId)
      storage.setItem('username', data.username)
      storage.setItem('realName', data.realName)
      storage.setItem('role', data.role)

      // ✅ 新增
      storage.setItem('avatarUrl', data.avatarUrl || '')
      storage.setItem('phone', data.phone || '')
    },

    setAvatarUrl(url) {
      this.avatarUrl = url || ''
      sessionStorage.setItem('avatarUrl', this.avatarUrl)
    },

    logout() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.realName = ''
      this.role = ''

      // ✅ 新增
      this.avatarUrl = ''
      this.phone = ''

      clearAuth(localStorage)
      clearAuth(sessionStorage)
    }
  }
})
