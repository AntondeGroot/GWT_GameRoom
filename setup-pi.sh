#!/bin/bash
set -e

SSH="ssh -i ~/.ssh/pi_deploy_key my-pi"
SCP="scp -i ~/.ssh/pi_deploy_key"

echo "📁 Creating directory..."
$SSH "sudo mkdir -p /opt/gameroom"

echo "📦 Installing nginx..."
$SSH "sudo apt-get install -y nginx"

echo "⚙️  Configuring nginx..."
$SCP nginx.conf my-pi:/home/ubuntu/gameroom-nginx.conf
$SSH "sudo mv /home/ubuntu/gameroom-nginx.conf /etc/nginx/sites-available/gameroom && \
      sudo ln -sf /etc/nginx/sites-available/gameroom /etc/nginx/sites-enabled/gameroom && \
      sudo rm -f /etc/nginx/sites-enabled/default && \
      sudo fuser -k 80/tcp; sudo nginx -t && sudo systemctl enable nginx && sudo systemctl restart nginx"

echo "⚙️  Installing gameroom systemd service..."
$SSH "sudo tee /etc/systemd/system/gameroom.service > /dev/null << 'EOF'
[Unit]
Description=GameRoom
After=network.target

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /opt/gameroom/gameroom.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF"
$SSH "sudo systemctl daemon-reload && sudo systemctl enable gameroom"

echo "☁️  Installing cloudflared..."
$SSH "if ! command -v cloudflared &>/dev/null; then
  curl -fsSL https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-arm64 -o /tmp/cloudflared
  sudo mv /tmp/cloudflared /usr/local/bin/cloudflared
  sudo chmod +x /usr/local/bin/cloudflared
fi"

echo "⚙️  Installing cloudflared quick-tunnel systemd service..."
$SSH "sudo tee /etc/systemd/system/cloudflared.service > /dev/null << 'EOF'
[Unit]
Description=Cloudflare Quick Tunnel
After=network.target

[Service]
ExecStart=/usr/local/bin/cloudflared tunnel --url http://localhost:80
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF"
$SSH "sudo systemctl daemon-reload && sudo systemctl enable cloudflared && sudo systemctl start cloudflared"

echo "📝 Writing games config..."
$SCP GameRoom-server/src/main/resources/games.yaml my-pi:/home/ubuntu/games.yaml
$SSH "sudo mv /home/ubuntu/games.yaml /opt/gameroom/games.yaml"

echo ""
echo "✅ Pi setup complete."
echo "   Run ./deploy.sh to deploy the application."
echo ""
echo "   To find your public tunnel URL, run: ./get-tunnel-url.sh"