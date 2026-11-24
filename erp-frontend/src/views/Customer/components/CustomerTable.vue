<script setup>
import dayjs from "dayjs";

// One table for both tabs: active clients (Edit / Archive) and archived ones (Unarchive).
defineProps({
  rows: { type: Array, required: true },
  total: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  archived: { type: Boolean, default: false },
  // id → name lookups owned by the page
  getEmployeeName: { type: Function, required: true },
  getDepartmentName: { type: Function, required: true },
});

const emit = defineEmits(["page-change", "edit", "archive", "restore"]);
</script>

<template>
  <el-table :data="rows" stripe class="tableBox">
    <el-table-column min-width="120" prop="companyName" label="Company Name" />
    <el-table-column min-width="120" prop="contactPerson" label="Contact Person" />
    <el-table-column min-width="120" prop="contactPhone" label="Contact Phone" />
    <el-table-column min-width="180" prop="contactEmail" label="Contact Email" />
    <el-table-column min-width="120" label="Employee">
      <template #default="{ row }">
        {{ getEmployeeName(row.employeeId) }}
      </template>
    </el-table-column>
    <el-table-column min-width="120" label="Department">
      <template #default="{ row }">
        {{ getDepartmentName(row.departmentId) }}
      </template>
    </el-table-column>
    <el-table-column min-width="180" prop="address" label="Address" />
    <el-table-column min-width="120" prop="remark" label="Remark" />
    <el-table-column min-width="170" label="Last Update Time">
      <template #default="{ row }">
        {{ dayjs(row.updateTime).format("YYYY-MM-DD HH:mm:ss") }}
      </template>
    </el-table-column>
    <el-table-column label="Operation" width="160" align="center" fixed="right">
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
