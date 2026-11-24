<script setup>
import { computed, onMounted, ref } from "vue";
import { assignableEmployees, isManager } from "@/utils/access";
import { ElMessage } from "element-plus";
import {
  OrderPageQueryAPI,
  archivedOrderPageQueryAPI,
  archiveOrderAPI,
  restoreOrderAPI,
  getAllEmployeeAPI,
} from "@/apis/order";
import OrderTable from "./components/OrderTable.vue";
import AssignOrderDialog from "./components/AssignOrderDialog.vue";

const activeName = ref("first");

//get all employee
const employeeList = ref([]);
const getAllEmployee = async () => {
  const res = await getAllEmployeeAPI();
  employeeList.value = res.data;
};
// a manager can assign orders only to employees of their own department
const assignableEmployeeList = computed(() => assignableEmployees(employeeList.value));

const getEmployeeName = (employeeId) => {
  const employee = employeeList.value.find((emp) => emp.id === employeeId);
  return employee ? employee.name : "-";
};

//active order page query
const activeOrderList = ref([]);
const total_1 = ref(0);
const params_1 = ref({
  page: 1,
  pageSize: 5,
});
const getActiveOrderList = async () => {
  const res = await OrderPageQueryAPI(params_1.value);
  activeOrderList.value = res.data.records;
  total_1.value = res.data.total;
};

//page change for active order list
const pageChange_1 = (page) => {
  params_1.value.page = page;
  getActiveOrderList();
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
  getActiveOrderList();
};

//get archived order page query
const archivedOrderList = ref([]);
const total_2 = ref(0);
const params_2 = ref({
  page: 1,
  pageSize: 5,
});
const getArchivedOrderList = async () => {
  const res = await archivedOrderPageQueryAPI(params_2.value);
  archivedOrderList.value = res.data.records;
  total_2.value = res.data.total;
};

//page change for archived order
const pageChange_2 = (page) => {
  params_2.value.page = page;
  getArchivedOrderList();
};

const refreshLists = () => {
  getActiveOrderList();
  getArchivedOrderList();
};

onMounted(() => {
  getAllEmployee();
  refreshLists();
});

//archive order
const archiveOrder = async (orderId) => {
  try {
    await archiveOrderAPI(orderId);
    ElMessage.success("Order archived successfully");
    refreshLists();
  } catch (error) {
    ElMessage.error("Failed to archive order");
  }
};

//restore order
const restoreOrder = async (orderId) => {
  try {
    await restoreOrderAPI(orderId);
    ElMessage.success("Order restored successfully");
    refreshLists();
  } catch (error) {
    ElMessage.error("Failed to restore order");
  }
};

// assign order
const assignDialogVisible = ref(false);
const selectedOrderId = ref(null);

const openAssignDialog = (orderId) => {
  selectedOrderId.value = orderId;
  assignDialogVisible.value = true;
};
</script>

<template>
  <div class="dashboard-container">
    <div class="container">
      <el-tabs v-model="activeName" type="card" class="demo-tabs">
        <el-tab-pane label="Orders" name="first">
          <div class="tableBar">
            <label style="margin-right: 5px">Company Name：</label>
            <el-input
              placeholder="Please enter name"
              class="search-input"
              clearable
              v-model="companyName"
            />
            <el-button class="normal-btn continue" @click="onSearch"
              >Search</el-button
            >
          </div>

          <OrderTable
            :rows="activeOrderList"
            :total="total_1"
            :page-size="params_1.pageSize"
            :can-assign="isManager()"
            :get-employee-name="getEmployeeName"
            @page-change="pageChange_1"
            @assign="openAssignDialog"
            @archive="archiveOrder"
          />
        </el-tab-pane>
        <el-tab-pane label="Archived" name="second">
          <OrderTable
            archived
            :rows="archivedOrderList"
            :total="total_2"
            :page-size="params_2.pageSize"
            :get-employee-name="getEmployeeName"
            @page-change="pageChange_2"
            @restore="restoreOrder"
          />
        </el-tab-pane>
      </el-tabs>
    </div>

    <AssignOrderDialog
      v-model="assignDialogVisible"
      :order-id="selectedOrderId"
      :employees="assignableEmployeeList"
      @assigned="getActiveOrderList"
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
