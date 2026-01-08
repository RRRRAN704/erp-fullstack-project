<script setup>
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { addOrderAPI } from "@/apis/order";
import { useRouter } from "vue-router";

const router = useRouter();
//import icon
import {
  OfficeBuilding,
  User,
  Phone,
  Message,
  Check,
  RefreshRight,
  Clock,
  Key,
} from "@element-plus/icons-vue";

// Form data
const formData = ref({
  companyName: "",
  contactPerson: "",
  contactPhone: "",
  contactEmail: "",
  remark: "",
});

// Form reference
const formRef = ref();

const isSubmitting = ref(false);

// verfication code 
const captchaCode = ref(""); // 
const captchaInput = ref(""); // user input
const captchaCanvas = ref(null); // canvas element

// generate verification code
const generateCaptcha = () => {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
  let code = "";
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  captchaCode.value = code;
  drawCaptcha(code);
};

// draw captcha on canvas
const drawCaptcha = (code) => {
  const canvas = captchaCanvas.value;
  if (!canvas) return;
  
  const ctx = canvas.getContext("2d");
  const width = canvas.width;
  const height = canvas.height;

  // clear canvas
  ctx.clearRect(0, 0, width, height);

  // draw background
  ctx.fillStyle = "#f0f0f0";
  ctx.fillRect(0, 0, width, height);

  // draw interference lines
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = getRandomColor(100, 200);
    ctx.beginPath();
    ctx.moveTo(Math.random() * width, Math.random() * height);
    ctx.lineTo(Math.random() * width, Math.random() * height);
    ctx.stroke();
  }

  // draw interference dots
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = getRandomColor(0, 255);
    ctx.beginPath();
    ctx.arc(
      Math.random() * width,
      Math.random() * height,
      1,
      0,
      2 * Math.PI
    );
    ctx.fill();
  }

  // draw text
  ctx.font = "bold 28px Arial";
  ctx.textBaseline = "middle";
  
  for (let i = 0; i < code.length; i++) {
    ctx.fillStyle = getRandomColor(50, 160);
    ctx.save();
    ctx.translate(20 + i * 25, height / 2);
    ctx.rotate(((Math.random() - 0.5) * Math.PI) / 6);
    ctx.fillText(code[i], 0, 0);
    ctx.restore();
  }
};

// generate random color
const getRandomColor = (min, max) => {
  const r = Math.floor(Math.random() * (max - min) + min);
  const g = Math.floor(Math.random() * (max - min) + min);
  const b = Math.floor(Math.random() * (max - min) + min);
  return `rgb(${r},${g},${b})`;
};

// refresh captcha
const refreshCaptcha = () => {
  captchaInput.value = "";
  generateCaptcha();
};

// validate captcha
const validateCaptcha = () => {
  if (!captchaInput.value) {
    ElMessage.warning("Please enter the code");
    return false;
  }
  if (captchaInput.value.toLowerCase() !== captchaCode.value.toLowerCase()) {
    ElMessage.error("Incorrect code, please try again");
    refreshCaptcha();
    return false;
  }
  return true;
};

// Generate captcha on component mount
onMounted(() => {
  generateCaptcha();
});


// Form validation rules
const rules = ref({
  companyName: [
    { required: true, message: "Please enter company name", trigger: "blur" },
  ],
  contactPerson: [
    {
      required: true,
      message: "Please enter contact person name",
      trigger: "blur",
    },
  ],
  contactPhone: [
    { required: true, message: "Please enter phone number", trigger: "blur" },
  ],
  contactEmail: [
    { required: true, message: "Please enter email address", trigger: "blur" },
    {
      type: "email",
      message: "Please enter a valid email address",
      trigger: "blur",
    },
  ],
  remark: [
    {
      required: true,
      message: "Please describe your consultation needs",
      trigger: "blur",
    },
  ],
});

// Submit form
const submitForm = async () => {
  if (isSubmitting.value) return; // prevent multiple submissions

  // validate captcha
  if (!validateCaptcha()) return;

  try {
    await formRef.value.validate();
    isSubmitting.value = true;
    await addOrderAPI(formData.value);
    console.log("Form data submitted:", formData.value);
    ElMessage.success("Form submitted successfully!");
    resetForm();
    setTimeout(() => {
      router.push("/success");
    }, 1500);
  } catch (error) {
    console.error("Form submission error:", error);

    if (error.name === "ValidationError") {
      ElMessage.error("Please check and complete the form information");
    } else {
      ElMessage.error("Submission failed, please try again later");
    }
    // reset captcha when submission fails
    refreshCaptcha();
  } finally {
    isSubmitting.value = false;
  }
};

// Reset form
const resetForm = () => {
  formData.value = {
    companyName: "",
    contactPerson: "",
    contactPhone: "",
    contactEmail: "",
    remark: "",
  };

  // clear validation
  if (formRef.value) {
    formRef.value.clearValidate();
  }
  
  // refresh captcha
  refreshCaptcha();
};
</script>

<template>
  <div class="consultation-form">
    <el-card class="form-card" shadow="hover">
      <!-- Header section -->
      <div class="form-header">
        <el-icon class="header-icon" size="48" color="#409EFF">
          <svg viewBox="0 0 1024 1024">
            <path
              d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"
              fill="currentColor"
            />
            <path
              d="M623.6 316.7C593.6 290.4 554 276 512 276s-81.6 14.4-111.6 40.7C369.2 344 352 380.7 352 420v7.6c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8V420c0-44.1 43.1-80 96-80s96 35.9 96 80c0 31.1-22 59.6-56.1 72.7-21.2 8.1-39.2 22.3-52.1 40.9-13.1 19-19.8 41.3-19.8 64.9V620c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8v-22.7a48.3 48.3 0 0 1 30.9-44.8c59-22.7 97.1-74.7 97.1-132.5 0-39.3-17.2-76-48.4-103.3z"
              fill="currentColor"
            />
            <path
              d="M512 732a40 40 0 1 0 0 80 40 40 0 0 0 0-80z"
              fill="currentColor"
            />
          </svg>
        </el-icon>
        <h1 class="form-title">Request Consultation</h1>
        <p class="form-subtitle">
          Fill out the form below and our professional team will get back to you
          shortly
        </p>
      </div>

      <!-- Form content -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="140px"
        size="large"
        class="consultation-form-content"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Company Name" prop="companyName">
              <el-input
                v-model="formData.companyName"
                placeholder="Enter your company name"
                :prefix-icon="OfficeBuilding"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Contact Person" prop="contactPerson">
              <el-input
                v-model="formData.contactPerson"
                placeholder="Enter contact person name"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Phone Number" prop="contactPhone">
              <el-input
                v-model="formData.contactPhone"
                placeholder="Enter phone number"
                :prefix-icon="Phone"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Email Address" prop="contactEmail">
              <el-input
                v-model="formData.contactEmail"
                placeholder="Enter email address"
                :prefix-icon="Message"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Consultation Request" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="6"
            placeholder="Please describe your consultation needs in detail. We will provide you with professional solutions..."
            show-word-limit
            maxlength="255"
          />
        </el-form-item>

        <!-- captcha area-->
        <el-form-item label="Verification Code" required>
          <div class="captcha-wrapper">
            <el-input
              v-model="captchaInput"
              placeholder="Enter verification code"
              :prefix-icon="Key"
              clearable
              class="captcha-input"
              @keyup.enter="submitForm"
            />
            <div class="captcha-box">
              <canvas
                ref="captchaCanvas"
                width="120"
                height="40"
                class="captcha-canvas"
                @click="refreshCaptcha"
                title="Click to refresh"
              ></canvas>
              <el-button
                type="info"
                link
                @click="refreshCaptcha"
                class="refresh-btn"
              >
                <el-icon><RefreshRight /></el-icon>
                Refresh
              </el-button>
            </div>
          </div>
        </el-form-item>

        <!-- Action buttons -->
        <el-form-item class="form-actions">
          <el-button
            type="primary"
            size="large"
            @click="submitForm"
            :icon="Check"
            :loading="isSubmitting"
          >
            Submit Request
          </el-button>
          <el-button size="large" @click="resetForm" :icon="RefreshRight">
            Reset Form
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Contact information card -->
    <el-card class="contact-card" shadow="hover">
      <div class="contact-info">
        <h3>Alternative Contact Methods</h3>
        <div class="contact-items">
          <div class="contact-item">
            <el-icon color="#409EFF"><Phone /></el-icon>
            <span>+44 123-4567-910</span>
          </div>
          <div class="contact-item">
            <el-icon color="#409EFF"><Message /></el-icon>
            <span>contact@company.com</span>
          </div>
          <div class="contact-item">
            <el-icon color="#409EFF"><Clock /></el-icon>
            <span>Business Hours: Mon-Fri 9:00 AM - 6:00 PM</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.consultation-form {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
}

.form-card {
  margin-bottom: 24px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 20px 0;
}

.header-icon {
  margin-bottom: 16px;
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.form-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 0;
  line-height: 1.5;
}

.consultation-form-content {
  padding: 0 20px;
}

.form-actions {
  text-align: center;
  margin-top: 32px;
}

.form-actions .el-button {
  min-width: 140px;
  margin: 0 12px;
}

.contact-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.contact-info h3 {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
}

.contact-items {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

/* captcha styles */
.captcha-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.captcha-input {
  flex: 1;
  max-width: 200px;
}

.captcha-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.captcha-canvas {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.captcha-canvas:hover {
  border-color: #409eff;
}

.refresh-btn {
  font-size: 12px;
  color: #409eff;
}

.refresh-btn:hover {
  color: #66b1ff;
}

/* Responsive design */
@media (max-width: 768px) {
  .consultation-form {
    margin: 20px auto;
    padding: 12px;
  }

  .consultation-form-content {
    padding: 0 12px;
  }

  .contact-items {
    flex-direction: column;
    align-items: center;
  }

  .form-actions .el-button {
    width: 100%;
    margin: 8px 0;
  }

  .captcha-wrapper {
    flex-wrap: wrap;
  }

  .captcha-input {
    max-width: 100%;
    width: 100%;
  }

  .captcha-box {
    margin-top: 8px;
  }
}

/* Custom style optimizations */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: box-shadow 0.2s ease;
}

:deep(.el-input__wrapper):hover {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%);
  border: none;
}

:deep(.el-button--primary):hover {
  background: linear-gradient(135deg, #66b1ff 0%, #40a9ff 100%);
}
</style>