<script setup>
import {
  positionPageQueryAPI,
  addPositionAPI,
  deletePositionAPI,
  editPositionAPI,
  getAllDepartmentAPI,
} from "@/apis/position";
import dayjs from "dayjs";
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

//get all departments for dropdown
const departmentList = ref([]);
const getAllDepartments = async () => {
  const res = await getAllDepartmentAPI();
  departmentList.value = res.data;
};
onMounted(() => {
  getAllDepartments();
});

const getDepartmentName = (departmentId) => {
  const department = departmentList.value.find(
    (dept) => dept.id === departmentId
  );
  return department ? department.departmentName : "-";
};

//page query logic
const positionList = ref([]);
const total = ref(0);
const params = ref({
  page: 1,
  pageSize: 5,
});

const getPositionList = async () => {
  const res = await positionPageQueryAPI(params.value);
  positionList.value = res.data.records;
  total.value = res.data.total;
};
onMounted(() => {
  getPositionList();
});

//page change
const pageChange = (page) => {
  console.log(page);
  params.value.page = page;
  getPositionList();
};
//position name input
const positionName = ref(""); // position name input
const onSearch = () => {
  params.value.page = 1; // reset to first page on search
  if (positionName.value.trim()) {
    params.value.positionName = positionName.value.trim();
  } else {
    delete params.value.positionName; // remove filter if input is empty
  }
  getPositionList();
};

// ============= Dialog  =============
const showDialog = ref(false);
const isEdit = ref(false);
const formRef = ref(null);

// table form
const form = ref({
  id: null,
  positionName: "",
  departmentId: null,
});

// rules
const rules = {
  positionName: [
    { required: true, message: "Please enter position name", trigger: "blur" },
  ],
  departmentId: [
    { required: true, message: "Please choose department", trigger: "change" },
  ],
};

// reset form
const resetForm = () => {
  form.value = {
    id: null,
    positionName: "",
    departmentId: null,
  };
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// Open add Dialog
const openAddDialog = () => {
  isEdit.value = false;
  resetForm();
  showDialog.value = true;
};

// open edit Dialog
const openEditDialog = (row) => {
  isEdit.value = true;
  form.value = {
    id: row.id,
    positionName: row.positionName,
    departmentId: row.departmentId,
  };
  showDialog.value = true;
};

// submit form
const submitForm = async () => {
  if (!formRef.value) return;

  try {
    // validate form
    await formRef.value.validate();

    if (isEdit.value) {
      // edit position
      await editPositionAPI(form.value);
      ElMessage.success("Edit position successfully");
    } else {
      // add position
      const submitData = {
        positionName: form.value.positionName,
        departmentId: form.value.departmentId,
      };
      await addPositionAPI(submitData);
      ElMessage.success("Successfully added position");
    }

    showDialog.value = false;
    getPositionList();
  } catch (error) {
    console.error("Submission error:", error);
    ElMessage.error("Submission failed, please check the form");
  }
};

// delete position
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
    await deletePositionAPI([id]);
    ElMessage.success("Delete position successfully");
    getPositionList();
  } catch (error) {
    ElMessage({ type: "error", message: "Failed to delete department" });
  }
};
</script>


<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 5px">Position Name：</label>
        <el-input
          v-model="positionName"
          placeholder="Please enter name"
          style="width: 15%"
          clearable
        />
        <el-button class="normal-btn continue" @click="onSearch"
          >Search</el-button
        >
        <el-button type="primary" style="float: right" @click="openAddDialog">
          + Add Position
        </el-button>
      </div>

      <el-table :data="positionList" stripe class="tableBox">
        <el-table-column prop="positionName" label="Position Name" />
        <el-table-column label="Department">
          <template #default="{ row }">
            {{ getDepartmentName(row.departmentId) }}
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

      <!-- Dialog  -->
      <el-dialog
        :title="isEdit ? 'Edit Position' : 'Add Position'"
        v-model="showDialog"
        width="30%"
      >
        <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
          <!-- Department name input -->
          <el-form-item label="Position Name" prop="positionName">
            <el-input
              v-model="form.positionName"
              placeholder="Enter position name"
            />
          </el-form-item>
          
          <!-- department select -->
          <el-form-item label="Department" prop="departmentId">
            <el-select
              v-model="form.departmentId"
              placeholder="Please select department"
              style="width: 100%"
            >
              <el-option
                v-for="dept in departmentList"
                :key="dept.id"
                :label="dept.departmentName"
                :value="dept.id"
              />
            </el-select>
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
