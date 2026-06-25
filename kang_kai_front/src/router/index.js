import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/user/UserManage.vue'),
        meta: { title: '用户管理', requiresKang: true }
      },
      {
        path: 'projects',
        name: 'ProjectManage',
        component: () => import('@/views/project/ProjectManage.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'employees',
        name: 'EmployeeManage',
        component: () => import('@/views/employee/EmployeeManage.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: 'work-days',
        name: 'WorkDayManage',
        component: () => import('@/views/workday/WorkDayManage.vue'),
        meta: { title: '工日管理' }
      },
      {
        path: 'ledger',
        name: 'LedgerDetail',
        component: () => import('@/views/ledger/LedgerDetail.vue'),
        meta: { title: '台账明细' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.path === '/login') {
    if (userStore.isLoggedIn) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!userStore.isLoggedIn) {
      next('/login')
      ElMessage.warning('请先登录')
    } else if (to.meta.requiresKang && !userStore.isKang) {
      ElMessage.warning('仅管理员可访问')
      next('/dashboard')
    } else {
      next()
    }
  }
})

export default router
