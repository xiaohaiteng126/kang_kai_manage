<template>
  <el-card>
    <div style="margin-bottom: 16px">
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
    </div>

    <el-table :data="pagedData" border v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'danger'" size="small">
            {{ row.enabled === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="warning" text size="small" @click="handleResetPwd(row)">重置密码</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="450px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" maxlength="50" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- Reset Password Dialog -->
    <el-dialog v-model="pwdDialogVisible" title="重置密码" width="400px" :close-on-click-modal="false">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="handlePwdSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { listUsers, createUser, updateUser, deleteUser, resetPassword } from '@/api/user'
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
  username: '',
  password: '',
  realName: '',
  phone: '',
  enabled: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

// Reset Password
const pwdDialogVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref(null)
const pwdTargetId = ref(null)
const pwdForm = reactive({ newPassword: '' })
const pwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listUsers()
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
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  form.enabled = 1
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.username = row.username
  form.realName = row.realName
  form.phone = row.phone
  form.enabled = row.enabled
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateUser(editId.value, {
        realName: form.realName,
        phone: form.phone,
        enabled: form.enabled
      })
      ElMessage.success('更新成功')
    } else {
      if (!form.password || form.password.length < 6) {
        ElMessage.warning('密码长度不能少于6位')
        submitLoading.value = false
        return
      }
      await createUser({
        username: form.username,
        password: form.password,
        realName: form.realName,
        phone: form.phone,
        enabled: form.enabled
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}

function handleResetPwd(row) {
  pwdTargetId.value = row.id
  pwdForm.newPassword = ''
  pwdDialogVisible.value = true
}

async function handlePwdSubmit() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdLoading.value = true
  try {
    await resetPassword(pwdTargetId.value, { newPassword: pwdForm.newPassword })
    ElMessage.success('密码重置成功')
    pwdDialogVisible.value = false
  } finally {
    pwdLoading.value = false
  }
}

onMounted(fetchData)
</script>
