# 康之凯台账管理系统

## 技术栈

- **后端**: Spring Boot 2.7 + MyBatis-Plus 3.5 + Spring Security + JWT + EasyExcel
- **前端**: Vue 3 + Vite + Element Plus + Pinia + Axios + xlsx
- **数据库**: MySQL 8.0

## 项目结构

```
kang_kai_manage/
├── kang_kai_back/                          # 后端 Spring Boot
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/kangkai/
│       │   ├── KangKaiApplication.java     # 启动类
│       │   ├── common/                      # Result, BusinessException
│       │   ├── config/                      # SecurityConfig, CorsConfig, MyBatisPlusConfig, DataInitializer
│       │   ├── controller/                  # REST 控制器
│       │   ├── dto/                         # 请求 DTO
│       │   ├── entity/                      # 数据库实体 (SysUser, Project, Employee, WorkDay, LedgerSign)
│       │   ├── mapper/                      # MyBatis Mapper 接口
│       │   ├── security/                    # JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl
│       │   ├── service/                     # 服务接口 + impl/
│       │   ├── utils/                       # DateUtils, ExcelExportUtil
│       │   └── vo/                          # 响应 VO
│       └── resources/
│           ├── application.yml              # 数据库/JWT 配置
│           ├── db/init.sql                  # 建库建表脚本
│           └── mapper/WorkDayMapper.xml     # 工日批量 upsert SQL
│
└── kang_kai_front/                         # 前端 Vue 3
    ├── vite.config.js                       # Vite 配置 + /api 代理
    └── src/
        ├── api/                             # axios 封装 (request.js) + 各模块 API
        ├── router/index.js                  # 路由 + 权限守卫
        ├── stores/user.js                   # Pinia 用户状态 (token, isKang)
        ├── views/                           # 页面组件
        │   ├── Layout.vue                   # 侧边栏 + 顶栏布局
        │   ├── Login.vue
        │   ├── Dashboard.vue
        │   ├── project/ProjectManage.vue
        │   ├── user/UserManage.vue          # 仅 kang 用户可见
        │   ├── employee/EmployeeManage.vue
        │   ├── workday/WorkDayManage.vue
        │   └── ledger/LedgerDetail.vue
        └── style.css                        # 全局样式
```

## 数据库表

| 表 | 说明 | 关键字段 |
|---|------|---------|
| `sys_user` | 系统用户 | username(UNIQUE), password(bcrypt) |
| `project` | 项目 | project_name |
| `employee` | 员工 | daily_wage, gender, status(在项/离项), project_id |
| `work_day` | 工日记录 | employee_id, project_id, \`year_month\`, work_days, piece_pay, loan, fine, tools_other, remark |
| `ledger_sign` | 台账签字 | employee_id, project_id, \`year_month\`, signature, remark |

> \`year_month\` 必须用反引号，因为 YEAR_MONTH() 是 MySQL 函数名。

## API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 → token |
| POST | `/api/auth/change-password` | 修改密码 |
| GET | `/api/auth/info` | 当前用户信息 |
| GET/POST/PUT/DELETE | `/api/users[/{id}]` | 用户管理（仅 kang） |
| GET/POST/PUT/DELETE | `/api/projects[/{id}]` | 项目管理 |
| GET/POST/PUT/DELETE | `/api/employees[/{id}]` | 员工管理（projectId 必填） |
| GET | `/api/work-days` | 工日查询（projectId, startMonth, endMonth） |
| POST | `/api/work-days/batch` | 工日批量保存（upsert） |
| GET | `/api/ledger` | 台账明细查询 |
| GET | `/api/ledger/export` | 台账 Excel 导出 |
| POST | `/api/ledger/sign` | 台账备注保存 |

## 注意事项

- 默认管理员：`kang` / `kang123`（应用首次启动时自动创建）
- `year_month` 列在 SQL 中必须用反引号包裹
- JDBC URL 中 `characterEncoding=UTF-8`（不是 utf8mb4）
- 金额字段统一用 `BigDecimal`，前端 `el-input-number :precision="2"`
- `SysUser.password` 使用 `@JsonProperty(access = WRITE_ONLY)`，可接收不可输出
- WorkDay 批量保存使用 `INSERT ... ON DUPLICATE KEY UPDATE`
- 工日管理和台账明细只显示"在项"员工

## 启动

```bash
# 后端
cd kang_kai_back
mvn spring-boot:run

# 前端
cd kang_kai_front
npm run dev
```
