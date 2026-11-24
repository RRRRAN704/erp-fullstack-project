<script setup>
import {
  clientPageQueryAPI,
  archivedClientPageQueryAPI,
  archiveClientAPI,
  restoreClientAPI,
  getAllEmployeeAPI,
  getAllDepartmentAPI,
} from "@/apis/client";

import { computed, onMounted, ref } from "vue";
import { assignableEmployees } from "@/utils/access";
import { ElMessage } from "element-plus";
import CustomerTable from "./components/CustomerTable.vue";
import CustomerFormDialog from "./components/CustomerFormDialog.vue";

const activeName = ref("first");

//get all employee
const employeeList = ref([]);
const getAllEmployee = async () => {
  const res = await getAllEmployeeAPI();
  employeeList.value = res.data;
};
// only the employees you may assign a client to (employeeList stays complete for showing names)
const assignableEmployeeList = computed(() => assignableEmployees(employeeList.value));

//get all department
const departmentList = ref([]);
const getAllDepartment = async () => {
  const res = await getAllDepartmentAPI();
  departmentList.value = res.data;
};

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

//page change for archived client
const pageChange_2 = (page) => {
  params_2.value.page = page;
  getArchivedClientList();
};

const refreshLists = () => {
  getActiveClientList();
  getArchivedClientList();
};

onMounted(() => {
  getAllEmployee();
  getAllDepartment();
  refreshLists();
});

//archive client
const archiveClient = async (clientId) => {
  try {
    await archiveClientAPI(clientId);
    ElMessage.success("Client archived successfully");
    refreshLists();
  } catch (error) {
    ElMessage.error("Failed to archive client");
  }
};

//restore client
const restoreClient = async (clientId) => {
  try {
    await restoreClientAPI(clientId);
    ElMessage.success("Client restored successfully");
    refreshLists();
  } catch (error) {
    ElMessage.error("Failed to restore client");
  }
};

// Add/Edit dialog: editingClient = null means "add"
const showDialog = ref(false);
const editingClient = ref(null);

const openAddDialog = () => {
  editingClient.value = null;
  showDialog.value = true;
};

const openEditDialog = (row) => {
  editingClient.value = row;
  showDialog.value = true;
};
</script>

<template>
  <div class="dashboard-container">
    <div class="container">
      <el-tabs v-model="activeName" type="card" class="demo-tabs">
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

          <CustomerTable
            :rows="activeClientList"
            :total="total_1"
            :page-size="params_1.pageSize"
            :get-employee-name="getEmployeeName"
            :get-department-name="getDepartmentName"
            @page-change="pageChange_1"
            @edit="openEditDialog"
            @archive="archiveClient"
          />
        </el-tab-pane>
        <el-tab-pane label="Archived" name="second">
          <CustomerTable
            archived
            :rows="archivedClientList"
            :total="total_2"
            :page-size="params_2.pageSize"
            :get-employee-name="getEmployeeName"
            :get-department-name="getDepartmentName"
            @page-change="pageChange_2"
            @restore="restoreClient"
          />
        </el-tab-pane>
      </el-tabs>
    </div>

    <CustomerFormDialog
      v-model="showDialog"
      :client="editingClient"
      :employees="employeeList"
      :assignable-employees="assignableEmployeeList"
      :get-department-name="getDepartmentName"
      @saved="refreshLists"
    />
  </div>
</template>

<style lang="scss" scoped>
.demo-tabs > .el-tabs__content {
  padding: 32px;
  color: #d8dde6;
  font-size: 32px;
  font-weight: 600;
}
</style>
