<script setup>
import {
  contractPageQueryAPI,
  archivedContractPageQueryAPI,
  archiveContractAPI,
  restoreContractAPI,
  getAllEmployeeAPI,
  getAllDepartmentAPI,
  getAllClientAPI,
} from "@/apis/contract";

import { computed, onMounted, ref } from "vue";
import { assignableEmployees, visibleClients } from "@/utils/access";
import { ElMessage } from "element-plus";
import ContractTable from "./components/ContractTable.vue";
import ContractFormDialog from "./components/ContractFormDialog.vue";

const activeName = ref("first");

//get all employee
const employeeList = ref([]);
const getAllEmployee = async () => {
  const res = await getAllEmployeeAPI();
  employeeList.value = res.data;
};
// only the employees you may assign a contract to (employeeList stays complete for showing names)
const assignableEmployeeList = computed(() => assignableEmployees(employeeList.value));

//get all client
const clientList = ref([]);
const getAllClient = async () => {
  const res = await getAllClientAPI();
  clientList.value = res.data;
};
// only clients from your own client list can be linked
const selectableClientList = computed(() => visibleClients(clientList.value));

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

//get archived contract page query
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

//page change for archived contract
const pageChange_2 = (page) => {
  params_2.value.page = page;
  getArchivedContractList();
};

const refreshLists = () => {
  getActiveContractList();
  getArchivedContractList();
};

onMounted(() => {
  getAllEmployee();
  getAllDepartment();
  getAllClient();
  refreshLists();
});

//archive contract
const archiveContract = async (contractId) => {
  try {
    await archiveContractAPI(contractId);
    ElMessage.success("Contract archived successfully");
    refreshLists();
  } catch (error) {
    ElMessage.error("Failed to archive contract");
  }
};

//restore contract
const restoreContract = async (contractId) => {
  try {
    await restoreContractAPI(contractId);
    ElMessage.success("Contract restored successfully");
    refreshLists();
  } catch (error) {
    ElMessage.error("Failed to restore contract");
  }
};

// Add/Edit dialog: editingContract = null means "add"
const showDialog = ref(false);
const editingContract = ref(null);

const openAddDialog = () => {
  editingContract.value = null;
  showDialog.value = true;
};

const openEditDialog = (row) => {
  editingContract.value = row;
  showDialog.value = true;
};
</script>

<template>
  <div class="dashboard-container">
    <div class="container">
      <el-tabs v-model="activeName" type="card" class="demo-tabs">
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

          <ContractTable
            :rows="activeContractList"
            :total="total_1"
            :page-size="params_1.pageSize"
            :get-client-company-name="getClientCompanyName"
            :get-employee-name="getEmployeeName"
            :get-department-name="getDepartmentName"
            @page-change="pageChange_1"
            @edit="openEditDialog"
            @archive="archiveContract"
          />
        </el-tab-pane>
        <el-tab-pane label="Archived" name="second">
          <ContractTable
            archived
            :rows="archivedContractList"
            :total="total_2"
            :page-size="params_2.pageSize"
            :get-client-company-name="getClientCompanyName"
            :get-employee-name="getEmployeeName"
            :get-department-name="getDepartmentName"
            @page-change="pageChange_2"
            @restore="restoreContract"
          />
        </el-tab-pane>
      </el-tabs>
    </div>

    <ContractFormDialog
      v-model="showDialog"
      :contract="editingContract"
      :employees="employeeList"
      :assignable-employees="assignableEmployeeList"
      :clients="selectableClientList"
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
