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
      <el-form-item style="float: right">
        <el-button type="primary" @click="handleQuery" :disabled="!projectId || !startMonth || !endMonth">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- Charts -->
    <el-row :gutter="16" v-loading="loading">
      <el-col :span="12" style="margin-bottom: 16px">
        <el-card shadow="never">
          <template #header>工日排行</template>
          <div ref="workDaysBarChart" style="height: 280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12" style="margin-bottom: 16px">
        <el-card shadow="never">
          <template #header>计件占比</template>
          <div ref="workDaysPieChart" style="height: 280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12" style="margin-bottom: 0">
        <el-card shadow="never">
          <template #header>罚款占比</template>
          <div ref="fineChart" style="height: 280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12" style="margin-bottom: 0">
        <el-card shadow="never">
          <template #header>借支占比</template>
          <div ref="loanChart" style="height: 280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && stats.length === 0" description="请选择项目后查询" />
  </el-card>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { listProjects } from '@/api/project'
import { getStatistics } from '@/api/statistics'

const projects = ref([])
const projectId = ref('')
const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')
const startMonth = ref(`${currentYear}-${currentMonth}`)
const endMonth = ref(`${currentYear}-${currentMonth}`)
const loading = ref(false)
const stats = ref([])

// Chart refs
const workDaysBarChart = ref(null)
const workDaysPieChart = ref(null)
const fineChart = ref(null)
const loanChart = ref(null)

let chartInstances = []

onMounted(async () => {
  const res = await listProjects()
  projects.value = res.data || []
  if (projects.value.length > 0) {
    projectId.value = projects.value[0].id
    handleQuery()
  }
})

async function handleQuery() {
  loading.value = true
  try {
    const res = await getStatistics({
      projectId: projectId.value,
      startMonth: startMonth.value,
      endMonth: endMonth.value
    })
    stats.value = res.data?.stats || []
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

const lightColors = ['#91cc75', '#73c0de', '#fac858', '#ee6666', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']

function topN(arr, key, n) {
  const sorted = [...arr].sort((a, b) => Number(b[key] || 0) - Number(a[key] || 0))
  const top = sorted.slice(0, n)
  const rest = sorted.slice(n)
  const result = top.map(s => ({ name: s.employeeName, value: Number(s[key] || 0) }))
  if (rest.length > 0) {
    const restSum = rest.reduce((sum, s) => sum + Number(s[key] || 0), 0)
    if (restSum > 0) result.push({ name: `其他(${rest.length}人)`, value: restSum })
  }
  return result
}

function topNBar(arr, n) {
  return [...arr].sort((a, b) => Number(b.totalWorkDays || 0) - Number(a.totalWorkDays || 0)).slice(0, n)
}

function renderCharts() {
  disposeCharts()
  if (!stats.value.length) return

  const pieTooltipDay = { trigger: 'item', formatter: '{b}: {c} 天 ({d}%)' }
  const pieTooltipMoney = { trigger: 'item', formatter: '{b}: {c} 元 ({d}%)' }

  // 1. Work Days Bar (工日排行) - top 10
  const top10 = topNBar(stats.value, 10)
  const barSeriesData = top10.reverse().map((s, i) => ({
    value: Number(s.totalWorkDays || 0),
    itemStyle: { color: lightColors[i % lightColors.length], borderRadius: [0, 4, 4, 0] }
  }))
  const barChart = echarts.init(workDaysBarChart.value)
  barChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}: {c} 天' },
    grid: { left: '3%', right: '10%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', name: '天', axisLabel: { fontSize: 11 } },
    yAxis: { type: 'category', data: top10.map(s => s.employeeName), axisLabel: { fontSize: 11 } },
    series: [{
      type: 'bar', data: barSeriesData,
      label: { show: true, position: 'right', fontSize: 12, formatter: '{c} 天' }
    }]
  })
  chartInstances.push(barChart)

  // 2. Piece Pay Pie (计件占比) - top 8 + 其他
  const wdChart = echarts.init(workDaysPieChart.value)
  wdChart.setOption({
    tooltip: pieTooltipMoney, color: lightColors,
    legend: { orient: 'vertical', right: 5, top: 'center', type: 'scroll', textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['38%', '50%'],
      label: { formatter: '{b}\n{d}%', fontSize: 11 },
      data: topN(stats.value, 'totalPiecePay', 8)
    }]
  })
  chartInstances.push(wdChart)

  // 3. Fine Pie - top 8 + 其他
  const fChart = echarts.init(fineChart.value)
  fChart.setOption({
    tooltip: pieTooltipMoney, color: lightColors,
    legend: { orient: 'vertical', right: 5, top: 'center', type: 'scroll', textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['38%', '50%'],
      label: { formatter: '{b}\n{d}%', fontSize: 11 },
      data: topN(stats.value, 'totalFine', 8)
    }]
  })
  chartInstances.push(fChart)

  // 4. Loan Pie - top 8 + 其他
  const lChart = echarts.init(loanChart.value)
  lChart.setOption({
    tooltip: pieTooltipMoney, color: lightColors,
    legend: { orient: 'vertical', right: 5, top: 'center', type: 'scroll', textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['38%', '50%'],
      label: { formatter: '{b}\n{d}%', fontSize: 11 },
      data: topN(stats.value, 'totalLoan', 8)
    }]
  })
  chartInstances.push(lChart)

  // Responsive resize
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  chartInstances.forEach(c => c.resize())
}

function disposeCharts() {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(c => c.dispose())
  chartInstances = []
}
</script>
