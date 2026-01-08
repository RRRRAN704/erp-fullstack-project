<script setup>
import dayjs from "dayjs";
import { onMounted, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
const activeName = ref("first");
import { useUserStore } from "@/stores/user";
const userStore = useUserStore();

import {
  assignOrderAPI,
  OrderPageQueryAPI,
  archivedOrderPageQueryAPI,
  archiveOrderAPI,
  restoreOrderAPI,
  getAllEmployeeAPI,
} from "@/apis/order";

import { getAiResponse } from "@/apis/ai";

const userInput = ref(""); // user input
const messages = ref([]); // chat history

const sendMessage = async () => {
  if (!userInput.value.trim()) {
    ElMessage.warning("Please enter a message");
    return;
  }

  // store the user input in messages.
  messages.value.push({ role: "user", content: userInput.value });

  try {
    const res = await getAiResponse(userInput.value);

    const aiText = res.data.text || "AI cannot understand your request";

    messages.value.push({ role: "ai", content: aiText });
  } catch (error) {
    console.error("AI call failed:", error);
    messages.value.push({
      role: "ai",
      content: "Failed to call AI interface. Please try again later.",
    });
  }

  // clear input field
  userInput.value = "";
};

//get all employee
const employeeList = ref([]);
const getAllEmployee = async () => {
  const res = await getAllEmployeeAPI();
  employeeList.value = res.data;
};

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
onMounted(() => {
  getAllEmployee();
  getActiveOrderList();
  getArchivedOrderList();
});

//page change for unarchived order
const pageChange_2 = (page) => {
  params_2.value.page = page;
  getArchivedOrderList();
};

//archive order
const archiveOrder = async (orderId) => {
  try {
    await archiveOrderAPI(orderId);
    ElMessage.success("Order archived successfully");
    getActiveOrderList();
    getArchivedOrderList();
  } catch (error) {
    ElMessage.error("Failed to archive order");
  }
};

//restore client
const restoreOrder = async (orderId) => {
  try {
    await restoreOrderAPI(orderId);
    ElMessage.success("Order restored successfully");
    getActiveOrderList();
    getArchivedOrderList();
  } catch (error) {
    ElMessage.error("Failed to restore order");
  }
};

// assign order
const assignDialogVisible = ref(false);
const selectedOrderId = ref(null);
const selectedEmployeeId = ref(null);

// open dialog to assign order
const openAssignDialog = (orderId) => {
  selectedOrderId.value = orderId;
  selectedEmployeeId.value = null;
  assignDialogVisible.value = true;
};

// confirm assign order
const confirmAssignOrder = async () => {
  if (!selectedEmployeeId.value) {
    ElMessage.warning("Please select an employee");
    return;
  }

  try {
    const params = {
      id: selectedOrderId.value,
      employeeId: selectedEmployeeId.value,
    };
    await assignOrderAPI(params);
    ElMessage.success("Order assigned successfully");
    assignDialogVisible.value = false;
    getActiveOrderList();
  } catch (error) {
    ElMessage.error("Failed to assign order");
    console.error("Assign order error:", error);
  }
};

// cencel assign order
const cancelAssignOrder = () => {
  assignDialogVisible.value = false;
  selectedOrderId.value = null;
  selectedEmployeeId.value = null;
};
</script>



<template>
  <div class="dashboard-container">
    <!-- AI chat box-->
    <div class="ai-chat-box">
      <div class="ai-chat-header">
        <div class="title">
          <div class="name">🤖 AI Chatbot</div>
          <div class="sub">
            Please ask these type of questions only: Number of contracts /
            Contract amount / Number of orders / Top 5 employees
          </div>
        </div>
      </div>

      <div class="messages">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role]"
        >
          <strong v-if="msg.role === 'user'">You：</strong>
          <strong v-else>AI：</strong>
          <span>{{ msg.content }}</span>
        </div>
      </div>

      <div class="input-area">
        <el-input
          v-model="userInput"
          placeholder="Please enter your message..."
          @keyup.enter="sendMessage"
          style="width: 80%; margin-right: 10px"
        />
        <el-button type="primary" @click="sendMessage">Send</el-button>
      </div>
    </div>

    <div class="container">
      <el-tabs
        v-model="activeName"
        type="card"
        class="demo-tabs"
        @tab-click="handleClick"
      >
        <el-tab-pane label="Orders" name="first">
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
          </div>

          <el-table :data="activeOrderList" stripe class="tableBox">
            <el-table-column prop="companyName" label="Company Name" />
            <el-table-column prop="contactPerson" label="Contact Person" />
            <el-table-column prop="contactEmail" label="Contact Email" />
            <el-table-column prop="contactPhone" label="Contact Phone" />
            <el-table-column label="Remark">
              <template #default="{ row }">
                {{ row.remark ? row.remark : "-" }}
              </template>
            </el-table-column>
            <el-table-column label="Assigned to">
              <template #default="{ row }">
                {{ getEmployeeName(row.employeeId) }}
              </template>
            </el-table-column>
            <el-table-column label="Last Update Time">
              <template #default="{ row }">
                {{
                  row.updateTime
                    ? dayjs(row.updateTime).format("YYYY-MM-DD HH:mm:ss")
                    : "-"
                }}
              </template>
            </el-table-column>
            <el-table-column label="Operation" width="160" align="center">
              <template #default="{ row }">
                <div class="operation-buttons">
                  <el-button
                    v-if="userStore.userInfo.position === 'Manager'"
                    type="text"
                    size="small"
                    class="edit-btn"
                    @click="openAssignDialog(row.id)"
                  >
                    Assign
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    class="delete-btn"
                    @click="archiveOrder(row.id)"
                  >
                    Archive
                  </el-button>
                </div>
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
          <el-table :data="archivedOrderList" stripe class="tableBox">
            <el-table-column prop="companyName" label="Company Name" />
            <el-table-column prop="contactPerson" label="Contact Person" />
            <el-table-column prop="contactEmail" label="Contact Email" />
            <el-table-column prop="contactPhone" label="Contact Phone" />
            <el-table-column prop="remark" label="Remark" />
            <el-table-column label="Assigned to">
              <template #default="{ row }">
                {{ getEmployeeName(row.employeeId) }}
              </template>
            </el-table-column>
            <el-table-column label="Last Update Time">
              <template #default="{ row }">
                {{
                  row.lastUpdateTime
                    ? dayjs(row.lastUpdateTime).format("YYYY-MM-DD HH:mm:ss")
                    : ""
                }}
              </template>
            </el-table-column>
            <el-table-column label="Operation" width="160" align="center">
              <template #default="{ row }">
                <el-button
                  type="text"
                  size="small"
                  class="delete-btn"
                  @click="restoreOrder(row.id)"
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

    <!-- dialog -->
    <el-dialog
      v-model="assignDialogVisible"
      title="Assign Order"
      width="400px"
      :before-close="cancelAssignOrder"
    >
      <div>
        <el-form label-width="120px">
          <el-form-item label="Assign to:">
            <el-select
              v-model="selectedEmployeeId"
              placeholder="Please select an employee"
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
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelAssignOrder">Cancel</el-button>
          <el-button type="primary" @click="confirmAssignOrder">
            Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>



<style lang="scss" scoped>
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

.demo-tabs > .el-tabs__content {
  padding: 32px;
  color: #d8dde6;
  font-size: 32px;
  font-weight: 600;
}
.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

.ai-chat-box {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
}

.messages {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 10px;
}

.message {
  margin: 5px 0;
}

.message.user {
  text-align: right;
  color: #409eff;
}

.message.ai {
  text-align: left;
  color: #67c23a;
}

.input-area {
  display: flex;
  align-items: center;
}

.ai-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.ai-chat-header .title .name {
  font-weight: 600;
  font-size: 16px;
}

.ai-chat-header .title .sub {
  font-size: 12px;
  color: #909399;
}
</style>