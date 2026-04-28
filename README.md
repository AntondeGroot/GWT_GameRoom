# Raspberry Pi Game Room

This project was written in GWT in order to get a better understanding of it, since this is used by my employer.
<p align="center">
   <img width="500" alt="image" src="https://github.com/user-attachments/assets/7035146e-9a82-42ae-85d1-6f7a33bca0a2" />
</p>

<p align="center">
   <img width="500" alt="image" src="https://github.com/user-attachments/assets/54ef3dde-b30c-47b6-9569-fbcff16861f3" />
</p>

# Games

## 1. Tock / Keezenspel

<p align="center">
   <a href="https://github.com/AntondeGroot/GWT_Keezenspel"><strong>View Repository</strong></a><br><br>
   <img width="500" alt="image" src="https://github.com/user-attachments/assets/70e1ac8a-416e-4b8a-9593-5207419b20b4" />
</p>

## 2. Qwixx

<p align="center">
   <a href="https://github.com/AntondeGroot/Qwixx"><strong>View Repository</strong></a><br><br>
   <img width="500" alt="image" src="https://github.com/user-attachments/assets/c0e45ab8-958e-4570-a303-cb2ba45c19c4" />
</p>

## Local development

1. Start the backend: `mvn spring-boot:run -pl *-server -am`
2. Start the GWT code server: `mvn gwt:codeserver -pl *-client -am`
3. Open http://localhost:4100/

## Deploying to Raspberry Pi

Run `./deploy.sh` to build, upload, and restart the service.

### First-time Pi setup

Run the setup script:

```bash
./setup-pi.sh
```

This will:
- Install and configure nginx
- Install cloudflared and start it as a service
- Write `/opt/gameroom/games.yaml` with the correct `baseUrl`

The script reads `deploy.local.conf` from the project root to decide which tunnel mode to use:

**Without `deploy.local.conf`** — a temporary public URL is generated, like `https://projector-improving-expired-discussed.trycloudflare.com/`. It changes on every restart but works immediately for quick sessions with friends.

**With `deploy.local.conf`** — a named Cloudflare tunnel is used with a stable custom domain. Set it up first:

1. On the Pi, authenticate and create a named tunnel:
   ```bash
   cloudflared tunnel login
   cloudflared tunnel create gameroom
   cloudflared tunnel route dns gameroom gameroom.yourdomain.com
   ```
   `cloudflared tunnel login` prints a URL — open it on your PC to authenticate. The Pi terminal will then show the tunnel UUID.

2. Create `deploy.local.conf` in the project root (it is gitignored):
   ```
   DOMAIN=gameroom.yourdomain.com
   TUNNEL_NAME=gameroom
   TUNNEL_ID=your-tunnel-uuid
   CREDENTIALS_FILE=/home/ubuntu/.cloudflared/your-tunnel-uuid.json
   ```

3. Run `./setup-pi.sh`

**Keezenspel config** — create `/opt/keezen/application-override.yaml` on the Pi (done by Keezenspel's `setup-pi.sh`):

```yaml
server:
  servlet:
    context-path: /keezen
```

---

<details>
<summary>Preparing your Raspberry Pi from scratch</summary>

## 0. Prerequisite: have Linux installed

## 1. Install Java and basics

```bash
sudo apt update
sudo apt install openjdk-25-jdk ufw
java -version
```

## 2. Create deployment folders

```bash
sudo mkdir -p /opt/keezen /opt/gameroom
sudo chown -R ubuntu:ubuntu /opt/keezen /opt/gameroom
```

## 3. Set up SSH key-based deploy from your laptop

Generate a dedicated deploy key:

```bash
ssh-keygen -t ed25519 -f ~/.ssh/pi_deploy_key
```

Copy the public key to the Pi:

```bash
cat ~/.ssh/pi_deploy_key.pub
```

**On the Pi:**

```bash
mkdir -p ~/.ssh
chmod 700 ~/.ssh
nano ~/.ssh/authorized_keys
# paste the public key, then Ctrl+O → Enter → Ctrl+X
chmod 600 ~/.ssh/authorized_keys
```

Test the connection from your laptop:

```bash
ssh -i ~/.ssh/pi_deploy_key ubuntu@<RASPBERRYPI_IP>
```

Add an SSH alias in `~/.ssh/config`:

```
Host my-pi
    HostName <RASPBERRYPI_IP>
    User ubuntu
    IdentityFile ~/.ssh/pi_deploy_key
```

Then connect with `ssh my-pi`.

## 4. Create systemd services

**`/etc/systemd/system/game-server.service`**

```ini
[Unit]
Description=Java Game Server
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/game-server
ExecStart=/usr/bin/java -jar /opt/game-server/game-server.jar
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

**`/etc/systemd/system/gameroom.service`**

```ini
[Unit]
Description=Gameroom Server
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/gameroom
ExecStart=/usr/bin/java -jar /opt/gameroom/gameroom.jar
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Reload and enable both:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now game-server
sudo systemctl enable --now gameroom
sudo systemctl status game-server gameroom
```

## 5. Verify apps listen on the correct ports

Expected ports: `game-server → 4200`, `gameroom → 4100`

```bash
sudo ss -ltnp | grep -E '4100|4200'
```

You should see both apps listed as `LISTEN`.

> **Note:** If Spring Boot is packaging resources inside the JAR, use `getResourceAsStream()` instead of `new File(getResource(...).toURI())` — the latter fails inside a packaged JAR.

## 6. Deploy scripts

**`deploy-game-server.sh`**

```bash
#!/bin/bash
set -e

echo "Building..."
mvn clean package

echo "Uploading..."
scp -i ~/.ssh/pi_deploy_key GWT_Keezenspel-server/target/GWT_Keezenspel.jar my-pi:/home/ubuntu/game-server.jar

echo "Installing..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo mkdir -p /opt/game-server && sudo mv /home/ubuntu/game-server.jar /opt/game-server/game-server.jar"

echo "Restarting..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo systemctl restart game-server"

echo "Done."
```

**`deploy-gameroom.sh`**

```bash
#!/bin/bash
set -e

echo "Building..."
mvn clean package

echo "Uploading..."
scp -i ~/.ssh/pi_deploy_key GWT_Keezenspel-server/target/GWT_Keezenspel.jar my-pi:/home/ubuntu/gameroom.jar

echo "Installing..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo mkdir -p /opt/gameroom && sudo mv /home/ubuntu/gameroom.jar /opt/gameroom/gameroom.jar"

echo "Restarting..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo systemctl restart gameroom"

echo "Done."
```

Make both scripts executable:

```bash
chmod +x deploy-game-server.sh deploy-gameroom.sh
```

## 7. Check logs

```bash
# Follow live
journalctl -u game-server -f
journalctl -u gameroom -f

# Last 100 lines (no pager)
journalctl -u game-server -n 100 --no-pager
journalctl -u gameroom -n 100 --no-pager
```

## 8. Firewall

```bash
sudo ufw allow OpenSSH
sudo ufw allow 4100/tcp
sudo ufw allow 4200/tcp
sudo ufw enable
sudo ufw status
```

## 9. Test on your local network first

From another device on the same network:

```bash
curl http://<PI_IP>:4100
curl http://<PI_IP>:4200
```

If it fails:
- Make sure both devices are on the same network (not guest Wi-Fi)
- Verify the Pi's IP with `hostname -I`

## 10. Why it may still fail from outside your house (CGNAT)

Even if the app works locally it may be unreachable from the internet. This happens when your ISP uses CGNAT: the public IP seen on the internet differs from your router's WAN IP, which makes port forwarding unreliable. The solution is a tunnel — see step 11.

## 11. Expose publicly with Cloudflare Tunnel

Install cloudflared on the Pi (ARM64):

```bash
wget https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-arm64.deb
sudo dpkg -i cloudflared-linux-arm64.deb
sudo apt-get install -f
cloudflared --version
```

Start a temporary tunnel:

```bash
cloudflared tunnel --url http://localhost:4100  # gameroom
cloudflared tunnel --url http://localhost:4200  # game-server
```

This gives a public `trycloudflare.com` URL immediately — no domain needed. It bypasses CGNAT but the URL is temporary and changes on restart.

## 12. Caddy (optional, if you have a domain)

Caddy can act as a reverse proxy in front of both apps. If you have a domain with proper public routing, configure it like this:

```
game.example.com {
    reverse_proxy 127.0.0.1:4200
}

gameroom.example.com {
    reverse_proxy 127.0.0.1:4100
}
```

## 13. Useful commands

```bash
# Check what's listening
sudo ss -ltnp | grep java

# Service management
sudo systemctl status game-server gameroom
sudo systemctl restart game-server gameroom

# Live logs
journalctl -u game-server -f
journalctl -u gameroom -f
```

</details>
