<script setup>
import {
  clientPageQueryAPI,
  archivedClientPageQueryAPI,
  addClientAPI,
  editClientAPI,
  archiveClientAPI,
  restoreClientAPI,
  getAllEmployeeAPI,
  getAllPositionAPI,
  getAllDepartmentAPI,
} from "@/apis/client";

import dayjs from "dayjs";
import { onMounted, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
const activeName = ref("first");

//get all employee
const employeeList = ref([]);
const getAllEmployee = async () => {
  const res = await getAllEmployeeAPI();
  employeeList.value = res.data;
};

//get all department
const departmentList = ref([]);
const getAllDepartment = async () => {
  const res = await getAllDepartmentAPI();
  departmentList.value = res.data;
};
onMounted(() => {
  getAllEmployee(), getAllDepartment();
});

const getEmployeeName = (employeeId) => {
  const employee = employeeList.value.find((emp) => emp.id === employeeId);
  return employee ? employee.name : "-";
};

const getDepartmentName = (departmentId) => {
  const department = departmentList.value.find(
    (dept) => dept.id === departmentId
  );
  return department ? department.departmentName : "-";
};

//get unarchived client page query

const activeClientList = ref([]);
const total_1 = ref(0);
const params_1 = ref({
  page: 1,
  pageSize: 5,
});
const getActiveClientList = async () => {
  const res = await clientPageQueryAPI(params_1.value);
  activeClientList.value = res.data.records;
  total_1.value = res.data.total;
};
onMounted(() => {
  getActiveClientList();
});

//page change for unarchived client
const pageChange_1 = (page) => {
  params_1.value.page = page;
  getActiveClientList();
};

//Company name input
const companyName = ref("");
const onSearch = () => {
  params_1.value.page = 1; // Reset to first page on search
  if (companyName.value.trim()) {
    params_1.value.companyName = companyName.value.trim();
  } else {
    delete params_1.value.companyName; // Remove filter if input is empty
  }
  getActiveClientList();
};

//get archived client page query

const archivedClientList = ref([]);
const total_2 = ref(0);
const params_2 = ref({
  page: 1,
  pageSize: 5,
});
const getArchivedClientList = async () => {
  const res = await archivedClientPageQueryAPI(params_2.value);
  archivedClientList.value = res.data.records;
  total_2.value = res.data.total;
};
onMounted(() => {
  getArchivedClientList();
});

//page change for unarchived client
const pageChange_2 = (page) => {
  params_2.value.page = page;
  getArchivedClientList();
};

//archive client
const archiveClient = async (clientId) => {
  try {
    await archiveClientAPI(clientId);
    ElMessage.success("Client archived successfully");
    getActiveClientList();
    getArchivedClientList();
  } catch (error) {
    ElMessage.error("Failed to archive client");
  }
};

//restore client
const restoreClient = async (clientId) => {
  try {
    await restoreClientAPI(clientId);
    ElMessage.success("Client restored successfully");
    getActiveClientList();
    getArchivedClientList();
  } catch (error) {
    ElMessage.error("Failed to restore client");
  }
};

//add and edit client logic
// ============= Dialog  =============
const showDialog = ref(false);
const isEdit = ref(false);
const formRef = ref(null);

// Status options for dropdown
const statusOptions = ref([
  { label: "Active", value: 0 },
  { label: "Archived", value: 1 },
]);

const formData = ref({
  id: null,
  companyName: "",
  contactPerson: "",
  contactPhone: "",
  contactEmail: "",
  employeeId: null,
  departmentId: null,
  isArchived: 0, // Default to active (0)
  address: "",
  remark: "",
});

//rules
const rules = ref({
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
    {
      required: true,
      message: "Please select a department",
      trigger: "change",
    },
  ],
  address: [
    { required: true, message: "Please enter address", trigger: "blur" },
  ],
  isArchived: [
    { required: true, message: "Please select status", trigger: "change" },
  ],
});

// Reset form data
const resetFormData = () => {
  formData.value = {
    id: null,
    companyName: "",
    contactPerson: "",
    contactPhone: "",
    contactEmail: "",
    employeeId: null,
    departmentId: null,
    isArchived: 0, // Default to active
    address: "",
    remark: "",
  };
};

// Open add dialog
const openAddDialog = () => {
  isEdit.value = false;
  resetFormData();
  showDialog.value = true;
  // Reset form validation
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// Open edit dialog
const openEditDialog = (row) => {
  isEdit.value = true;
  formData.value = {
    id: row.id,
    companyName: row.companyName,
    contactPerson: row.contactPerson,
    contactPhone: row.contactPhone,
    contactEmail: row.contactEmail,
    employeeId: row.employeeId,
    departmentId: row.departmentId,
    isArchived: row.isArchived || 0, // Use existing value or default to active
    address: row.address,
    remark: row.remark || "",
  };
  showDialog.value = true;
  // Reset form validation
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// Auto-fill department when employee is selected
const onEmployeeChange = (employeeId) => {
  const selectedEmployee = employeeList.value.find(
    (emp) => emp.id === employeeId
  );
  if (selectedEmployee) {
    formData.value.departmentId = selectedEmployee.departmentId;
  }
};

// Watch for employee change to auto-fill department
watch(
  () => formData.value.employeeId,
  (newEmployeeId) => {
    if (newEmployeeId) {
      onEmployeeChange(newEmployeeId);
    }
  }
);

// Submit form
const submitForm = async () => {
  try {
    // Validate form
    await formRef.value.validate();

    if (isEdit.value) {
      // Edit client
      await editClientAPI(formData.value);
      ElMessage.success("Client updated successfully");
    } else {
      // Add new client
      await addClientAPI(formData.value);
      ElMessage.success("Client added successfully");
    }

    // Close dialog and refresh data
    showDialog.value = false;
    getActiveClientList();
    getArchivedClientList();
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

// Cancel form
const cancelForm = () => {
  showDialog.value = false;
  resetFormData();
};
</script>


<template>
  <div class="dashboard-container">
    <div class="container">
      <el-tabs
        v-model="activeName"
        type="card"
        class="demo-tabs"
        @tab-click="handleClick"
      >
        <el-tab-pane label="Customers" name="first">
          <div class="tableBar">
            <label style="margin-right: 5px">Company Name：</label>
            <el-input
              placeholder="Please enter name"
              style="width: 15%"
              clearable
              v-model="companyName"
            />
            <el-button class="normal-btn continue" @click="onSearch"
              >Search</el-button
            >
            <el-button
              type="primary"
              style="float: right"
              @click="openAddDialog"
            >
              + Add Client
            </el-button>
          </div>

          <el-table :data="activeClientList" stripe class="tableBox">
            <el-table-column prop="companyName" label="Company Name" />
            <el-table-column prop="contactPerson" label="Contact Person" />
            <el-table-column prop="contactPhone" label="Contact Phone" />
            <el-table-column prop="contactEmail" label="Contact Email" />
            <el-table-column label="Employee">
              <template #default="{ row }">
                {{ getEmployeeName(row.employeeId) }}
              </template>
            </el-table-column>
            <el-table-column label="Department">
              <template #default="{ row }">
                {{ getDepartmentName(row.departmentId) }}
              </template>
            </el-table-column>
            <el-table-column prop="address" label="Address" />
            <el-table-column prop="name" label="remark" />
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
                  @click="archiveClient(row.id)"
                >
                  Archive
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container-1" style="margin-top: 20px">
            <el-pagination
              :total="total_1"
              @current-change="pageChange_1"
              :page-size="params_1.pageSize"
              background
              layout="prev, pager, next"
            />
          </div>
        </el-tab-pane>
        <el-tab-pane label="Archived" name="second">
          <el-table :data="archivedClientList" stripe class="tableBox">
            <el-table-column prop="companyName" label="Company Name" />
            <el-table-column prop="contactPerson" label="Contact Person" />
            <el-table-column prop="contactPhone" label="Contact Phone" />
            <el-table-column prop="contactEmail" label="Contact Email" />
            <el-table-column label="Employee">
              <template #default="{ row }">
                {{ getEmployeeName(row.employeeId) }}
              </template>
            </el-table-column>
            <el-table-column label="Department">
              <template #default="{ row }">
                {{ getDepartmentName(row.departmentId) }}
              </template>
            </el-table-column>
            <el-table-column prop="address" label="Address" />
            <el-table-column prop="name" label="remark" />
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
                  class="delete-btn"
                  @click="restoreClient(row.id)"
                >
                  Unarchive
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container-2" style="margin-top: 20px">
            <el-pagination
              :total="total_2"
              @current-change="pageChange_2"
              :page-size="params_2.pageSize"
              background
              layout="prev, pager, next"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      :title="isEdit ? 'Edit Client' : 'Add Client'"
      v-model="showDialog"
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
              v-for="employee in employeeList"
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
          <el-button @click="cancelForm">Cancel</el-button>
          <el-button type="primary" @click="submitForm">
            {{ isEdit ? "Update" : "Add" }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>



<style lang="scss" scoped>
.demo-tabs > .el-tabs__content {
  padding: 32px;
  color: #d8dde6;
  font-size: 32px;
  font-weight: 600;
}

.edit-btn {
  color: #409eff;
}

.delete-btn {
  color: #f56c6c;
}

.pagination-container-1 {
  display: flex;
  justify-content: center;
}

.pagination-container-2 {
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>