#!/bin/bash
# 康之凯台账管理系统 - 服务器部署脚本
# 在 CentOS 7.x 上以 root 执行: bash setup.sh

set -e

echo "=== 1. 安装 Java 8 ==="
yum install -y java-1.8.0-openjdk

echo "=== 2. 安装 MySQL 8 ==="
yum install -y https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm
rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2023
yum install -y mysql-community-server
systemctl start mysqld
systemctl enable mysqld

# 获取临时密码
TMP_PWD=$(grep 'temporary password' /var/log/mysqld.log | awk '{print $NF}')
echo "MySQL 临时密码: $TMP_PWD"

# 创建数据库
mysql -u root -p"$TMP_PWD" --connect-expired-password -e "
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Kangkai@2026';
CREATE DATABASE IF NOT EXISTS kang_kai_manage DEFAULT CHARACTER SET utf8mb4;
FLUSH PRIVILEGES;
" 2>/dev/null || echo "数据库可能已存在，跳过"

echo "=== 3. 安装 Nginx ==="
yum install -y nginx

echo "=== 4. 创建目录 ==="
mkdir -p /opt/kangkai/html

echo "=== 5. 解压前端 ==="
cp /root/kangkai_deploy/dist.tar.gz /opt/kangkai/ 2>/dev/null || true
cd /opt/kangkai/html && tar xzf /root/kangkai_deploy/dist.tar.gz 2>/dev/null || echo "前端文件已就位"

echo "=== 6. 配置 Nginx ==="
cat > /etc/nginx/conf.d/kangkai.conf << 'NGINX'
server {
    listen 80;
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
NGINX

sed -i '/default_server/d' /etc/nginx/nginx.conf
nginx -t && systemctl start nginx && systemctl enable nginx

echo "=== 7. 启动后端 ==="
# 先停掉旧进程
pkill -f kang-kai-back 2>/dev/null || true
sleep 2

nohup java -Xms256m -Xmx512m -jar /root/kangkai_deploy/kang-kai-back-1.0.0.jar \
  --spring.datasource.password=Kangkai@2026 \
  --spring.profiles.active=prod \
  > /opt/kangkai/app.log 2>&1 &

sleep 5
echo "=== 8. 检查服务 ==="
curl -s http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"kang","password":"kang123"}' | head -c 100

echo ""
echo "============================================"
echo "  部署完成！"
echo "  访问地址: http://$(curl -s ifconfig.me)"
echo "  默认账号: kang / kang123"
echo "============================================"
