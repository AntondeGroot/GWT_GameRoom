#!/bin/bash
ssh -i ~/.ssh/pi_deploy_key my-pi 'journalctl -u cloudflared | grep -o "https://[^/ ]*trycloudflare\.com" | tail -1'