<script setup>
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { assignOrderAPI } from "@/apis/order";

// Manager assigns an order to an employee of their department.
const visible = defineModel({ type: Boolean, required: true });
const props = defineProps({
  orderId: { type: Number, default: null },
  employees: { type: Array, required: true }, // the employees offered in the dropdown
});
const emit = defineEmits(["assigned"]);

const selectedEmployeeId = ref(null);

// Start with no selection each time the dialog opens
watch(visible, (open) => {
  if (open) selectedEmployeeId.value = null;
});

const confirmAssignOrder = async () => {
  if (!selectedEmployeeId.value) {
    ElMessage.warning("Please select an employee");
    return;
  }

  try {
    await assignOrderAPI({ id: props.orderId, employeeId: selectedEmployeeId.value });
    ElMessage.success("Order assigned successfully");
    visible.value = false;
    emit("assigned");
  } catch (error) {
    ElMessage.error("Failed to assign order");
    console.error("Assign order error:", error);
  }
};
</script>

<template>
  <el-dialog v-model="visible" title="Assign Order" width="400px">
    <el-form label-width="120px">
      <el-form-item label="Assign to:">
        <el-select
          v-model="selectedEmployeeId"
          placeholder="Please select an employee"
          style="width: 100%"
        >
          <el-option
            v-for="employee in employees"
            :key="employee.id"
            :label="employee.name"
            :value="employee.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmAssignOrder">Confirm</el-button>
      </span>
    </template>
  </el-dialog>
</template>
