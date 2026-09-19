const TOKEN_KEY = 'smart_exam_token'
const USER_KEY = 'smart_exam_user'

export const getToken = () => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token) => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = () => {
  localStorage.removeItem(TOKEN_KEY)
}

export const getUser = () => {
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const getUserRole = () => {
  const user = getUser()
  return user ? user.role : null
}

export const setUser = (user) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const isAdmin = () => {
  const user = getUser()
  return user && user.role === 0
}

export const isStudent = () => {
  const user = getUser()
  return user && user.role === 1
}

export const isSuperAdmin = () => {
  const user = getUser()
  return user && user.role === 2
}
