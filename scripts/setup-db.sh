#!/bin/bash
set -e
echo "DB Server (EC2 #2) 초기 설정"

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

# 3. Git 클론 (DB Compose 실행용)
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
    read -sp "MySQL Password Config: " MYSQL_PW
    echo ""
    cat <<EOF > "$REPO_DIR/.env"
MYSQL_ROOT_PASSWORD=${MYSQL_PW}
MYSQL_USERNAME=user
MYSQL_PASSWORD=${MYSQL_PW}
EOF
fi

echo "DB Server 초기 설정 완료. 터미널 재접속 후 'docker-compose -f docker-compose.db.yml up -d'를 실행하세요."
