# Hunter Installation Guide

## Hunter - Version 1 Installation Guide

A step-by-step guide to install and configure the **Hunter** Spring Boot application on an Ubuntu server.

---

## Table of Contents

1. [System Update](#1-system-update)
2. [Install Java](#2-install-java)
3. [Install & Configure MySQL](#3-install--configure-mysql)
4. [Install Hunter](#4-install-hunter)
5. [Check Logs](#5-check-logs)
6. [Other Configuration (Optional)](#6-other-configuration-optional)

---

## 1. System Update

Before beginning, make sure your system packages are up to date:

```bash
sudo apt update && sudo apt upgrade -y
```

---

## 2. Install Java

Hunter requires Java 24 to run.

### Download and Install Java 24

```bash
wget https://download.oracle.com/java/24/latest/jdk-24_linux-x64_bin.deb
sudo dpkg -i jdk-24_linux-x64_bin.deb
sudo apt-get install -f  # Fix dependencies
```

### Make Java 24 Default (Optional)

```bash
sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-24/bin/java 1
```

---

## 3. Install & Configure MySQL

Hunter uses a MySQL database. Create a user and grant necessary privileges.

### For Localhost Only

```sql
CREATE USER 'hunter'@'localhost' IDENTIFIED BY 'Dineshdb121@gmail.com';
GRANT ALL PRIVILEGES ON hunter.* TO 'hunter'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

### Verify MySQL User

```sql
SELECT user, host FROM mysql.user;
SHOW GRANTS FOR 'hunter'@'localhost';
```

---

## 4. Install Hunter

### Step-by-Step Installation

#### Download Hunter Package

```bash
wget https://github.com/dinesh-official/Hunter/releases/download/v1.0.0/Hunter.deb
```

#### Install Hunter

```bash
sudo apt install ./hunter.deb
```

#### Start Hunter

```bash
sudo systemctl enable --now hunter.service
```

#### Status Hunter

```bash
sudo systemctl status hunter.service
```

### One-Click Installation (Optional)

```bash
wget https://github.com/dinesh-official/Hunter/raw/main/hunter.deb && \
sudo apt install ./hunter.deb && \
sudo systemctl enable hunter.service && \
sudo systemctl start hunter.service
```

---

## Edit Configuration File

Modify the configuration file to suit your setup:

```bash
sudo nano /etc/hunter/hunter.properties
```

Save with `Ctrl+X`, then press `Y` and `Enter`.

---

## 5. Check Logs

### View Journal Logs

```bash
journalctl -u hunter.service
```

### Check the Execution Success Logs

```bash
tail -f /var/log/hunter/hunter.log
```

### Check the Execution Error Logs

```bash
tail -f /var/log/hunter/hunter-error.log
```

---

## 6. Other Configuration (Optional)

### Service File Customization

Edit the systemd service file if needed:

```bash
sudo nano /etc/systemd/system/hunter.service
```

```ini
[Unit]
Description=Hunter Spring Boot Service
After=network.target

[Service]
User=hunter
WorkingDirectory=/usr/lib/hunter
ExecStart=/usr/bin/java -jar /usr/lib/hunter/Hunter.jar --spring.config.location=/etc/hunter/hunter.properties
SuccessExitStatus=143
Restart=on-failure
RestartSec=5
StandardOutput=append:/var/log/hunter/hunter.log
StandardError=append:/var/log/hunter/hunter-error.log

[Install]
WantedBy=multi-user.target
```
