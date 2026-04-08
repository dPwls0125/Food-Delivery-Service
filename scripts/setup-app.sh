#!/bin/bash
set -e
echo "🚀 App Server (EC2 #1) 초기 설정"

# 1. Swap 설정
if [ ! -f /swapfile ]; then
    sudo dd if=/dev/zero of=/swapfile bs=128M count=16
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
fi

# 2. Docker & Docker Compose 설치
if ! command -v docker &> /dev/null; then
    sudo apt-get update -y && sudo apt-get install -y docker.io
    sudo usermod -aG docker $USER
fi
if ! command -v docker-compose &> /dev/null; then
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# 3. Git 클론
REPO_DIR="$HOME/Food-Delivery-Service"
if ! command -v git &> /dev/null; then sudo apt-get install -y git; fi
if [ -d "$REPO_DIR" ]; then
    cd "$REPO_DIR" && git pull origin feat/infra
else
    cd "$HOME" && git clone -b feat/infra https://github.com/dPwls0125/Food-Delivery-Service.git
    cd "$REPO_DIR"
fi

# 4. .env 생성
if [ ! -f "$REPO_DIR/.env" ]; then
    read -p "DB Server Private IP (EC2 #2 IP): " DB_IP
    read -sp "MySQL Password: " MYSQL_PW
    echo ""
    cat <<EOF > "$REPO_DIR/.env"
MYSQL_USERNAME=user
MYSQL_PASSWORD=${MYSQL_PW}
DATABASE_HOST=${DB_IP}
PAYMENT_DB_HOST=${DB_IP}
REDIS_HOST=${DB_IP}
SPRING_PROFILE=prod
EOF
fi

echo "✅ App Server 초기 설정이 완료되었습니다. 터미널 재접속 후 'docker-compose up -d --build'를 실행하세요."
