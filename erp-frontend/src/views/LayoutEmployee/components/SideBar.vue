<script setup>
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();
</script>

<template>
  <div class="sidebar">
    <div class="logo-container">
      <img src="@/assets/logo.png" alt="Logo" class="logo" />
    </div>
    <ul class="nav-list">
      <li 
        class="nav-item" 
        :class="{ active: route.path === '/employee/dashboard' }"
        @click="$router.push('/employee/dashboard')"
      >
        <i class="iconfont icon-code"></i>
        <span>Dashboard</span>
      </li>
      <li 
        class="nav-item" 
        :class="{ active: route.path === '/employee/contract' }"
        @click="$router.push('/employee/contract')"
      >
        <i class="iconfont icon-file"></i>
        <span>Contract Management</span>
      </li>
      <li 
        class="nav-item" 
        :class="{ active: route.path === '/employee/customer' }"
        @click="$router.push('/employee/customer')"
      >
        <i class="iconfont icon-customer-center"></i>
        <span>Customer Management</span>
      </li>
      <li 
        class="nav-item" 
        :class="{ active: route.path === '/employee/chart' }"
        @click="$router.push('/employee/chart')"
      >
        <i class="iconfont icon-chart-area"></i>
        <span>Charts</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.sidebar {
  width: 200px;
  height: 100vh;
  background-color: #609fdc;
  color: white;
  display: flex;
  flex-direction: column;
  padding-top: 20px;
  flex-shrink: 0; 
  box-sizing: border-box;
  overflow-y: auto; 
}

.logo-container {
  text-align: center; 
}

.logo {
  width: 120px;
  height: auto;
  display: inline-block;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.nav-item:hover {
  background-color: #4c8ec5;
}

.nav-item.active {
  background-color: #4c8ec5;
}

.iconfont {
  margin-right: 10px;
}
</style>