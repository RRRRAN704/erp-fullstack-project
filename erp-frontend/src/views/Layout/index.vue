<script setup>
import Sidebar from '@/views/Layout/components/SideBar.vue'
import Topbar from '@/views/Layout/components/TopBar.vue'
import AiAssistant from '@/views/Layout/components/AiAssistant.vue'
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useIsMobile } from '@/composables/useIsMobile'

// One layout for both portals; the router says which one (props: { portal: 'admin' | 'employee' }).
defineProps({
  portal: { type: String, required: true },
})

// Phones: the sidebar becomes a drawer opened from the top bar, closed again after navigating.
const isMobile = useIsMobile()
const menuOpen = ref(false)
const route = useRoute()
watch(() => route.path, () => (menuOpen.value = false))
watch(isMobile, (mobile) => { if (!mobile) menuOpen.value = false })

const MENUS = {
  admin: [
    { path: '/admin/employee', icon: 'icon-user', label: 'Employee Management' },
    { path: '/admin/department', icon: 'icon-department', label: 'Department Management' },
    { path: '/admin/position', icon: 'icon-file', label: 'Position Management' },
  ],
  employee: [
    { path: '/employee/dashboard', icon: 'icon-code', label: 'Dashboard' },
    { path: '/employee/contract', icon: 'icon-file', label: 'Contract Management' },
    { path: '/employee/customer', icon: 'icon-customer-center', label: 'Customer Management' },
    { path: '/employee/chart', icon: 'icon-chart-area', label: 'Charts' },
  ],
}
</script>

<template>
  <div class="layout">
    <Sidebar v-if="!isMobile" :items="MENUS[portal]" />
    <el-drawer
      v-else
      v-model="menuOpen"
      direction="ltr"
      size="200px"
      :with-header="false"
      class="menu-drawer"
    >
      <Sidebar :items="MENUS[portal]" />
    </el-drawer>
    <div class="content">
      <Topbar :show-menu-button="isMobile" @toggle-menu="menuOpen = !menuOpen" />
      <div class="main-body">
        <RouterView />
      </div>
    </div>
    <!-- the AI assistant answers business questions, so only in the employee portal -->
    <AiAssistant v-if="portal === 'employee'" />
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.content {
  flex: 1;
  min-width: 0; /* let wide tables scroll inside instead of stretching the page */
  display: flex;
  flex-direction: column;
}

.main-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
@media (max-width: 767px) {
  .main-body {
    padding: 12px;
  }
}
</style>

<style>
/* Not scoped: el-drawer is teleported to <body>, outside this component's scoped styles. */
.menu-drawer .el-drawer__body {
  padding: 0;
}
</style>
