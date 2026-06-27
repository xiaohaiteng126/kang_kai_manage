# 康之凯台账管理系统 — 部署文档

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| 操作系统 | CentOS 7.x | 推荐 7.6 或 7.9 |
| Java | 8 | OpenJDK 1.8+ |
| MySQL | 8.0 | 数据库端口 3306 |
| Nginx | 1.x | 反向代理端口 80 |
| 内存 | ≥ 2GB | 建议 4GB |

## 部署包内容

```
kangkai_deploy/
├── kang-kai-back-1.0.0.jar    # 后端 Spring Boot JAR
├── dist.tar.gz                # 前端 Vue 构建产物
└── setup.sh                   # 自动部署脚本
```

---

## 方案一：自动部署（SSH 直连）

> 适用场景：你和部署者都在同一网络，或服务器已配置 SSH 密钥。

### 1. 打包

```bash
# 本地执行
cd kang_kai_back
mvn package -DskipTests

cd ../kang_kai_front
npm run build
tar czf ../kangkai_deploy/dist.tar.gz -C dist .

cd ..
ls kangkai_deploy/   # 确认三个文件都在
```

### 2. 上传并部署

```bash
# 上传
scp kangkai_deploy/* root@<服务器IP>:/root/

# SSH 连接
ssh root@<服务器IP>

# 执行部署脚本
bash /root/setup.sh
```

`setup.sh` 会自动完成：
- 安装 Java 8 / MySQL 8 / Nginx
- 初始化数据库，建表
- 解压前端到 /opt/kangkai/html/
- 配置 Nginx 反向代理
- 启动后端（JVM 256m-512m）

### 3. 验证

```bash
curl http://<服务器IP>/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"kang","password":"kang123"}'
# 返回 token 即成功
```

浏览器访问 `http://<服务器IP>`，默认账号 `kang / kang123`。

---

## 方案二：手动部署

> 适用场景：没有 SSH 权限，需要用云厂商网页终端或 VNC 操作。

### 第一步：登录服务器

腾讯云控制台 → 实例 → 远程连接 → 登录。

### 第二步：安装环境

```bash
# 1. Java 8
yum install -y java-1.8.0-openjdk
java -version    # 确认：openjdk version "1.8.0_xxx"

# 2. MySQL 8
yum install -y https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm
rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2023
yum install -y mysql-community-server
systemctl start mysqld
systemctl enable mysqld

# 获取 MySQL 临时密码
grep 'temporary password' /var/log/mysqld.log

# 用临时密码登录，修改密码
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY '你的新密码@2026';
CREATE DATABASE kang_kai_manage DEFAULT CHARACTER SET utf8mb4;
EXIT;

# 3. Nginx
yum install -y nginx
```

### 第三步：上传文件

用 **WinSCP** 或 **FileZilla** 连接到服务器 IP（SFTP 协议，22 端口），把以下文件拖到 `/root/`：

```
kang-kai-back-1.0.0.jar
dist.tar.gz
```

### 第四步：创建数据库表

把项目里的 `kang_kai_back/src/main/resources/db/init.sql` 文件内容复制，在服务器的 MySQL 中执行：

```bash
mysql -u root -p kang_kai_manage < /root/init.sql
# 或者通过 MySQL 客户端手动执行 SQL
```

### 第五步：部署前端

```bash
mkdir -p /opt/kangkai/html
tar xzf /root/dist.tar.gz -C /opt/kangkai/html/
chown -R nginx:nginx /opt/kangkai/html
```

### 第六步：配置 Nginx

```bash
cat > /etc/nginx/conf.d/kangkai.conf << 'EOF'
server {
    listen 80 default_server;
    server_name _;

    root /opt/kangkai/html;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF

# 确保 nginx.conf 中没有重复的 server 块
nginx -t && systemctl reload nginx && systemctl enable nginx
```

### 第七步：启动后端

```bash
nohup java -Xms256m -Xmx512m \
  -jar /root/kang-kai-back-1.0.0.jar \
  --spring.datasource.password=你的数据库密码 \
  > /opt/kangkai/app.log 2>&1 &

# 等 8 秒后测试
sleep 8
curl http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"kang","password":"kang123"}'
```

### 第八步：配置安全组

腾讯云控制台 → 安全组 → 入站规则，开放：

| 端口 | 协议 | 来源 | 说明 |
|------|------|------|------|
| 80 | TCP | 0.0.0.0/0 | Nginx HTTP |
| 22 | TCP | 0.0.0.0/0 | SSH（建议限制为你的 IP） |

> 3306（MySQL）**不要**对外开放，有安全风险。

### 第九步：验证

浏览器访问 `http://<服务器IP>`，用 `kang / kang123` 登录。

---

## Set 开机自启

```bash
# 后端 systemd 服务
cat > /etc/systemd/system/kangkai.service << 'EOF'
[Unit]
Description=Kang Kai Management
After=network.target mysqld.service

[Service]
User=root
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /root/kang-kai-back-1.0.0.jar --spring.datasource.password=你的数据库密码
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable kangkai
systemctl start kangkai
```

---

## 常用运维命令

```bash
# 查看后端状态
systemctl status kangkai

# 重启后端
systemctl restart kangkai

# 查看日志
tail -100 /opt/kangkai/app.log

# 查看内存
free -h

# 查看端口占用
netstat -tlnp | grep -E '80|8080'
```

## 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| 浏览器 403 | Nginx 默认 server 冲突或 SELinux | `setenforce 0`，检查 nginx.conf 无多余 server 块 |
| 后端 500 | 数据库密码不匹配 | 确认 `--spring.datasource.password=...` 参数正确 |
| `year_month` SQL 报错 | MySQL 关键字冲突 | 所有 SQL 中 `year_month` 必须加反引号 `` `year_month` `` |
| 内存不足 | 2GB 机器跑 Java+MySQL+Nginx | JVM 限制 `-Xmx512m`，MySQL `innodb_buffer_pool_size=128M` |

---

## 方案三：Docker 部署

> 适用场景：不想手动装环境，一行命令拉起所有服务。

### 部署包结构

```
kangkai_deploy/
├── docker-compose.yml       # 编排 MySQL + 后端 + Nginx
├── Dockerfile.backend       # 后端镜像
├── nginx.conf               # Nginx 配置（代理到容器内 backend:8080）
├── dist/                    # 前端构建产物（解压后的目录）
├── init.sql                 # 数据库建表脚本
└── kang-kai-back-1.0.0.jar  # 后端 JAR
```

### 1. 服务器安装 Docker

```bash
# CentOS 7
yum install -y yum-utils
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
systemctl start docker && systemctl enable docker

# Ubuntu 22.04
apt update && apt install -y docker.io docker-compose-v2
```

### 2. 准备文件

```bash
# 本地（打包后上传到服务器的 /root/kangkai-deploy/）
cd kangkai_deploy
tar xzf dist.tar.gz   # 解压前端，得到 dist/ 目录

# 把整个目录上传到服务器
scp -r . root@<服务器IP>:/root/kangkai-deploy/
```

上传后服务器上的目录内容：
```
/root/kangkai-deploy/
├── docker-compose.yml
├── Dockerfile.backend
├── nginx.conf
├── init.sql
├── kang-kai-back-1.0.0.jar
└── dist/
    ├── index.html
    └── assets/
```

### 3. 启动

```bash
cd /root/kangkai-deploy
docker compose up -d
```

三个容器自动构建并启动：

| 容器 | 端口 | 说明 |
|------|------|------|
| kangkai-mysql | 3306 | MySQL 8，数据库自动建表 |
| kangkai-backend | 8080 | Spring Boot，JVM 256m-512m |
| kangkai-nginx | 80 | 前端 + API 代理 |

首次启动需要拉取镜像（约 2-3 分钟），之后秒级启动。

### 4. 常用命令

```bash
# 查看运行状态
docker compose ps

# 查看日志
docker compose logs -f backend     # 后端日志
docker compose logs -f nginx       # Nginx 日志

# 重启某个服务
docker compose restart backend

# 重新构建并启动（代码有更新时）
docker compose up -d --build

# 停止所有服务
docker compose down

# 停止并删除数据（数据库也会删！）
docker compose down -v
```

### 5. 更新部署

```bash
# 代码更新后，上传新的 JAR 和 dist 文件
scp kang-kai-back-1.0.0.jar root@<IP>:/root/kangkai-deploy/
scp -r dist/* root@<IP>:/root/kangkai-deploy/dist/

# 服务器上重建并重启
ssh root@<IP>
cd /root/kangkai-deploy
docker compose up -d --build
docker compose restart    # 或者重建后端镜像生效新 JAR
```

### Docker vs 手动部署

| 对比 | Docker | 手动 |
|------|--------|------|
| 首次部署速度 | 需拉取镜像（2-3 分钟） | yum 安装（约 1 分钟） |
| 环境隔离 | ✅ 各服务独立容器 | ❌ 共享 OS 环境 |
| 迁移/重装 | `docker compose up -d` 一行 | 重新手动安装配置 |
| 内存占用 | 略高（~200MB 容器开销） | 更省 |
| 适合场景 | 开发/测试/快速重建 | 生产长期运行 |
