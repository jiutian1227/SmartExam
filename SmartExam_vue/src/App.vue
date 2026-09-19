<template>
  <router-view />
  <!-- AI助手悬浮窗（登录注册页面不显示） -->
  <AiAssistantFloat v-if="!isLoginOrRegister" />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AiAssistantFloat from './components/AiAssistantFloat.vue'

const route = useRoute()

const isLoginOrRegister = computed(() => {
  return ['/login', '/register'].includes(route.path) || route.path.startsWith('/student/exam-detail')
})
</script>

<style>
body {
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #e0e7ff 0%, #dbeafe 50%, #e8f0fe 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'PingFang SC', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  min-height: 100vh;
}
</style>
