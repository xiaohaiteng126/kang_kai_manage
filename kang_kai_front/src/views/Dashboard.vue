<template>
  <div>
    <el-card>
      <h2>欢迎使用康之凯台账管理系统</h2>
      <p style="margin-top: 12px; color: #666">当前用户：{{ userStore.userInfo.realName }}（{{ userStore.userInfo.username }}）</p>
    </el-card>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>
