-- Create database if not exists
CREATE DATABASE IF NOT EXISTS f1_champions;
USE f1_champions;

-- Grant privileges to the application user
GRANT ALL PRIVILEGES ON f1_champions.* TO 'f1_user'@'%';
FLUSH PRIVILEGES; 