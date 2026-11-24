<script setup>
import { computed, nextTick, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { addClientAPI, editClientAPI } from "@/apis/client";
import { defaultAssignee } from "@/utils/access";

// Add/Edit client dialog. client = the row to edit, or null to add a new one.
const visible = defineModel({ type: Boolean, required: true });
const props = defineProps({
  client: { type: Object, default: null },
  employees: { type: Array, required: true }, // all employees (to look up the department)
  assignableEmployees: { type: Array, required: true }, // the ones offered in the dropdown
  getDepartmentName: { type: Function, required: true },
});
const emit = defineEmits(["saved"]);

const isEdit = computed(() => props.client !== null);
const formRef = ref(null);

// Status options for dropdown
const statusOptions = [
  { label: "Active", value: 0 },
  { label: "Archived", value: 1 },
];

const emptyForm = () => ({
  id: null,
  companyName: "",
  contactPerson: "",
  contactPhone: "",
  contactEmail: "",
  employeeId: defaultAssignee(),
  departmentId: null,
  isArchived: 0, // Default to active
  address: "",
  remark: "",
});

const formFromRow = (row) => ({
  id: row.id,
  companyName: row.companyName,
  contactPerson: row.contactPerson,
  contactPhone: row.contactPhone,
  contactEmail: row.contactEmail,
  employeeId: row.employeeId,
  departmentId: row.departmentId,
  isArchived: row.isArchived || 0,
  address: row.address,
  remark: row.remark || "",
});

const formData = ref(emptyForm());

// Fill the form each time the dialog opens
watch(visible, async (open) => {
  if (!open) return;
  formData.value = isEdit.value ? formFromRow(props.client) : emptyForm();
  await nextTick();
  formRef.value?.clearValidate();
});

//rules
const rules = {
  companyName: [
    { required: true, message: "Please enter company name", trigger: "blur" },
  ],
  contactPerson: [
    { required: true, message: "Please enter contact person", trigger: "blur" },
  ],
  contactPhone: [
    { required: true, message: "Please enter contact phone", trigger: "blur" },
  ],
  contactEmail: [
    { required: true, message: "Please enter contact email", trigger: "blur" },
  ],
  employeeId: [
    { required: true, message: "Please select an employee", trigger: "change" },
  ],
  departmentId: [
    { required: true, message: "Please select a department", trigger: "change" },
  ],
  address: [
    { required: true, message: "Please enter address", trigger: "blur" },
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

// Submit form
const submitForm = async () => {
  try {
    await formRef.value.validate();

    if (isEdit.value) {
      await editClientAPI(formData.value);
      ElMessage.success("Client updated successfully");
    } else {
      await addClientAPI(formData.value);
      ElMessage.success("Client added successfully");
    }

    visible.value = false;
    emit("saved");
  } catch (error) {
    if (error === false) {
      // Form validation failed
      ElMessage.error("Please check the form data");
    } else {
      ElMessage.error(
        isEdit.value ? "Failed to update client" : "Failed to add client"
      );
    }
  }
};
</script>

<template>
  <el-dialog
    :title="isEdit ? 'Edit Client' : 'Add Client'"
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
      <el-form-item label="Company Name" prop="companyName">
        <el-input
          v-model="formData.companyName"
          placeholder="Please enter company name"
          clearable
        />
      </el-form-item>

      <el-form-item label="Contact Person" prop="contactPerson">
        <el-input
          v-model="formData.contactPerson"
          placeholder="Please enter contact person"
          clearable
        />
      </el-form-item>

      <el-form-item label="Contact Phone" prop="contactPhone">
        <el-input
          v-model="formData.contactPhone"
          placeholder="Please enter contact phone"
          clearable
        />
      </el-form-item>

      <el-form-item label="Contact Email" prop="contactEmail">
        <el-input
          v-model="formData.contactEmail"
          placeholder="Please enter contact email"
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

      <el-form-item label="Department">
        <el-input
          :value="getDepartmentName(formData.departmentId)"
          placeholder="Auto-filled based on selected employee"
          disabled
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

      <el-form-item label="Address" prop="address">
        <el-input
          v-model="formData.address"
          type="textarea"
          :rows="3"
          placeholder="Please enter address"
        />
      </el-form-item>

      <el-form-item label="Remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="Please enter remark (optional)"
        />
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
