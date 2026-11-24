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
            <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
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
  align-items: center;
  min-height: 100vh;
  padding: 16px;
  background-color: #333;
}

/* Desktop: photo on the left, form on the right */
.login-box {
  width: 60%;
  max-width: 960px;
  min-height: 60vh;
  border-radius: 8px;
  display: flex;
  overflow: hidden;

  img {
    width: 60%;
    object-fit: cover;
  }
}

.login-form {
  background: #ffffff;
  width: 40%;
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .brandName {
    text-align: center;
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 24px;
    color: #002b5b;
    font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  }

  .form {
    width: 100%;

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

/* Phones: one card across the screen, photo as a banner on top */
@media (max-width: 767px) {
  .login-box {
    width: 100%;
    max-width: 400px;
    min-height: 0;
    flex-direction: column;

    /* photo becomes a banner above the form, cropped rather than squashed */
    img {
      width: 100%;
      height: 150px;
      object-fit: cover;
    }
  }

  .login-form {
    width: 100%;
    padding: 32px 24px;
  }
}
</style>
