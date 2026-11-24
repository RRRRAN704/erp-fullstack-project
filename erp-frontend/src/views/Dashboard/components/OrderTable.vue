<script setup>
import dayjs from "dayjs";

// One table for both tabs: active orders (Assign for managers / Archive) and archived ones (Unarchive).
defineProps({
  rows: { type: Array, required: true },
  total: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  archived: { type: Boolean, default: false },
  canAssign: { type: Boolean, default: false },
  getEmployeeName: { type: Function, required: true },
});

const emit = defineEmits(["page-change", "assign", "archive", "restore"]);
</script>

<template>
  <el-table :data="rows" stripe class="tableBox">
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
        {{ row.updateTime ? dayjs(row.updateTime).format("YYYY-MM-DD HH:mm:ss") : "-" }}
      </template>
    </el-table-column>
    <el-table-column label="Operation" width="160" align="center">
      <template #default="{ row }">
        <div v-if="!archived" class="operation-buttons">
          <el-button
            v-if="canAssign"
            type="text"
            size="small"
            class="edit-btn"
            @click="emit('assign', row.id)"
          >
            Assign
          </el-button>
          <el-button type="text" size="small" class="delete-btn" @click="emit('archive', row.id)">
            Archive
          </el-button>
        </div>
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

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}
</style>
