<script setup>

import Topbar from '@/views/LayoutAdmin/components/TopBar.vue'
import Sidebar from '@/views/LayoutAdmin/components/SideBar.vue'

</script>

<template>
  <div class="layout">
    <Sidebar />
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
