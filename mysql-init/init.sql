CREATE DATABASE IF NOT EXISTS authdb;

-- Application user for auth-service only (password overridden by MYSQL_PASSWORD in compose if needed)
CREATE USER IF NOT EXISTS 'courier'@'%' IDENTIFIED BY 'courier';
GRANT ALL PRIVILEGES ON authdb.* TO 'courier'@'%';
FLUSH PRIVILEGES;
