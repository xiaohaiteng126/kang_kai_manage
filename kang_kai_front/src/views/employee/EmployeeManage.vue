<template>
  <el-card>
    <!-- Search Bar -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom: 16px">
      <el-form-item label="所属项目" required>
        <el-select v-model="searchForm.projectId" placeholder="请选择项目" clearable style="width: 180px" @change="handleSearch">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="searchForm.name" placeholder="姓名" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="searchForm.phone" placeholder="手机号" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="身份证">
        <el-input v-model="searchForm.idCard" placeholder="身份证" clearable style="width: 180px" />
      </el-form-item>
    </el-form>

    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <span></span>
      <span>
        <el-button type="success" @click="handleAdd" :disabled="!searchForm.projectId">新增员工</el-button>
        <el-button type="warning" @click="handleExport" :disabled="!tableData.length">导出Excel</el-button>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </span>
    </div>

    <el-table :data="tableData" border v-loading="loading" style="width: 100%"
      :header-cell-style="{ textAlign: 'center' }">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="dailyWage" label="日资" width="100" />
      <el-table-column prop="age" label="年龄" width="70" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="bankCard" label="银行卡号" width="170" />
      <el-table-column prop="idCard" label="身份证号" width="180" />
      <el-table-column prop="address" label="地址" min-width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '在项' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
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
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑员工' : '新增员工'" width="600px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="日资" prop="dailyWage">
              <el-input-number v-model="form.dailyWage" :min="0" :precision="2" style="width: 100%" placeholder="日资" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="0" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行卡号" prop="bankCard">
              <el-input v-model="form.bankCard" placeholder="请输入银行卡号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="在项" value="在项" />
                <el-option label="离项" value="离项" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="请选择" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listEmployees, createEmployee, updateEmployee, deleteEmployee } from '@/api/employee'
import { listProjects } from '@/api/project'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'

const tableData = ref([])
const projects = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  projectId: '',
  name: '',
  phone: '',
  idCard: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({
  name: '',
  dailyWage: null,
  age: null,
  gender: '',
  phone: '',
  bankCard: '',
  idCard: '',
  address: '',
  status: '在项',
  projectId: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  dailyWage: [{ required: true, message: '请输入日资', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }]
}

async function fetchProjects() {
  const res = await listProjects()
  projects.value = res.data || []
  if (projects.value.length > 0 && !searchForm.projectId) {
    searchForm.projectId = projects.value[0].id
    fetchData()
  }
}

async function fetchData() {
  if (!searchForm.projectId) {
    tableData.value = []
    return
  }
  loading.value = true
  try {
    const res = await listEmployees({
      projectId: searchForm.projectId,
      name: searchForm.name || undefined,
      phone: searchForm.phone || undefined,
      idCard: searchForm.idCard || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  fetchData()
}

function handleReset() {
  searchForm.name = ''
  searchForm.phone = ''
  searchForm.idCard = ''
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.name = ''
  form.dailyWage = null
  form.age = null
  form.gender = '男'
  form.phone = ''
  form.bankCard = ''
  form.idCard = ''
  form.address = ''
  form.status = '在项'
  form.projectId = searchForm.projectId
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateEmployee(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createEmployee({ ...form })
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
    await ElMessageBox.confirm(`确定删除员工 "${row.name}" 吗？`, '删除确认', { type: 'warning' })
    await deleteEmployee(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

async function handleExport() {
  // Fetch all data for export
  const res = await listEmployees({
    projectId: searchForm.projectId,
    name: searchForm.name || undefined,
    phone: searchForm.phone || undefined,
    idCard: searchForm.idCard || undefined,
    pageNum: 1,
    pageSize: 9999
  })
  const allData = res.data?.records || []
  const exportData = allData.map((r, i) => ({
    '序号': i + 1,
    '姓名': r.name,
    '日资': r.dailyWage,
    '年龄': r.age,
    '性别': r.gender,
    '手机号': r.phone,
    '银行卡号': r.bankCard,
    '身份证号': r.idCard,
    '地址': r.address,
    '状态': r.status
  }))
  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '员工信息')
  const project = projects.value.find(p => p.id === searchForm.projectId)
  const projectName = project ? project.projectName : '未知项目'
  const now = new Date()
  const timestamp = now.getFullYear().toString() +
    String(now.getMonth() + 1).padStart(2, '0') +
    String(now.getDate()).padStart(2, '0') +
    String(now.getHours()).padStart(2, '0') +
    String(now.getMinutes()).padStart(2, '0') +
    String(now.getSeconds()).padStart(2, '0')
  XLSX.writeFile(wb, `${projectName}员工信息${timestamp}.xlsx`)
  ElMessage.success('导出成功')
}

onMounted(() => {
  fetchProjects()
})
</script>
