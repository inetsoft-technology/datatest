#!/bin/bash
set -e

apt-get update

apt-get install -y ca-certificates curl gnupg

# Ubuntu's default repos don't ship docker-compose-plugin, so add Docker's official apt repo
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  > /etc/apt/sources.list.d/docker.list

apt-get update

apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

systemctl enable docker
systemctl start docker

# 限制 docker 日志大小，避免磁盘写满
mkdir -p /etc/docker

cat > /etc/docker/daemon.json <<'EOF'
{
 "log-driver": "json-file",
 "log-opts": {
   "max-size": "50m",
   "max-file": "3"
 }
}
EOF

systemctl restart docker

mkdir -p /opt/db-server
cd /opt/db-server

cat > /opt/db-server/docker-compose.yml <<'EOF'
${docker_compose_content}
EOF

docker compose up -d

echo "DB Server Ready" > /opt/db-server/status.txt
