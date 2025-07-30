# Hunter Application Configuration Guide

This guide explains each configuration entry in the `application.properties` file used for the Hunter Spring Boot application.

---

## ✨ Application Basics

```properties
spring.application.name=Hunter
server.port=8080
```

* `spring.application.name`: The display name of the Spring Boot application.
* `server.port`: Port on which the service runs (default is 8080).

---

## 📂 MySQL Database (Primary)

```properties
spring.datasource.url=jdbc:mysql://<MYSQL_IP>:<PORT>/hunter?createDatabaseIfNotExist=true
spring.datasource.username=<MYSQL_USERNAME>
spring.datasource.password=<MYSQL_PASSWORD>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

* `spring.datasource.url`: JDBC connection string for MySQL.
* `spring.datasource.username`: MySQL username.
* `spring.datasource.password`: MySQL password.
* `spring.datasource.driver-class-name`: JDBC driver for MySQL.

---

## ⚡ ClickHouse Database

```properties
spring.datasource.clickhouse.url=jdbc:clickhouse://<CLICKHOUSE_IP>:8123/default
spring.datasource.clickhouse.username=<CLICKHOUSE_USERNAME>
spring.datasource.clickhouse.password=<CLICKHOUSE_PASSWORD>
spring.datasource.clickhouse.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver
```

* `spring.datasource.clickhouse.url`: ClickHouse database URL.
* `spring.datasource.clickhouse.username`: Username for ClickHouse.
* `spring.datasource.clickhouse.password`: Password for ClickHouse.
* `spring.datasource.clickhouse.driver-class-name`: JDBC driver for ClickHouse.

---

## 📧 SMTP Email Settings

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<YOUR_EMAIL>
spring.mail.password=<APP_PASSWORD>
spring.mail.to=<TO_EMAIL>
spring.mail.cc=<CC_EMAIL>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

* Configures SMTP email sending using Gmail.
* Includes authentication and TLS settings for secure email.

---

## ⏰ Task Scheduler (CRON Jobs)

```properties
schedule.ssh=0/5 * * * * *
schedule.outbound=0/5 * * * * ?
schedule.bandwidth=0/3 * * * * ?
schedule.open_ports=0/5 * * * * *
```

* Defines when to run SSH, outbound, bandwidth, and port scans.
* CRON expressions specify the interval.

---

## 🛡 SSH Scan Conditions

```properties
ssh.port=22
ssh.asn=132420
ssh.duration.hours=365
ssh.min-flow-count=10
ssh.response-count=1
ssh.password-based-condition=0
```

* Sets thresholds to detect suspicious SSH behavior.

---

## 🌐 Outbound Traffic Rules

```properties
outbound.port=0
outbound.dst-asn=132420
outbound.src-asn=132420
outbound.duration.hours=60000
outbound.client-country=IN
outbound.server-country=IN
outbound.response-count=1
outbound.min-ob-count=0
outbound.min-unique-server-ips=0
```

* Conditions to identify potentially harmful outbound traffic.

---

## 📡 Bandwidth Monitoring

```properties
bandwidth.src-asn=132420
bandwidth.duration.hours=60000
bandwidth.mingb=1
bandwidth.response-count=1
```

* Used to flag high-bandwidth activity from an ASN.

---

## 🔓 Open Ports Scanning

```properties
openports.dstAsn=132420
openports.intervalHours=240000
openports.minRequestCount=0
openports.response-count=1
openports.ports=5432,27017,6379,1433,11211,3306
```

* Scans specific ports for unauthorized access.
* Useful for detecting exposed databases.

---

## ✉ Email Alerts Setup

```properties
ssh.mail.type=SSH-PB
outbound.mail.type=OB
bandwidth.mail.type=BW
openports.mail.type=OP

ssh.mail.enabled=false
outbound.mail.enabled=false
bandwidth.mail.enabled=false
openports.mail.enabled=false

ssh.mail.skip-days-if-mailed=2
outbound.mail.skip-days-if-mailed=2
bandwidth.mail.skip-days-if-mailed=2
openports.mail.skipDaysIfMailed=2
```

* Sets how and when email alerts are sent.
* `enabled=false` disables email notifications.
* `skip-days-if-mailed` avoids resending the same alert too frequently.

---

## 📃 Logging

```properties
logging.file.name=/var/log/hunter/hunter.log
logging.level.root=INFO
```

* Path to store log files.
* Default logging level set to INFO.
