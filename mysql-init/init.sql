CREATE DATABASE IF NOT EXISTS parceldb;
CREATE DATABASE IF NOT EXISTS courierdb;
CREATE DATABASE IF NOT EXISTS deliverydb;
CREATE DATABASE IF NOT EXISTS authdb;

-- Application user (password overridden by MYSQL_PASSWORD in compose if needed)
CREATE USER IF NOT EXISTS 'courier'@'%' IDENTIFIED BY 'courier';
GRANT ALL PRIVILEGES ON parceldb.* TO 'courier'@'%';
GRANT ALL PRIVILEGES ON courierdb.* TO 'courier'@'%';
GRANT ALL PRIVILEGES ON deliverydb.* TO 'courier'@'%';
GRANT ALL PRIVILEGES ON authdb.* TO 'courier'@'%';
FLUSH PRIVILEGES;
