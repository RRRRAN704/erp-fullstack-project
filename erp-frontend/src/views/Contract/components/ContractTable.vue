<script setup>
import dayjs from "dayjs";
import { ElMessage } from "element-plus";

// One table for both tabs: active contracts (Edit / Archive) and archived ones (Unarchive).
defineProps({
  rows: { type: Array, required: true },
  total: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  archived: { type: Boolean, default: false },
  // id → name lookups owned by the page
  getClientCompanyName: { type: Function, required: true },
  getEmployeeName: { type: Function, required: true },
  getDepartmentName: { type: Function, required: true },
});

const emit = defineEmits(["page-change", "edit", "archive", "restore"]);

const openFile = (fileUrl) => {
  if (fileUrl) {
    window.open(fileUrl, "_blank");
  } else {
    ElMessage.warning("No file available");
  }
};
</script>

<template>
  <el-table :data="rows" stripe class="tableBox">
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
        <template v-if="!archived">
          <el-button type="text" size="small" class="edit-btn" @click="emit('edit', row)">
            Edit
          </el-button>
          <el-button type="text" size="small" class="delete-btn" @click="emit('archive', row.id)">
            Archive
          </el-button>
        </template>
        <el-button v-else type="text" size="small" class="delete-btn" @click="emit('restore', row.id)">
          Unarchive
        </el-button>
      </template>
    </el-table-column>
  </el-table>
  <div class="pagination-container">
    <el-pagination
      :total="total"
      @current-change="(page) => emit('page-change', page)"
      :page-size="pageSize"
      background
      layout="prev, pager, next"
    />
  </div>
</template>

<style lang="scss" scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.edit-btn {
  color: #409eff;
}

.delete-btn {
  color: #f56c6c;
}
</style>
