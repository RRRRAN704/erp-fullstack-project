<script setup>
import { useRoute } from "vue-router";

// Menu entries for the current portal, given by the layout: { path, icon, label }
defineProps({
  items: { type: Array, required: true },
});

const route = useRoute();
</script>

<template>
  <div class="sidebar">
    <div class="logo-container">
      <img src="@/assets/logo.png" alt="Logo" class="logo" />
    </div>
    <ul class="nav-list">
      <li
        v-for="item in items"
        :key="item.path"
        class="nav-item"
        :class="{ active: route.path === item.path }"
        @click="$router.push(item.path)"
      >
        <i :class="['iconfont', item.icon]"></i>
        <span>{{ item.label }}</span>
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