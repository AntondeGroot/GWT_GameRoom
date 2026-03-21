#!/bin/bash
set -e

echo "🔨 Building..."
mvn clean package

echo "📦 Uploading..."
scp -i ~/.ssh/pi_deploy_key GameRoom-server/target/GameRoom.jar my-pi:/home/ubuntu/gameroom.jar

echo "📁 Installing..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo mkdir -p /opt/gameroom && sudo mv /home/ubuntu/gameroom.jar /opt/gameroom/gameroom.jar"

echo "⚙️  Ensuring systemd service exists..."
ssh -i ~/.ssh/pi_deploy_key my-pi "
if [ ! -f /etc/systemd/system/gameroom.service ]; then
  sudo tee /etc/systemd/system/gameroom.service > /dev/null << 'EOF'
[Unit]
Description=GameRoom
After=network.target

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /opt/gameroom/gameroom.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
  sudo systemctl daemon-reload
  sudo systemctl enable gameroom
fi"

echo "🔄 Restarting..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo systemctl restart gameroom"

echo "✅ Done."