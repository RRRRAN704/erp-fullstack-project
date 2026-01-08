<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import "element-plus/theme-chalk/el-message.css";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

//form check
const form = ref({
  email: "",
  verifyCode: "",
  newPassword: "",
  confirmPassword: "",
});

// 1. define vadidation functions
const validatePass = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("New Password cannot be empty"));
  } else {
    // Regular Expression：8-20, 8-20 chars, contain letters & numbers
    // (?=.*[A-Za-z]) At least one letter
    // (?=.*\d)       At least one digit
    // .{8,20}        between 8 to 20 characters
    const regex = /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/;
    
    if (!regex.test(value)) {
      callback(new Error("Password must be 8-20 chars, contain letters & numbers"));
    } else {
      //if confirm password already has value, trigger its validation to check if they match
      if (form.value.confirmPassword !== "") {
        if (formRef.value) formRef.value.validateField("confirmPassword");
      }
      callback(); // validation passed
    }
  }
};

//2. confirm password validation
const validatePass2 = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("Confirm Password cannot be empty"));
  } else if (value !== form.value.newPassword) {
    // check if passwords match
    callback(new Error("Two passwords do not match!"));
  } else {
    callback(); // validation passed
  }
};

// 2. define rules object
const rules = ref({
  email: [
    { required: true, message: "Email cannot be empty", trigger: "blur" },
    { type: 'email', message: "Please input correct email address", trigger: ['blur', 'change'] }
  ],
  verifyCode: [
    { required: true, message: "Verify Code cannot be empty", trigger: "blur" },
  ],
  newPassword: [
    { required: true, validator: validatePass, trigger: "blur" }, 
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: "blur" },
  ],
});

//get form instance for validation
const formRef = ref(null);
const router = useRouter();

const sendCode = () => {
  console.log(form.value);
  //form rules check
  const { email } = form.value;
  formRef.value.validateField('email',async (valid) => {
    console.log("validate result", valid);
    if (valid) {
      const res = await userStore.sendCode({ email });
      if (res.code === 1) {
        ElMessage.success("Code sent successfully, please check your email!");
      } else {
        ElMessage.error(
          res.msg || "Failed to send code, please try again later."
        );
      }
    } else {
      ElMessage.error(
        "Please fill in the email correctly before sending the code."
      );
    }
  });
};


const resetPassword = () => {
  // console.log(form.value);
  // validate form
  formRef.value.validate(async (valid) => {
    if (valid) {
      
      // Destructure assignment
      const { email, verifyCode, newPassword, confirmPassword } = form.value;
      
      const res = await userStore.resetPassword({
        email,
        verifyCode,
        newPassword,
        confirmPassword
      });
      
      if (res.code === 1) {
        ElMessage.success("Password reset successfully");
        router.push("/login");
      } else {
        ElMessage.error(res.msg || "Failed to reset password");
      }
    } else {

      console.log('error submit!!');
      return false;
    }
  });
};
</script>



<template>
  <div class="login">
    <div class="login-box">
      <img src="@/assets/login/password.png" alt="" />
      <div class="login-form">
        <div class="reset-text">Password Reset</div>

        <div class="account-box">
          <div class="form">
            <el-form ref="formRef" :model="form" :rules="rules">
              <el-form-item prop="email">
                <el-input placeholder="Email" type="email" v-model="form.email">
                  <template #prefix>
                    <i class="iconfont icon-user"></i>
                  </template>

                  <template #append>
                    <el-button
                      type="primary"
                      size="small"
                      class="send-btn"
                      @click="sendCode"
                      >Send</el-button
                    >
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="verifyCode">
                <el-input
                  placeholder="Verification Code"
                  type="number"
                  style="margin-top: 20px"
                  v-model="form.verifyCode"
                >
                  <template #prefix>
                    <i class="iconfont icon-code"></i>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="newPassword">
                <el-input
                  placeholder="New Password"
                  type="password"
                  show-password
                  style="margin-top: 20px"
                  v-model="form.newPassword"
                >
                  <template #prefix>
                    <i class="iconfont icon-password"></i>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <el-input
                  type="password"
                  placeholder="Confirm Password"
                  show-password
                  style="margin-top: 20px"
                  v-model="form.confirmPassword"
                >
                  <template #prefix>
                    <i class="iconfont icon-password"></i>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item>
                <el-button class="reset-btn" type="primary" @click="resetPassword">
                  Reset Password
                </el-button>
              </el-form-item>

                <el-form-item>
                <el-button class="login-btn" type="primary" @click="$router.push('/login')">
                 Return to Login
                </el-button>
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
  width: 80%;
  height: auto;
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

  .reset-text {
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

      .send-btn {
        background-color: #002b5b;
        color: #ffffff;
      }

      .reset-btn {
        width: 100%;
        background-color: #f0f0f0;
        color: #333;
      }

      .login-btn {
        width: 100%;
        background-color: #f0f0f0;
        color: #333;
      }
    }
  }
}
</style>
