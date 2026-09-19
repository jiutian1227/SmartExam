/**
 * Format a date/time value to a localized string
 * @param {string|number|Date} time - The date/time value to format
 * @param {string} fallback - Fallback string when time is falsy
 * @returns {string}
 */
export const formatDateTime = (time, fallback = '-') => {
  if (!time) return fallback
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * Format a date/time value to date only (YYYY-MM-DD)
 * @param {string|number|Date} time
 * @param {string} fallback
 * @returns {string}
 */
export const formatDate = (time, fallback = '-') => {
  if (!time) return fallback
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * Format to short date (MM-DD)
 * @param {string|number|Date} time
 * @param {string} fallback
 * @returns {string}
 */
export const formatDateShort = (time, fallback = '-') => {
  if (!time) return fallback
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

/**
 * Format to relative time (e.g. "3分钟前")
 * @param {string|number|Date} time
 * @returns {string}
 */
export const formatRelativeTime = (time) => {
  if (!time) return '-'
  const now = Date.now()
  const diff = now - new Date(time).getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return formatDate(time)
}
