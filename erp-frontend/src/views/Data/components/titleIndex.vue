<template>

  <div class="title-index">

    <div class="month">
   
      <ul class="tabs">
        <li
          class="li-tab"
          v-for="(item, index) in tabsParam"
          @click="toggleTabs(index)"
          :class="{ active: index === nowIndex }"
          :key="index"
        >
 
          {{ item }}
   
          <span class="tab-underline"></span>
        </li>
      </ul>
    </div>
    
 
    <div class="get-time">
      <p>
     <!-- Display the first and last elements of the time array as the time range -->
        Selected Time: {{ tateData[0] }} to {{ tateData[tateData.length - 1] }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  // Flag used to synchronise the currently selected tab index
  flag: {
    type: [String, Number], // Supports string or numeric types
    default: 2  //// Change the default value to 2, corresponding to ‘Recent 7 Days’
  },
 // Time data array used to display the selected time range
  tateData: {
    type: Array,
    default: () => [] 
  },
})


const emit = defineEmits(['sendTitleInd'])


const value = ref([])


const tabsParam = ref(['Yesterday', 'Recent 7 Days', 'Recent 30 Days', 'This Week', 'This Month'])


const nowIndex = computed(() => {

  const flagValue = Number(props.flag)
  return flagValue > 0 ? flagValue - 1 : 1 // Default is index 1 (Recent 7 Days)
})


const toggleTabs = (index) => {
  // Clear the value of the time selector
  value.value = []
  
  // Fire an event to the parent component, passing the tab index + 1 (because the business logic starts counting from 1).
  emit('sendTitleInd', index + 1)
}
</script>

<style scoped>
.title-index {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20px;
}

.month {
  flex: 1;
}

.tabs {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  gap: 0;
  background-color: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.li-tab {
  position: relative;
  flex: 1;
  text-align: center;
  padding: 12px 16px;
  cursor: pointer;
  background-color: #fff;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-right: 1px solid #e5e7eb;
  user-select: none;
}

.li-tab:last-child {
  border-right: none;
}

.li-tab:hover {
  background-color: #f3f4f6;
  color: #374151;
}

.li-tab.active {
  background-color: #3b82f6;
  color: #fff;
  font-weight: 600;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
}

.tab-underline {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background-color: #3b82f6;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.li-tab.active .tab-underline {
  width: 80%;
}

.li-tab:not(.active):hover .tab-underline {
  width: 60%;
  background-color: #6b7280;
}

.get-time {
  background-color: #fff;
  padding: 12px 16px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  white-space: nowrap;
  min-width: fit-content;
}

.get-time p {
  margin: 0;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .title-index {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .tabs {
    flex-direction: column;
  }
  
  .li-tab {
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }
  
  .li-tab:last-child {
    border-bottom: none;
  }
  
  .title-index {
    padding: 16px;
  }
  
  .li-tab {
    padding: 14px 16px;
    font-size: 15px;
  }
  
  .get-time {
    white-space: normal;
  }
}

/* animation effects */
@keyframes tabSelect {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-2px);
  }
  100% {
    transform: translateY(-1px);
  }
}

.li-tab.active {
  animation: tabSelect 0.3s ease;
}

.li-tab:focus {
  outline: 2px solid #3b82f6;
  outline-offset: -2px;
}
</style>