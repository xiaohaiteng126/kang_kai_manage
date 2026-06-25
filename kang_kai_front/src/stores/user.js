import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const isKang = computed(() => userInfo.value.username === 'kang')

  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      realName: res.data.realName
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.clear()
    router.push('/login')
  }

  async function refreshUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = {
        userId: res.data.userId,
        username: res.data.username,
        realName: res.data.realName
      }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } catch (e) {
      // ignore
    }
  }

  return { token, userInfo, isLoggedIn, isKang, login, logout, refreshUserInfo }
})
