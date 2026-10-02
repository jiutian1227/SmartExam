<template>
  <div class="app-layout">
    <Sidebar :collapsed="sidebarCollapsed" />
    <div class="main-area" :class="{ collapsed: sidebarCollapsed }">
      <Header :collapsed="sidebarCollapsed" @toggleCollapse="sidebarCollapsed = !sidebarCollapsed" @openAnnouncement="showAnnouncementModal = true" />
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
      <AnnouncementModal v-model:visible="showAnnouncementModal" />
    </div>
  </div>
</template>

<script setup>
import { ref, provide, onMounted } from 'vue'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import AnnouncementModal from './AnnouncementModal.vue'
import { getToken, getUser, setUser } from '../../utils/auth'
import { getCurrentUser } from '../../api/auth'

const sidebarCollapsed = ref(false)
const showAnnouncementModal = ref(false)

// 以JWT为唯一身份来源：进入主框架时校验token，并用服务端返回的身份刷新本地缓存
onMounted(async () => {
  if (!getToken()) return
  try {
    const res = await getCurrentUser()
    if (res.code === 200 && res.data) {
      setUser({ ...(getUser() || {}), ...res.data })
    }
  } catch (error) {
    // token失效导致的401/403已由request拦截器统一处理并跳转登录页
  }
})

provide('showAnnouncement', () => {
  showAnnouncementModal.value = true
})
</script>

<style scoped>
.app-layout {
  display: flex;
  width: 100%;
  min-height: 100vh;
  background: var(--color-bg-base);
}

.main-area {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left var(--transition-base);
}

.main-area.collapsed {
  margin-left: 68px;
}

.main-content {
  flex: 1;
  padding: var(--space-6);
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--color-bg-base);
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .main-area {
    margin-left: 0;
  }
  .main-area.collapsed {
    margin-left: 0;
  }
}
</style>
