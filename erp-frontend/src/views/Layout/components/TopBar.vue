<script setup>
import { useUserStore } from "@/stores/user";
import { useRouter } from "vue-router";

defineProps({
  showMenuButton: { type: Boolean, default: false }, // phones: ☰ opens the menu drawer
});
const emit = defineEmits(["toggle-menu"]);

const userStore = useUserStore();
const router = useRouter();
const confirm = () => {
  console.log("User logout");
  // 1.clear user info
  userStore.clearUserInfo();
  // 2.push to login page
  router.push("/login");
};
</script>


<template>
  <div class="topbar">
    <button
      v-if="showMenuButton"
      class="menu-btn"
      aria-label="Open menu"
      @click="emit('toggle-menu')"
    >
      ☰
    </button>
    <div class="status">
      <i class="iconfont icon-user"></i>
      <div class="text">{{ userStore.userInfo.name }}</div>
    </div>
    <div class="logout-btn">
      <el-popconfirm
        @confirm="confirm"
        title="Confirm Logout?"
        confirm-button-text="Confirm"
        cancel-button-text="Cancel"
        width="200px"
      >
        <template #reference>
          <a href="javascript:;">Logout</a>
        </template>
      </el-popconfirm>
    </div>
  </div>
</template>


<style scoped>
.topbar {
  height: 88px;
  width: 100%;
  background-color: #90c2f0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
}


.status {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 50px;
}

.text{
 margin-left: 20px;
color: white;
}

.logout-btn {
  background-color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  margin-left: 50px;
}
.logout-btn:hover {
  background-color: #f0f0f0;
}

.menu-btn {
  margin-right: auto; /* pushes the user name and logout to the right */
  background: none;
  border: none;
  color: white;
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
  padding: 4px 8px;
}

@media (max-width: 767px) {
  .topbar {
    height: 56px;
    padding: 0 12px;
  }

  .status {
    width: auto;
    margin-right: 16px;
  }

  .logout-btn {
    margin-left: 0;
  }
}
</style>
