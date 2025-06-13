# Managing hunter.mail Table in MySQL

### 1. View the Data (Ordered by Creation Date)

```sql
SELECT *  FROM hunter.mail ORDER BY createdAd DESC;
```
### 2. Delete All Rows from the Table

```sql
TRUNCATE TABLE hunter.mail;
```
### 3. Reset the Primary Key Auto-Increment (Optional Redundancy)
```sql
 ALTER TABLE hunter.mail AUTO_INCREMENT = 1;
```
