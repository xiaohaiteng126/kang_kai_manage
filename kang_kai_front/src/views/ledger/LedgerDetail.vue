<template>
  <el-card>
    <!-- Controls -->
    <el-form :inline="true" style="margin-bottom: 16px">
      <el-form-item label="所属项目" required>
        <el-select v-model="projectId" placeholder="请选择项目" style="width: 200px">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始月份">
        <el-date-picker v-model="startMonth" type="month" format="YYYY-MM" value-format="YYYY-MM" placeholder="开始月份" />
      </el-form-item>
      <el-form-item label="结束月份">
        <el-date-picker v-model="endMonth" type="month" format="YYYY-MM" value-format="YYYY-MM" placeholder="结束月份" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery" :disabled="!projectId || !startMonth || !endMonth">查询</el-button>
        <el-button type="warning" @click="handleExport" :disabled="!rows.length">导出Excel</el-button>
      </el-form-item>
    </el-form>

    <!-- Read-only Table -->
    <div style="overflow-x: auto" v-loading="loading">
      <el-table :data="pagedData" border v-if="months.length > 0" show-summary :summary-method="getSummaries"
        :header-cell-style="{ textAlign: 'center' }">
        <!-- Fixed left columns -->
        <el-table-column type="index" label="序号" width="60" fixed="left" />
        <el-table-column prop="employeeName" label="姓名" width="100" fixed="left" />
        <el-table-column label="总工日" width="90">
          <template #default="{ row }">{{ formatNum(row.totalWorkDays) }}</template>
        </el-table-column>
        <el-table-column label="日资" width="90">
          <template #default="{ row }">{{ formatNum(row.dailyWage) }}</template>
        </el-table-column>
        <el-table-column label="总计件" width="100">
          <template #default="{ row }">{{ formatNum(row.totalPiecePay) }}</template>
        </el-table-column>

        <!-- Dynamic month columns (read-only) -->
        <template v-for="month in months" :key="month">
          <el-table-column :label="month">
            <el-table-column label="工日" width="80">
              <template #default="{ row }">{{ formatNum(getMonthData(row, month).workDays) }}</template>
            </el-table-column>
            <el-table-column label="计件" width="90">
              <template #default="{ row }">{{ formatNum(getMonthData(row, month).piecePay) }}</template>
            </el-table-column>
            <el-table-column label="借支" width="90">
              <template #default="{ row }">{{ formatNum(getMonthData(row, month).loan) }}</template>
            </el-table-column>
            <el-table-column label="罚款" width="80">
              <template #default="{ row }">{{ formatNum(getMonthData(row, month).fine) }}</template>
            </el-table-column>
            <el-table-column label="工具/其他" width="100">
              <template #default="{ row }">{{ formatNum(getMonthData(row, month).toolsOther) }}</template>
            </el-table-column>
            <el-table-column label="备注" width="140">
              <template #default="{ row }">{{ getMonthData(row, month).remark || '' }}</template>
            </el-table-column>
          </el-table-column>
        </template>

        <!-- Trailing columns -->
        <el-table-column label="总借支" width="100">
          <template #default="{ row }">{{ formatNum(row.totalLoan) }}</template>
        </el-table-column>
        <el-table-column label="总罚款" width="90">
          <template #default="{ row }">{{ formatNum(row.totalFine) }}</template>
        </el-table-column>
        <el-table-column label="工具/其他" width="100">
          <template #default="{ row }">{{ formatNum(row.totalToolsOther) }}</template>
        </el-table-column>
        <el-table-column label="余额" width="110">
          <template #default="{ row }">{{ formatNum(row.balance) }}</template>
        </el-table-column>
        <el-table-column label="签字" width="120">
          <template #default="{ row }">{{ row.signature || '' }}</template>
        </el-table-column>
        <el-table-column label="备注" width="180">
          <template #default="{ row }">
            <el-input v-model="row.remark" size="small" placeholder="可编辑备注" @blur="handleRemarkBlur(row)" />
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px" v-if="rows.length > 0">
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
import { ref, computed, onMounted } from 'vue'
import { listProjects } from '@/api/project'
import { queryLedger, exportLedger, saveLedgerSign } from '@/api/ledger'
import { ElMessage } from 'element-plus'

const projects = ref([])
const projectId = ref('')
const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')
const startMonth = ref(`${currentYear}-01`)
const endMonth = ref(`${currentYear}-${currentMonth}`)
const loading = ref(false)
const months = ref([])
const rows = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const pagedData = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return rows.value.slice(start, start + pageSize.value)
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
    const res = await queryLedger({
      projectId: projectId.value,
      startMonth: startMonth.value,
      endMonth: endMonth.value
    })
    months.value = res.data.months || []
    rows.value = res.data.rows || []
    total.value = rows.value.length
    pageNum.value = 1
  } finally {
    loading.value = false
  }
}

function getMonthData(row, month) {
  if (!row.months) return {}
  return row.months.find(m => m.yearMonth === month) || {}
}

function formatNum(val) {
  if (val === null || val === undefined) return ''
  return Number(val).toFixed ? Number(val).toFixed(2) : val
}

async function handleExport() {
  try {
    const res = await exportLedger({
      projectId: projectId.value,
      startMonth: startMonth.value,
      endMonth: endMonth.value
    })
    // Extract filename from Content-Disposition or use default
    const disposition = res.headers?.['content-disposition'] || ''
    const match = disposition.match(/filename\*=UTF-8''(.+)/)
    const filename = match ? decodeURIComponent(match[1]) : '台账明细.xlsx'

    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    // handled
  }
}

async function handleRemarkBlur(row) {
  try {
    await saveLedgerSign({
      employeeId: row.employeeId,
      projectId: projectId.value,
      yearMonth: endMonth.value,
      remark: row.remark || '',
      signature: row.signature || ''
    })
  } catch (e) {
    // ignore
  }
}

function getSummaries() {
  const data = rows.value
  if (!data.length) return []

  const sums = ['合计', ''] // 序号, 姓名

  // 总工日, 日资(不合计), 总计件
  sums.push(data.reduce((s, r) => s + Number(r.totalWorkDays || 0), 0).toFixed(1))
  sums.push('')
  sums.push(data.reduce((s, r) => s + Number(r.totalPiecePay || 0), 0).toFixed(2))

  // Month sub-columns
  months.value.forEach(m => {
    sums.push(data.reduce((s, r) => {
      const md = (r.months || []).find(d => d.yearMonth === m)
      return s + Number(md ? md.workDays || 0 : 0)
    }, 0).toFixed(1))
    sums.push(data.reduce((s, r) => {
      const md = (r.months || []).find(d => d.yearMonth === m)
      return s + Number(md ? md.piecePay || 0 : 0)
    }, 0).toFixed(2))
    sums.push(data.reduce((s, r) => {
      const md = (r.months || []).find(d => d.yearMonth === m)
      return s + Number(md ? md.loan || 0 : 0)
    }, 0).toFixed(2))
    sums.push(data.reduce((s, r) => {
      const md = (r.months || []).find(d => d.yearMonth === m)
      return s + Number(md ? md.fine || 0 : 0)
    }, 0).toFixed(2))
    sums.push(data.reduce((s, r) => {
      const md = (r.months || []).find(d => d.yearMonth === m)
      return s + Number(md ? md.toolsOther || 0 : 0)
    }, 0).toFixed(2))
    sums.push('') // 月备注
  })

  // 总借支, 总罚款, 工具/其他, 余额, 签字, 备注
  sums.push(data.reduce((s, r) => s + Number(r.totalLoan || 0), 0).toFixed(2))
  sums.push(data.reduce((s, r) => s + Number(r.totalFine || 0), 0).toFixed(2))
  sums.push(data.reduce((s, r) => s + Number(r.totalToolsOther || 0), 0).toFixed(2))
  sums.push(data.reduce((s, r) => s + Number(r.balance || 0), 0).toFixed(2))
  sums.push('')
  sums.push('')

  return sums
}
</script>
