<template>
  <el-card>
    <div style="margin-bottom: 16px">
      <el-button type="primary" @click="handleAdd">新增项目</el-button>
    </div>

    <el-table :data="pagedData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="projectName" label="项目名称" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="display: flex; justify-content: flex-end; margin-top: 16px">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑项目' : '新增项目'" width="450px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { listProjects, createProject, updateProject, deleteProject } from '@/api/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const pagedData = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({
  projectName: ''
})

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listProjects()
    tableData.value = res.data || []
    total.value = tableData.value.length
    pageNum.value = 1
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.projectName = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.projectName = row.projectName
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateProject(editId.value, { projectName: form.projectName })
      ElMessage.success('更新成功')
    } else {
      await createProject({ projectName: form.projectName })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除项目 "${row.projectName}" 吗？`, '删除确认', { type: 'warning' })
    await deleteProject(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    // cancel or error
  }
}

onMounted(fetchData)
</script>
