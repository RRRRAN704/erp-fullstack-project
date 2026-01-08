<script setup>
import {
  employeePageQueryAPI,
  addEmployeeAPI,
  enableOrDisableAPI,
  editEmployeeAPI,
  getAllDepartmentAPI,
  getAllPositionAPI,
} from "@/apis/employee";
import dayjs from "dayjs";
import { onMounted, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

// Get all departments and positions for dropdowns
const departmentList = ref([]);
const positionList = ref([]);
const filteredPositionList = ref([]); // Filtered positions based on selected department

const getAllDepartments = async () => {
  const res = await getAllDepartmentAPI();
  departmentList.value = res.data;
};

const getAllPositions = async () => {
  const res = await getAllPositionAPI();
  positionList.value = res.data;
};

onMounted(() => {
  getAllDepartments();
  getAllPositions();
});

const getDepartmentName = (departmentId) => {
  const department = departmentList.value.find(
    (dept) => dept.id === departmentId
  );
  return department ? department.departmentName : "-";
};

const getPositionName = (positionId) => {
  const position = positionList.value.find((pos) => pos.id === positionId);
  return position ? position.positionName : "-";
};

// Page query logic
const employeeList = ref([]);
const total = ref(0);
const params = ref({
  page: 1,
  pageSize: 5,
});

const getEmployeeList = async () => {
  const res = await employeePageQueryAPI(params.value);
  employeeList.value = res.data.records;
  total.value = res.data.total;
};

onMounted(() => {
  getEmployeeList();
});

// Page change
const pageChange = (page) => {
  console.log(page);
  params.value.page = page;
  getEmployeeList();
};

// Employee name input
const employeeName = ref(""); // Employee name input
const onSearch = () => {
  params.value.page = 1; // Reset to first page on search
  if (employeeName.value.trim()) {
    params.value.name = employeeName.value.trim();
  } else {
    delete params.value.name; // Remove filter if input is empty
  }
  getEmployeeList();
};

// ============= Dialog  =============
const showDialog = ref(false);
const isEdit = ref(false);
const formRef = ref(null);

// Table form
const form = ref({
  id: null,
  name: "",
  username: "",
  departmentId: null,
  positionId: null,
  phone: "",
  email: "",
  sex: null,
});

// Rules
const rules = {
  name: [
    { required: true, message: "Please enter full name", trigger: "blur" },
  ],
  username: [
    { required: true, message: "Please enter username", trigger: "blur" },
  ],
  departmentId: [
    { required: true, message: "Please select department", trigger: "change" },
  ],
  positionId: [
    { required: true, message: "Please select position", trigger: "change" },
  ],
  phone: [
    { required: true, message: "Please enter phone", trigger: "blur" },
  ],
  email: [
    { required: true, message: "Please enter email", trigger: "blur" },
  ],
  sex: [
    { required: true, message: "Please select gender", trigger: "change" },
  ],
};

// Gender options 
const genderOptions = [
  { label: "Male", value: "1" },
  { label: "Female", value: "2" },
];

// Watch department change to filter positions
watch(() => form.value.departmentId, (newDepartmentId) => {
  
  if (newDepartmentId) {
    filteredPositionList.value = positionList.value.filter(
      position => position.departmentId === newDepartmentId
    );
    console.log("Filtered positions:", filteredPositionList.value);
    
    // Reset position if it's not in the new department
    if (form.value.positionId) {
      const isPositionInDepartment = filteredPositionList.value.some(
        position => position.id === form.value.positionId
      );
      if (!isPositionInDepartment) {
        form.value.positionId = null;
      }
    }
  } else {
    filteredPositionList.value = [];
    form.value.positionId = null;
  }
}, { immediate: true });

// Reset form
const resetForm = () => {
  form.value = {
    id: null,
    name: "",
    username: "",
    departmentId: null,
    positionId: null,
    phone: "",
    email: "",
    sex: null, 
  };
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// Open add Dialog
const openAddDialog = () => {
  isEdit.value = false;
  resetForm();
  showDialog.value = true;
};

// Open edit Dialog
const openEditDialog = (row) => {
  isEdit.value = true;
  form.value = {
    id: row.id,
    name: row.name,
    username: row.username,
    departmentId: row.departmentId,
    positionId: row.positionId,
    phone: row.phone,
    email: row.email,
    sex: row.sex,
  };
  
  // Filter positions based on selected department
  if (row.departmentId) {
    filteredPositionList.value = positionList.value.filter(
      position => position.departmentId === row.departmentId
    );
  }
  
  showDialog.value = true;
};

// Submit form
const submitForm = async () => {
  if (!formRef.value) return;

  try {
    // Validate form
    await formRef.value.validate();

    if (isEdit.value) {
      // Edit employee
      await editEmployeeAPI(form.value);
      ElMessage.success("Edit employee successfully");
    } else {
      // Add employee
      const submitData = {
        name: form.value.name,
        username: form.value.username,
        departmentId: form.value.departmentId,
        positionId: form.value.positionId,
        phone: form.value.phone,
        email: form.value.email,
        sex: form.value.sex,
      };
      await addEmployeeAPI(submitData);
      ElMessage.success("Successfully added employee");
    }

    showDialog.value = false;
    getEmployeeList();
  } catch (error) {
    console.error("Submission error:", error);
    ElMessage.error("Submission failed, please check the form");
  }
};

// Enable/Disable employee
const handleEnableDisable = async (row) => {
  const action = row.status == "1" ? "disable" : "enable";
  const newStatus = row.status == "1" ? "0" : "1";
  
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to ${action} this employee?`,
      "Warning",
      {
        confirmButtonText: "Yes",
        cancelButtonText: "Cancel",
        type: "warning",
      }
    );
    await enableOrDisableAPI(newStatus, row.id);
    ElMessage.success(`${action} employee successfully`);
    getEmployeeList();
  } catch (error) {
    ElMessage({ type: "error", message: `Failed to ${action} employee` });
  }
};
</script>

<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 5px">Employee Name：</label>
        <el-input
          v-model="employeeName"
          placeholder="Please enter name"
          style="width: 15%"
          clearable
        />
        <el-button class="normal-btn continue" @click="onSearch">
          Search
        </el-button>
        <el-button type="primary" style="float: right" @click="openAddDialog">
          + Add Employee
        </el-button>
      </div>

      <el-table :data="employeeList" stripe class="tableBox">
        <el-table-column prop="name" label="Full Name" />
        <el-table-column prop="username" label="Username" />
        <el-table-column label="Position">
          <template #default="{ row }">
            {{ getPositionName(row.positionId) }}
          </template>
        </el-table-column>
        <el-table-column label="Department">
          <template #default="{ row }">
            {{ getDepartmentName(row.departmentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="Phone" />
        <el-table-column label="Gender">
          <template #default="{ row }">
            {{ row.sex === "1" ? "Male" : "Female" }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="Email" />
        <el-table-column label="Last Update Time">
          <template #default="{ row }">
            {{ dayjs(row.updateTime).format("YYYY-MM-DD HH:mm:ss") }}
          </template>
        </el-table-column>
        <el-table-column label="Operation" width="160" align="center">
          <template #default="{ row }">
            <el-button
              type="text"
              size="small"
              class="edit-btn"
              @click="openEditDialog(row)"
            >
              Edit
            </el-button>
            <el-button
              type="text"
              size="small"
              class="delete-btn"
              @click="handleEnableDisable(row)"
            >
              {{ row.status == "1" ? "Disable" : "Enable" }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px">
        <el-pagination
          :total="total"
          @current-change="pageChange"
          :page-size="params.pageSize"
          background
          layout="prev, pager, next"
        />
      </div>

      <!-- Dialog  -->
      <el-dialog
        :title="isEdit ? 'Edit Employee' : 'Add Employee'"
        v-model="showDialog"
        width="30%"
      >
        <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
          <!-- Full Name input -->
          <el-form-item label="Full Name" prop="name">
            <el-input
              v-model="form.name"
              placeholder="Enter full name"
            />
          </el-form-item>

          <!-- Username input -->
          <el-form-item label="Username" prop="username">
            <el-input
              v-model="form.username"
              placeholder="Enter username"
            />
          </el-form-item>

          <!-- Department select -->
          <el-form-item label="Department" prop="departmentId">
            <el-select
              v-model="form.departmentId"
              placeholder="Please select department"
              style="width: 100%"
            >
              <el-option
                v-for="dept in departmentList"
                :key="dept.id"
                :label="dept.departmentName"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>

          <!-- Position select -->
          <el-form-item label="Position" prop="positionId">
            <el-select
              v-model="form.positionId"
              placeholder="Please select position"
              style="width: 100%"
              :disabled="!form.departmentId"
            >
              <el-option
                v-for="pos in filteredPositionList"
                :key="pos.id"
                :label="pos.positionName"
                :value="pos.id"
              />
            </el-select>
          </el-form-item>

          <!-- Phone input -->
          <el-form-item label="Phone" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="Enter phone number"
            />
          </el-form-item>

          <!-- Email input -->
          <el-form-item label="Email" prop="email">
            <el-input
              v-model="form.email"
              placeholder="Enter email"
            />
          </el-form-item>

          <!-- Gender select -->
          <el-form-item label="Gender" prop="sex">
            <el-select
              v-model="form.sex"
              placeholder="Please select gender"
              style="width: 100%"
              value-key="value"
            >
              <el-option
                v-for="option in genderOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showDialog = false">Cancel</el-button>
          <el-button type="primary" @click="submitForm">Confirm</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.edit-btn {
  color: #409eff;
}

.delete-btn {
  color: #f56c6c;
}

.pagination-container {
  display: flex;
  justify-content: center;
}
</style>