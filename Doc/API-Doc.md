

# Hunter API Documentation

> A high-performance network monitoring API to detect suspicious SSH, outbound, bandwidth, and open port activity.

---

## 🚀 Endpoints Overview

| Endpoint    | Description                               |
| ----------- | ----------------------------------------- |
| `/api/ssh`  | Detect suspicious SSH connection attempts |
| `/api/ob`   | Monitor outbound traffic anomalies        |
| `/api/bw`   | Identify top bandwidth-consuming IPs      |
| `/api/op`   | Detect exposed open ports from flow data  |
| `/api/mail` | View historical mail alerts               |

---

## 🔐 `/api/ssh` – SSH Attack Detection

Detects unusual SSH login attempts (e.g., brute-force or unauthorized logins).

### 🔧 Query Parameters

| Param | Type | Default  | Description                         |
| ----- | ---- | -------- | ----------------------------------- |
| `p`   | int  | `22`     | Destination port (usually SSH)      |
| `a`   | int  | `132420` | Your ASN (autonomous system number) |
| `h`   | int  | `600`    | Time window in minutes              |
| `fc`  | int  | `1`      | Minimum number of flows             |
| `rc`  | int  | `2`      | Max records to return               |
| `np`  | int  | `2`      | Filter no-password login attempts   |

### 🔗 Example

```
GET /api/ssh?p=22&a=132420&h=600&fc=5&rc=10&np=2
```

---

## 🌍 `/api/ob` – Outbound Traffic Monitoring

Detects outbound connections from internal IPs to foreign servers.

### 🔧 Query Parameters

| Param   | Type   | Default  | Description                   |
| ------- | ------ | -------- | ----------------------------- |
| `p`     | int    | `0`      | Destination port              |
| `sa`    | int    | `132420` | Destination ASN               |
| `ca`    | int    | `132420` | Source ASN                    |
| `h`     | int    | `600`    | Time window (minutes)         |
| `cctry` | string | `IN`     | Source country (client)       |
| `sctry` | string | `IN`     | Server country                |
| `rc`    | int    | `10`     | Max records                   |
| `obc`   | int    | `0`      | Outbound connection threshold |
| `usip`  | int    | `0`      | Unique server IPs threshold   |

### 🔗 Example

```
GET /api/ob?p=443&sa=132420&ca=132420&h=600&cctry=IN&sctry=CN&rc=5&obc=2&usip=1
```

---

## 📊 `/api/bw` – Bandwidth Usage

Lists top IPs consuming the most bandwidth in a given time.

### 🔧 Query Parameters

| Param   | Type | Default  | Description         |
| ------- | ---- | -------- | ------------------- |
| `h`     | int  | `600`    | Lookback in minutes |
| `sa`    | int  | `132420` | Destination ASN     |
| `rc`    | int  | `10`     | Max results         |
| `minGb` | int  | `1`      | Min bandwidth in GB |

### 🔗 Example

```
GET /api/bw?h=600&sa=132420&rc=10&minGb=5
```

---

## 🔓 `/api/op` – Open Ports Detection

Finds IPs with open ports exposed to the network.

### 🔧 Query Parameters

| Param             | Type      | Default  | Description                     |
| ----------------- | --------- | -------- | ------------------------------- |
| `dstAsn`          | int       | `132420` | Destination ASN                 |
| `dstPorts`        | list<int> | -        | Ports to filter (e.g. 22, 3389) |
| `minRequestCount` | int       | `0`      | Min flow requests seen          |
| `intervalHours`   | int       | `24`     | Time range                      |
| `limit`           | int       | `10`     | Max records                     |
| `notMailed`       | boolean   | `false`  | Exclude already-emailed results |

### 🔗 Example

```
GET /api/op?dstAsn=132420&dstPorts=22,3389&minRequestCount=5&intervalHours=24&limit=5&notMailed=true
```

---

## 📧 `/api/mail` – Mail Log Viewer

Fetch past mail alerts sent from the system.

### 🔧 Query Parameters

| Param | Type   | Description       |
| ----- | ------ | ----------------- |
| `vi`  | string | VM ID             |
| `vo`  | string | VM Owner          |
| `vip` | string | VM IP             |
| `vn`  | string | VM Name           |
| `mt`  | string | Mail Type         |
| `d`   | int    | Days to look back |

### 🔗 Example

```
GET /api/mail?vi=vm123&vip=10.0.0.5&d=2
```

---

## ✅ Tips

* All timestamps are based on the system time.
* Use `rc` or `limit` to control result size.
* Combine filters for more precise results (e.g., by ASN, port, time).
* API is read-only and safe to query without auth (if in a protected network).
