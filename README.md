# GWT_GameRoom

## Local development

1. Start the backend: `mvn spring-boot:run -pl *-server -am`
2. Start the GWT code server: `mvn gwt:codeserver -pl *-client -am`
3. Open http://localhost:4100/

## Deploying to Raspberry Pi

Run `./deploy.sh` to build, upload, and restart the service.

### First-time Pi setup

Run the setup script with your public hostname (a domain you have on Cloudflare):

```bash
./setup-pi.sh gameroom.example.com
```

This will:
- Install and configure nginx
- Install cloudflared, log in to Cloudflare, create the tunnel, and start it as a service
- Write `/opt/gameroom/games.yaml` with the correct `baseUrl`

**Keezenspel config** — create `/opt/keezen/application-override.yaml` on the Pi (done by Keezenspel's `setup-pi.sh`):

```yaml
server:
  servlet:
    context-path: /keezen
```