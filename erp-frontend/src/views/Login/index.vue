<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import "element-plus/theme-chalk/el-message.css";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

//form check
const form = ref({
  username: "",
  password: "",
});

//rules object
const rules = ref({
  username: [
    { required: true, message: "Username cannot be empty", trigger: "blur" },
  ],
  password: [
    { required: true, message: "Password cannot be empty", trigger: "blur" },
  ],
});

const formRef = ref(null);
const router = useRouter();
const doLogin = () => {
  console.log(form.value);
  //form validation
  const { username, password } = form.value;
  formRef.value.validate(async (valid) => {
    if (valid) {
      const res = await userStore.getUserInfo({ username, password });

      if (res.msg === "Account not found") {
        ElMessage({ type: "error", message: "Account not found" });
        return;
      }
      if (res.msg === "Wrong password") {
        ElMessage({ type: "error", message: "Wrong password" });
        return;
      }
      if (res.msg === "Account locked") {
        ElMessage({ type: "error", message: "Account locked" });
        return;
      }

      ElMessage({ type: "success", message: "login success" });

      if (userStore.userInfo.position === "System Admin") {
        router.replace({ path: "/admin/employee" });
      } else {
        router.replace({ path: "/employee/dashboard" });
      }
    }
  });
};
</script>

<template>
  <div class="login">

    <div class="login-box">
      <img src="@/assets/login/Login.jpg" alt="" />
      <div class="login-form">
        <div class="brandName">FlowBit ERP</div>
        <div class="account-box">
          <div class="form">
            <el-form ref="formRef" :model="form" :rules="rules">
              <el-form-item label="Username">
                <el-input v-model="form.username" />
              </el-form-item>
              <el-form-item label="Password">
                <el-input type="password" v-model="form.password" />
              </el-form-item>
              <el-form-item>
                <el-button class="subBtn" @click="doLogin">Sign in</el-button>
              </el-form-item>
              <el-form-item>
                <el-button class="rstBtn" @click="$router.push('/reset')"
                  >Reset</el-button
                >
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>



<style scoped lang="scss">
.login {
  display: flex;
  justify-content: center;
  margin: 0;
  padding: 0;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background-color: #333;
}

.login-box {
  width: 60%;
  height: 60%;
  border-radius: 8px;
  display: flex;
  overflow: hidden;

  img {
    width: 60%;
    height: auto;
  }
}

.login-form {
  background: #ffffff;
  width: 40%;
  padding: 40px 30px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;

  .brandName {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #002b5b;
    font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  }

  .account-box {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .form {
      width: 100%;
      height: 80%;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .el-form-item {
        margin-bottom: 20px;
      }

      .subBtn {
        width: 100%;
        background-color: #002b5b;
        color: #ffffff;
      }

      .rstBtn {
        width: 100%;
        background-color: #f0f0f0;
        color: #333;
      }
    }
  }
}
</style>
