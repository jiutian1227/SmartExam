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
import { ref, provide } from 'vue'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import AnnouncementModal from './AnnouncementModal.vue'

const sidebarCollapsed = ref(false)
const showAnnouncementModal = ref(false)

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
