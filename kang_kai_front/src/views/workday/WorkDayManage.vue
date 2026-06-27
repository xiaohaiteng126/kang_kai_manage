<template>
  <el-card>
    <!-- Controls -->
    <el-form :inline="true" style="margin-bottom: 16px">
      <el-form-item label="所属项目" required>
        <el-select v-model="projectId" placeholder="请选择项目" style="width: 200px" @change="handleQuery">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始月份">
        <el-date-picker v-model="startMonth" type="month" format="YYYY-MM" value-format="YYYY-MM" placeholder="开始月份" />
      </el-form-item>
      <el-form-item label="结束月份">
        <el-date-picker v-model="endMonth" type="month" format="YYYY-MM" value-format="YYYY-MM" placeholder="结束月份" />
      </el-form-item>
      <el-form-item style="float: right">
        <el-button type="primary" @click="handleQuery" :disabled="!projectId || !startMonth || !endMonth">查询</el-button>
        <el-button type="success" @click="handleSave" :disabled="!tableData.length" :loading="saving">保存</el-button>
      </el-form-item>
    </el-form>

    <!-- Dynamic Table -->
    <div style="overflow-x: auto" v-loading="loading">
      <el-table :data="pagedData" border style="width: 100%" v-if="months.length > 0"
        :header-cell-style="{ textAlign: 'center' }">
        <!-- Fixed columns -->
        <el-table-column type="index" label="序号" width="60" fixed="left" />
        <el-table-column prop="name" label="姓名" width="100" fixed="left" />
        <el-table-column prop="dailyWage" label="日资" width="90" fixed="left" />

        <!-- Dynamic month columns -->
        <template v-for="month in months" :key="month">
          <el-table-column :label="month">
            <el-table-column label="工日" width="110">
              <template #default="{ row }">
                <el-input-number
                  v-model="row[`${month}_workDays`]"
                  :min="0" :max="99" :precision="1"
                  size="small" controls-position="right"
                  style="width: 100%" @change="dirty = true" />
              </template>
            </el-table-column>
            <el-table-column label="计件薪资" width="130">
              <template #default="{ row }">
                <el-input-number
                  v-model="row[`${month}_piecePay`]"
                  :min="0" :precision="2"
                  size="small" controls-position="right"
                  style="width: 100%" @change="dirty = true" />
              </template>
            </el-table-column>
            <el-table-column label="借支" width="120">
              <template #default="{ row }">
                <el-input-number
                  v-model="row[`${month}_loan`]"
                  :min="0" :precision="2"
                  size="small" controls-position="right"
                  style="width: 100%" @change="dirty = true" />
              </template>
            </el-table-column>
            <el-table-column label="罚款" width="120">
              <template #default="{ row }">
                <el-input-number
                  v-model="row[`${month}_fine`]"
                  :min="0" :precision="2"
                  size="small" controls-position="right"
                  style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="工具/其他" min-width="100">
              <template #default="{ row }">
                <el-input-number
                  v-model="row[`${month}_toolsOther`]"
                  :min="0" :precision="2"
                  size="small" controls-position="right"
                  style="width: 100%" @change="dirty = true" />
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row[`${month}_remark`]" size="small" placeholder="备注" @change="dirty = true" />
              </template>
            </el-table-column>
          </el-table-column>
        </template>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px" v-if="tableData.length > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
        />
      </div>

      <el-empty v-else description="请选择项目和月份范围后查询" />
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { listProjects } from '@/api/project'
import { queryWorkDays, batchSaveWorkDays } from '@/api/workDay'
import { ElMessage, ElMessageBox } from 'element-plus'

const projects = ref([])
const projectId = ref('')
const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')
const startMonth = ref(`${currentYear}-${currentMonth}`)
const endMonth = ref(`${currentYear}-${currentMonth}`)
const dirty = ref(false)
const loading = ref(false)
const saving = ref(false)
const months = ref([])
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pagedData = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

onMounted(async () => {
  const res = await listProjects()
  projects.value = res.data || []
  if (projects.value.length > 0) {
    projectId.value = projects.value[0].id
    handleQuery()
  }
})

async function handleQuery() {
  if (!projectId.value || !startMonth.value || !endMonth.value) {
    ElMessage.warning('请选择项目和月份范围')
    return
  }

  loading.value = true
  try {
    const res = await queryWorkDays({
      projectId: projectId.value,
      startMonth: startMonth.value,
      endMonth: endMonth.value
    })

    months.value = res.data.months || []

    // Build flat table data
    dirty.value = false
    tableData.value = (res.data.employees || []).map(emp => {
      const row = {
        employeeId: emp.employeeId,
        name: emp.employeeName,
        dailyWage: emp.dailyWage
      }
      months.value.forEach(m => {
        const md = (emp.monthData && emp.monthData[m]) || {}
        row[`${m}_workDays`] = md.workDays ?? 0
        row[`${m}_piecePay`] = md.piecePay ?? 0
        row[`${m}_loan`] = md.loan ?? 0
        row[`${m}_fine`] = md.fine ?? 0
        row[`${m}_toolsOther`] = md.toolsOther ?? 0
        row[`${m}_remark`] = md.remark ?? ''
      })
      return row
    })
    total.value = tableData.value.length
    pageNum.value = 1
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const batch = []
    tableData.value.forEach(row => {
      months.value.forEach(m => {
        batch.push({
          employeeId: row.employeeId,
          projectId: projectId.value,
          yearMonth: m,
          workDays: row[`${m}_workDays`] ?? 0,
          piecePay: row[`${m}_piecePay`] ?? 0,
          loan: row[`${m}_loan`] ?? 0,
          fine: row[`${m}_fine`] ?? 0,
          toolsOther: row[`${m}_toolsOther`] ?? 0,
          remark: row[`${m}_remark`] ?? ''
        })
      })
    })
    await batchSaveWorkDays(batch)
    dirty.value = false
    ElMessage.success('保存成功')
  } catch (e) {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}

// 切换路由/关闭页面时提醒
onBeforeRouteLeave((to, from, next) => {
  if (dirty.value) {
    ElMessageBox.confirm('有未保存的数据，确定离开吗？', '提示', {
      confirmButtonText: '确定离开',
      cancelButtonText: '继续编辑',
      type: 'warning'
    }).then(() => next()).catch(() => next(false))
  } else {
    next()
  }
})

function handleBeforeUnload(e) {
  if (dirty.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}
onMounted(() => window.addEventListener('beforeunload', handleBeforeUnload))
onBeforeUnmount(() => window.removeEventListener('beforeunload', handleBeforeUnload))
</script>
