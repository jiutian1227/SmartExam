import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken, getUser, setUser, removeUser } from '../utils/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const user = ref(getUser())

  const setUserInfo = (userInfo) => {
    user.value = userInfo
  }

  const login = (tokenValue, userInfo) => {
    token.value = tokenValue
    user.value = userInfo
    setToken(tokenValue)
    setUser(userInfo)
  }

  const logout = () => {
    token.value = null
    user.value = null
    removeToken()
    removeUser()
  }

  const isAdmin = () => {
    return user.value && user.value.role === 0
  }

  const isStudent = () => {
    return user.value && user.value.role === 1
  }

  const isSuperAdmin = () => {
    return user.value && user.value.role === 2
  }

  return {
    token,
    user,
    setUserInfo,
    login,
    logout,
    isAdmin,
    isStudent,
    isSuperAdmin
  }
})
