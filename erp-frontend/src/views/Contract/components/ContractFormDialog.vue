<script setup>
import { computed, nextTick, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { addContractAPI, editContractAPI, uploadAPI } from "@/apis/contract";
import { defaultAssignee } from "@/utils/access";

// Add/Edit contract dialog. contract = the row to edit, or null to add a new one.
const visible = defineModel({ type: Boolean, required: true });
const props = defineProps({
  contract: { type: Object, default: null },
  employees: { type: Array, required: true }, // all employees (to look up the department)
  assignableEmployees: { type: Array, required: true }, // the ones offered in the dropdown
  clients: { type: Array, required: true }, // the clients that may be linked
  getDepartmentName: { type: Function, required: true },
});
const emit = defineEmits(["saved"]);

const isEdit = computed(() => props.contract !== null);
const formRef = ref(null);

// Status options for dropdown
const statusOptions = [
  { label: "Active", value: 0 },
  { label: "Archived", value: 1 },
];

const emptyForm = () => ({
  id: null,
  contractName: "",
  contractNumber: "",
  clientId: null,
  employeeId: defaultAssignee(),
  departmentId: null,
  amount: 0,
  startDate: "",
  endDate: "",
  fileUrl: "",
  isArchived: 0,
});

const formFromRow = (row) => ({
  id: row.id,
  contractName: row.contractName,
  contractNumber: row.contractNumber,
  clientId: row.clientId,
  employeeId: row.employeeId,
  departmentId: row.departmentId,
  amount: row.amount,
  startDate: row.startDate,
  endDate: row.endDate,
  fileUrl: row.fileUrl || "",
  isArchived: row.isArchived || 0,
});

const formData = ref(emptyForm());

// Fill the form each time the dialog opens
watch(visible, async (open) => {
  if (!open) return;
  formData.value = isEdit.value ? formFromRow(props.contract) : emptyForm();
  await nextTick();
  formRef.value?.clearValidate();
});

// Form validation rules
const rules = {
  contractName: [
    { required: true, message: "Please enter contract name", trigger: "blur" },
  ],
  contractNumber: [
    { required: true, message: "Please enter contract number", trigger: "blur" },
  ],
  employeeId: [
    { required: true, message: "Please select an employee", trigger: "change" },
  ],
  clientId: [
    { required: true, message: "Please select a client", trigger: "change" },
  ],
  amount: [
    { required: true, message: "Please enter amount", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (!Number.isInteger(Number(value)) || Number(value) < 0) {
          callback(new Error("Amount must be a positive integer"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  startDate: [
    { required: true, message: "Please select start date", trigger: "change" },
  ],
  endDate: [
    { required: true, message: "Please select end date", trigger: "change" },
  ],
  isArchived: [
    { required: true, message: "Please select status", trigger: "change" },
  ],
};

// Auto-fill department when employee is selected
watch(
  () => formData.value.employeeId,
  (employeeId) => {
    const employee = props.employees.find((emp) => emp.id === employeeId);
    if (employee) {
      formData.value.departmentId = employee.departmentId;
    }
  }
);

//upload file
const handleFileUpload = async (file) => {
  try {
    const res = await uploadAPI(file);
    ElMessage.success("File uploaded successfully");
    formData.value.fileUrl = res.data;
    return res.data;
  } catch (error) {
    console.error("Upload failed:", error);
  }
};

// Handle file change
const handleFileChange = (file) => {
  // make sure the file exist
  if (file && file.raw) {
    handleFileUpload(file.raw);
  } else if (file) {
    handleFileUpload(file);
  }
  return false;
};

// Submit form
const submitForm = async () => {
  try {
    await formRef.value.validate();
    formData.value.amount = parseInt(formData.value.amount);

    if (isEdit.value) {
      await editContractAPI(formData.value);
      ElMessage.success("Contract updated successfully");
    } else {
      await addContractAPI(formData.value);
      ElMessage.success("Contract added successfully");
    }

    visible.value = false;
    emit("saved");
  } catch (error) {
    if (error === false) {
      // Form validation failed
      ElMessage.error("Please check the form data");
    } else {
      ElMessage.error(
        isEdit.value ? "Failed to update contract" : "Failed to add contract"
      );
    }
  }
};
</script>

<template>
  <el-dialog
    :title="isEdit ? 'Edit Contract' : 'Add Contract'"
    v-model="visible"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="140px"
      label-position="left"
    >
      <el-form-item label="Contract Name" prop="contractName">
        <el-input
          v-model="formData.contractName"
          placeholder="Please enter contract name"
          clearable
        />
      </el-form-item>

      <el-form-item label="Contract Number" prop="contractNumber">
        <el-input
          v-model="formData.contractNumber"
          placeholder="Please enter contract number"
          clearable
        />
      </el-form-item>

      <el-form-item label="Employee" prop="employeeId">
        <el-select
          v-model="formData.employeeId"
          placeholder="Please select an employee"
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="employee in assignableEmployees"
            :key="employee.id"
            :label="employee.name"
            :value="employee.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="Client" prop="clientId">
        <el-select
          v-model="formData.clientId"
          placeholder="Please select a client"
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="client in clients"
            :key="client.id"
            :label="client.companyName"
            :value="client.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="Department">
        <el-input
          :value="getDepartmentName(formData.departmentId)"
          placeholder="Auto-filled based on selected employee"
          disabled
        />
      </el-form-item>

      <el-form-item label="Amount (£)" prop="amount">
        <el-input
          v-model.number="formData.amount"
          type="number"
          placeholder="Please enter amount"
          clearable
        />
      </el-form-item>

      <el-form-item label="Start Date" prop="startDate">
        <el-date-picker
          v-model="formData.startDate"
          type="date"
          placeholder="Please select start date"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="End Date" prop="endDate">
        <el-date-picker
          v-model="formData.endDate"
          type="date"
          placeholder="Please select end date"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="Status" prop="isArchived">
        <el-select
          v-model="formData.isArchived"
          placeholder="Please select status"
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="File Upload">
        <el-upload
          :before-upload="handleFileChange"
          :on-change="handleFileChange"
          :on-select="handleFileChange"
          :show-file-list="true"
          :limit="1"
          accept=".pdf,.doc,.docx,.jpg,.png"
          :auto-upload="false"
          @change="handleFileChange"
        >
          <el-button type="primary">Choose File</el-button>
          <template #tip>
            <div class="el-upload__tip">
              Only pdf/doc/docx/jpg/png files are allowed
            </div>
          </template>
        </el-upload>

        <div v-if="formData.fileUrl" style="margin-top: 10px; color: #67c23a">
          ✓ Uploaded: {{ formData.fileUrl }}
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">Cancel</el-button>
        <el-button type="primary" @click="submitForm">
          {{ isEdit ? "Update" : "Add" }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
