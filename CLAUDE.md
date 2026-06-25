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

<!-- superpowers-zh:begin (do not edit between these markers) -->
# Superpowers-ZH 中文增强版

本项目已安装 superpowers-zh 技能框架（20 个 skills）。

## 核心规则

1. **收到任务时，先检查是否有匹配的 skill** — 哪怕只有 1% 的可能性也要检查
2. **设计先于编码** — 收到功能需求时，先用 brainstorming skill 做需求分析
3. **测试先于实现** — 写代码前先写测试（TDD）
4. **验证先于完成** — 声称完成前必须运行验证命令

## 可用 Skills

Skills 位于 `.claude/skills/` 目录，每个 skill 有独立的 `SKILL.md` 文件。

- **brainstorming**: 在任何创造性工作之前必须使用此技能——创建功能、构建组件、添加功能或修改行为。在实现之前先探索用户意图、需求和设计。
- **chinese-code-review**: 中文 review 沟通参考——话术模板、分级标注（必须修复/建议修改/仅供参考）、国内团队常见反模式应对。仅在用户显式 /chinese-code-review 时调用，不要根据上下文自动触发。
- **chinese-commit-conventions**: 中文 commit 与 changelog 配置参考——Conventional Commits 中文适配、commitlint/husky/commitizen 中文模板、conventional-changelog 中文配置。仅在用户显式 /chinese-commit-conventions 时调用，不要根据上下文自动触发。
- **chinese-documentation**: 中文文档排版参考——中英文空格、全半角标点、术语保留、链接格式、中文文案排版指北约定。仅在用户显式 /chinese-documentation 时调用，不要根据上下文自动触发。
- **chinese-git-workflow**: 国内 Git 平台配置参考——Gitee、Coding.net、极狐 GitLab、CNB 的 SSH/HTTPS/凭据/CI 接入差异与镜像同步配置。仅在用户显式 /chinese-git-workflow 时调用，不要根据上下文自动触发。
- **dispatching-parallel-agents**: 当面对 2 个以上可以独立进行、无共享状态或顺序依赖的任务时使用
- **executing-plans**: 当你有一份书面实现计划需要在单独的会话中执行，并设有审查检查点时使用
- **finishing-a-development-branch**: 当实现完成、所有测试通过、需要决定如何集成工作时使用——通过提供合并、PR 或清理等结构化选项来引导开发工作的收尾
- **mcp-builder**: MCP 服务器构建方法论 — 系统化构建生产级 MCP 工具，让 AI 助手连接外部能力
- **receiving-code-review**: 收到代码审查反馈后、实施建议之前使用，尤其当反馈不明确或技术上有疑问时——需要技术严谨性和验证，而非敷衍附和或盲目执行
- **requesting-code-review**: 完成任务、实现重要功能或合并前使用，用于验证工作成果是否符合要求
- **subagent-driven-development**: 当在当前会话中执行包含独立任务的实现计划时使用
- **systematic-debugging**: 遇到任何 bug、测试失败或异常行为时使用，在提出修复方案之前执行
- **test-driven-development**: 在实现任何功能或修复 bug 时使用，在编写实现代码之前
- **using-git-worktrees**: 当需要开始与当前工作区隔离的功能开发，或在执行实现计划之前使用——通过原生工具或 git worktree 回退机制确保隔离工作区存在
- **using-superpowers**: 在开始任何对话时使用——确立如何查找和使用技能，要求在任何响应（包括澄清性问题）之前调用 Skill 工具
- **verification-before-completion**: 在宣称工作完成、已修复或测试通过之前使用，在提交或创建 PR 之前——必须运行验证命令并确认输出后才能声称成功；始终用证据支撑断言
- **workflow-runner**: 在 Claude Code / OpenClaw / Cursor 中直接运行 agency-orchestrator YAML 工作流——无需 API key，使用当前会话的 LLM 作为执行引擎。当用户提供 .yaml 工作流文件或要求多角色协作完成任务时触发。
- **writing-plans**: 当你有规格说明或需求用于多步骤任务时使用，在动手写代码之前
- **writing-skills**: 当创建新技能、编辑现有技能或在部署前验证技能是否有效时使用

## 如何使用

当任务匹配某个 skill 时，使用 `Skill` 工具加载对应 skill 并严格遵循其流程。绝不要用 Read 工具读取 SKILL.md 文件。

如果你认为哪怕只有 1% 的可能性某个 skill 适用于你正在做的事情，你必须调用该 skill 检查。
<!-- superpowers-zh:end -->
