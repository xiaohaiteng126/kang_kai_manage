-- 康之凯台账管理系统 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS kang_kai_manage
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE kang_kai_manage;

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(bcrypt)',
    real_name VARCHAR(50) DEFAULT '' COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 项目表
CREATE TABLE IF NOT EXISTS project (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 员工表
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    daily_wage DECIMAL(12,2) NOT NULL COMMENT '日资',
    age INT DEFAULT 0 COMMENT '年龄',
    gender VARCHAR(4) NOT NULL COMMENT '性别 男/女',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    bank_card VARCHAR(30) DEFAULT '' COMMENT '银行卡号',
    id_card VARCHAR(20) DEFAULT '' COMMENT '身份证号',
    address VARCHAR(200) DEFAULT '' COMMENT '地址',
    status VARCHAR(4) NOT NULL DEFAULT '在项' COMMENT '状态 在项/离项',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_project_id (project_id),
    KEY idx_name (name),
    KEY idx_phone (phone),
    KEY idx_id_card (id_card)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 工日记录表
CREATE TABLE IF NOT EXISTS work_day (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    `year_month` VARCHAR(7) NOT NULL COMMENT '年月',
    work_days DECIMAL(10,1) DEFAULT 0 COMMENT '工日',
    piece_pay DECIMAL(12,2) DEFAULT 0 COMMENT '计件薪资',
    loan DECIMAL(12,2) DEFAULT 0 COMMENT '借支',
    fine DECIMAL(12,2) DEFAULT 0 COMMENT '罚款',
    tools_other DECIMAL(12,2) DEFAULT 0 COMMENT '工具其他款项',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_project_month (employee_id, project_id, `year_month`),
    KEY idx_project_year_month (project_id, `year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工日记录表';

-- 台账签字表
CREATE TABLE IF NOT EXISTS ledger_sign (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    `year_month` VARCHAR(7) NOT NULL COMMENT '年月',
    signature VARCHAR(100) DEFAULT '' COMMENT '签字',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_project_month (employee_id, project_id, `year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='台账签字表';

-- 默认管理员用户 kang/kang123 由应用启动时 DataInitializer 自动创建
-- 此处不需要手动 INSERT
