<script setup>
import {
  contractPageQueryAPI,
  archivedContractPageQueryAPI,
  addContractAPI,
  archiveContractAPI,
  restoreContractAPI,
  editContractAPI,
  getAllEmployeeAPI,
  getAllPositionAPI,
  getAllDepartmentAPI,
  uploadAPI,
  getAllClientAPI,
} from "@/apis/contract";

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

//get all client
const clientList = ref([]);
const getAllClient = async () => {
  const res = await getAllClientAPI();
  clientList.value = res.data;
};

//get all department
const departmentList = ref([]);
const getAllDepartment = async () => {
  const res = await getAllDepartmentAPI();
  departmentList.value = res.data;
};
onMounted(() => {
  getAllEmployee(), getAllDepartment(), getAllClient();
});

//get names
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

const getClientCompanyName = (clientId) => {
  const client = clientList.value.find((cli) => cli.id === clientId);
  return client ? client.companyName : "-";
};

//get unarchived contract page query
const activeContractList = ref([]);
const total_1 = ref(0);
const params_1 = ref({
  page: 1,
  pageSize: 5,
});
const getActiveContractList = async () => {
  const res = await contractPageQueryAPI(params_1.value);
  activeContractList.value = res.data.records;
  total_1.value = res.data.total;
};
onMounted(() => {
  getActiveContractList();
});

//page change for unarchived contract
const pageChange_1 = (page) => {
  params_1.value.page = page;
  getActiveContractList();
};

//Contract name input
const contractName = ref("");
const onSearch = () => {
  params_1.value.page = 1; // Reset to first page on search
  if (contractName.value.trim()) {
    params_1.value.contractName = contractName.value.trim();
  } else {
    delete params_1.value.contractName; // Remove filter if input is empty
  }
  getActiveContractList();
};

//get archived client page query
const archivedContractList = ref([]);
const total_2 = ref(0);
const params_2 = ref({
  page: 1,
  pageSize: 5,
});
const getArchivedContractList = async () => {
  const res = await archivedContractPageQueryAPI(params_2.value);
  archivedContractList.value = res.data.records;
  total_2.value = res.data.total;
};
onMounted(() => {
  getArchivedContractList();
});

//page change for unarchived contract
const pageChange_2 = (page) => {
  params_2.value.page = page;
  getArchivedContractList();
};

//archive contract
const archiveContract = async (contractId) => {
  try {
    await archiveContractAPI(contractId);
    ElMessage.success("Contract archived successfully");
    getActiveContractList();
    getArchivedContractList();
  } catch (error) {
    ElMessage.error("Failed to archive contract");
  }
};

//restore contract
const restoreContract = async (contractId) => {
  try {
    await restoreContractAPI(contractId);
    ElMessage.success("Contract restored successfully");
    getActiveContractList();
    getArchivedContractList();
  } catch (error) {
    ElMessage.error("Failed to restore contract");
  }
};

//upload file
const handleFileUpload = async (file) => {
  try {
    const res = await uploadAPI(file);
    const filePath = res.data;
    ElMessage.success("File uploaded successfully");
    console.log("File path:", filePath);
    formData.value.fileUrl = filePath;
    return filePath;
  } catch (error) {
    console.error("Upload failed:", error);
  }
};

//add and edit contract logic
// ============= Dialog  =============
const showDialog = ref(false);
const isEdit = ref(false);
const formRef = ref(null);

// Status options for dropdown
const statusOptions = ref([
  { label: "Active", value: 0 },
  { label: "Archived", value: 1 },
]);

//Form data for contract
const formData = ref({
  id: null,
  contractName: "",
  contractNumber: "",
  clientId: null,
  employeeId: null,
  departmentId: null,
  amount: 0,
  startDate: "",
  endDate: "",
  fileUrl: "",
  isArchived: 0,
});

// Form validation rules
const rules = ref({
  contractName: [
    { required: true, message: "Please enter contract name", trigger: "blur" },
  ],
  contractNumber: [
    {
      required: true,
      message: "Please enter contract number",
      trigger: "blur",
    },
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
});

// Reset form data
const resetFormData = () => {
  formData.value = {
    id: null,
    contractName: "",
    contractNumber: "",
    clientId: null,
    employeeId: null,
    departmentId: null,
    amount: 0,
    startDate: "",
    endDate: "",
    fileUrl: "",
    isArchived: 0,
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
    // Validate form
    await formRef.value.validate();


    formData.value.amount = parseInt(formData.value.amount);

    if (isEdit.value) {
      // Edit contract
      await editContractAPI(formData.value);
      ElMessage.success("Contract updated successfully");
    } else {
      // Add new contract
      await addContractAPI(formData.value);
      ElMessage.success("Contract added successfully");
    }

    // Close dialog and refresh data
    showDialog.value = false;
    getActiveContractList();
    getArchivedContractList();
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

// Cancel form
const cancelForm = () => {
  showDialog.value = false;
  resetFormData();
};

const openFile = (fileUrl) => {
  if (fileUrl) {
    window.open(fileUrl, '_blank'); 
  } else {
    ElMessage.warning('No file available');
  }
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
        <el-tab-pane label="Contracts" name="first">
          <div class="tableBar">
            <label style="margin-right: 5px">Contract Name：</label>
            <el-input
              placeholder="Please enter name"
              style="width: 15%"
              clearable
              v-model="contractName"
            />
            <el-button class="normal-btn continue" @click="onSearch"
              >Search</el-button
            >
            <el-button
              type="primary"
              style="float: right"
              @click="openAddDialog"
            >
              + Add Contract
            </el-button>
          </div>

          <el-table :data="activeContractList" stripe class="tableBox">
            <el-table-column prop="contractName" label="Contract Name" />
            <el-table-column prop="contractNumber" label="Contract Number" />
            <el-table-column label="Client Company Name">
              <template #default="{ row }">
                {{ getClientCompanyName(row.clientId) }}
              </template>
            </el-table-column>
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
            <el-table-column label="Amount">
              <template #default="{ row }">
                {{ `£${row.amount}` }}
              </template>
            </el-table-column>
            <el-table-column label="Start Date">
              <template #default="{ row }">
                {{ dayjs(row.startDate).format("YYYY-MM-DD") }}
              </template>
            </el-table-column>
            <el-table-column label="End Date">
              <template #default="{ row }">
                {{ dayjs(row.endDate).format("YYYY-MM-DD") }}
              </template>
            </el-table-column>
            <el-table-column label="File">
              <template #default="{ row }">
                <el-button
                  v-if="row.fileUrl"
                  type="primary"
                  size="small"
                  @click="openFile(row.fileUrl)"
                  
                >
                  View 
                </el-button>
                <span v-else style="color: #909399">No File</span>
              </template>
            </el-table-column>
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
                  @click="archiveContract(row.id)"
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
          <el-table :data="archivedContractList" stripe class="tableBox">
            <el-table-column prop="contractName" label="Contract Name" />
            <el-table-column prop="contractNumber" label="Contract Number" />
            <el-table-column label="Client Company Name">
              <template #default="{ row }">
                {{ getClientCompanyName(row.clientId) }}
              </template>
            </el-table-column>
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
            <el-table-column label="Amount">
              <template #default="{ row }">
                {{ `£${row.amount}` }}
              </template>
            </el-table-column>
            <el-table-column label="Start Date">
              <template #default="{ row }">
                {{ dayjs(row.startDate).format("YYYY-MM-DD") }}
              </template>
            </el-table-column>
            <el-table-column label="End Date">
              <template #default="{ row }">
                {{ dayjs(row.endDate).format("YYYY-MM-DD") }}
              </template>
            </el-table-column>
            <el-table-column label="File">
              <template #default="{ row }">
                <el-button
                  v-if="row.fileUrl"
                  type="primary"
                  size="small"
                  @click="openFile(row.fileUrl)"
                >
                  View
                </el-button>
                <span v-else style="color: #909399">No File</span>
              </template>
            </el-table-column>
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
                  @click="restoreContract(row.id)"
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
      :title="isEdit ? 'Edit Contract' : 'Add Contract'"
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
              v-for="employee in employeeList"
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
              v-for="client in clientList"
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
            ref="uploadRef"
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
.pagination-container-1 {
  display: flex;
  justify-content: center;
}

.pagination-container-2 {
  display: flex;
  justify-content: center;
}

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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>