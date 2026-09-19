<template>
  <div class="home">
    <SuperAdminDashboard v-if="isSuperAdmin()" />
    <TeacherDashboard v-else-if="isAdmin()" />
    <StudentDashboard v-else />
  </div>
</template>

<script setup>
import { isAdmin, isSuperAdmin } from '../utils/auth'
import SuperAdminDashboard from '../components/Dashboard/SuperAdminDashboard.vue'
import TeacherDashboard from '../components/Dashboard/TeacherDashboard.vue'
import StudentDashboard from '../components/Dashboard/StudentDashboard.vue'
</script>

<style scoped>
.home {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  padding-bottom: var(--space-6);
}

</style>
