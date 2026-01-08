<script setup>
import { onMounted, ref } from "vue";
import {
  departmentPageQueryAPI,
  addDepartmentAPI,
  deleteDepartmentAPI,
  editDepartmentAPI,
} from "@/apis/department";
import dayjs from "dayjs";

//page query logic
const departmentList = ref([]);
const total = ref(0);
const params = ref({
  page: 1,
  pageSize: 5,
});

const getDepartmentList = async () => {
  const res = await departmentPageQueryAPI(params.value);
  departmentList.value = res.data.records;
  total.value = res.data.total;
};
onMounted(() => {
  getDepartmentList();
});

//page change
const pageChange = (page) => {
  console.log(page);
  params.value.page = page;
  getDepartmentList();
};

const departmentName = ref(""); // department name input

const onSearch = () => {
  params.value.page = 1; // reset to first page on search
  if (departmentName.value.trim()) {
    params.value.departmentName = departmentName.value.trim();
  } else {
    delete params.value.departmentName; // remove filter if input is empty
  }
  getDepartmentList();
};

//add department logic/edit department logic

//rules 
const rules = {
  departmentName: [
    {
      required: true,
      message: "Department name cannot be empty",
      trigger: "blur",
    },
  ]
};

const showDialog = ref(false);
const form = ref({
  departmentName: "",
});
const formRef = ref(null);
const isEdit = ref(false);
const currentEditId = ref(null);

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      if (isEdit.value) {
        await editDepartmentAPI({
          id: currentEditId.value,
          departmentName: form.value.departmentName,
        });
        ElMessage({ type: "success", message: "Updated successfully" });
      } else {
        // add new department
        await addDepartmentAPI(form.value);
        ElMessage({
          type: "success",
          message: "Department added successfully",
        });
      }
      showDialog.value = false;
      resetForm();
      getDepartmentList(); // update the list after adding or editing
    } else {
      ElMessage({
        type: "error",
        message: "Please fill in the required fields",
      });
    }
  });
};

const resetForm = () => {
  form.value.departmentName = "";
  isEdit.value = false;
  currentEditId.value = null;
};

const openAddDialog = () => {
  resetForm();
  showDialog.value = true;
};

const openEditDialog = (row) => {
  isEdit.value = true;
  currentEditId.value = row.id;
  form.value.departmentName = row.departmentName;
  showDialog.value = true;
};

//delete department logic
const deleteDepartment = async (id) => {
  try {
    await ElMessageBox.confirm(
      "Are you sure you want to delete this department?",
      "Warning",
      {
        confirmButtonText: "Yes",
        cancelButtonText: "Cancel",
        type: "warning",
      }
    );
    await deleteDepartmentAPI([id]);
    ElMessage({ type: "success", message: "Department deleted successfully" });
    getDepartmentList(); // refresh the list
  } catch (error) {
    ElMessage({ type: "error", message: "Failed to delete department" });
  }
};
</script>




<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 5px">Department Name：</label>
        <el-input
          v-model="departmentName"
          placeholder="Please enter name"
          style="width: 15%"
          clearable
        />
        <el-button class="normal-btn continue" @click="onSearch"
          >Search</el-button
        >
        <el-button type="primary" style="float: right" @click="openAddDialog">
          + Add Department
        </el-button>
      </div>

      <el-table :data="departmentList" stripe class="tableBox">
        <el-table-column prop="departmentName" label="Deparment Name" />
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
              @click="deleteDepartment(row.id)"
            >
              Delete
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px">
        <el-pagination
          :total="total"
          @current-change="pageChange"
          :page-size="params.pageSize"
          background
          layout="prev, pager, next"
        />
      </div>

      <!-- el dialouge -->
      <el-dialog
        :title="isEdit ? 'Edit Department' : 'Add Department'"
        v-model="showDialog"
        width="30%"
      >
        <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
          <el-form-item label="Department Name" prop="departmentName">
            <el-input
              v-model="form.departmentName"
              placeholder="Enter department name"
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showDialog = false">Cancel</el-button>
          <el-button type="primary" @click="submitForm">Confirm</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>



<style lang="scss" scoped>
.edit-btn {
  color: #409eff;
}

.delete-btn {
  color: #f56c6c;
}
.pagination-container {
  display: flex;
  justify-content: center;
}
</style>