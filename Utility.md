### Managing hunter.mail Table in MySQL

1. View the Data (Ordered by Creation Date)

```sql
SELECT *  FROM hunter.mail ORDER BY createdAd DESC;
```
2. Delete All Rows from the Table

```sql
TRUNCATE TABLE hunter.mail;
```
3. Reset the Primary Key Auto-Increment (Optional Redundancy)
```sql
 ALTER TABLE hunter.mail AUTO_INCREMENT = 1;
```


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
