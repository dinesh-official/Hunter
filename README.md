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

---

## 3. Install & Configure MySQL

Hunter uses a MySQL database. Create a user and grant necessary privileges.

### Install MySql 
```bash
sudo apt install mysql-server
```
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
## To Allow Remote MySQL Access Bellow ( Optional ): 

### Then edit your MySQL config file (my.cnf or mysqld.cnf) and ensure:

Enable Remote Access to MySQL

## 📂 Step 1: Open the MySQL Configuration File

Edit the following file:

```bash
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

```bash
bind-address = 0.0.0.0
```

Then restart MySQL:

```bash
sudo systemctl restart mysql
```

To Allow Remote MySQL Access:
```sql
CREATE USER 'hunter'@'%' IDENTIFIED BY 'Dineshdb121@gmail.com';
GRANT ALL PRIVILEGES ON hunter.* TO 'hunter'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
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
sudo systemctl enable --now hunter
```

#### Status Hunter

```bash
sudo systemctl status hunter
```

### One-Click Installation (Optional)

```bash
wget https://github.com/dinesh-official/Hunter/releases/download/v2.0.0/Hunter.deb && \
sudo apt install ./Hunter.deb && \
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


