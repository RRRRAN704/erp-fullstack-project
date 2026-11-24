<script setup>
import Sidebar from '@/views/Layout/components/SideBar.vue'
import Topbar from '@/views/Layout/components/TopBar.vue'

// One layout for both portals; the router says which one (props: { portal: 'admin' | 'employee' }).
defineProps({
  portal: { type: String, required: true },
})

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
    <Sidebar :items="MENUS[portal]" />
    <div class="content">
      <Topbar />
      <div class="main-body">
        <RouterView />
      </div>
    </div>
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
  display: flex;
  flex-direction: column;
}

.main-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
