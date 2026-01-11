-- RBAC schema for MySQL
-- Change database name if needed.

CREATE DATABASE IF NOT EXISTS pet_life
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE pet_life;

-- Users
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NULL,
  email VARCHAR(100) NULL,
  phone VARCHAR(20) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Roles
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Permissions
CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(100) NOT NULL,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(20) NOT NULL DEFAULT 'API',
  method VARCHAR(10) NULL,
  path VARCHAR(255) NULL,
  parent_id BIGINT UNSIGNED NULL,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_permission_code (code),
  KEY idx_sys_permission_parent_id (parent_id),
  CONSTRAINT fk_sys_permission_parent
    FOREIGN KEY (parent_id) REFERENCES sys_permission (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User-Role mapping
CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_sys_user_role_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_sys_user_role_role
    FOREIGN KEY (role_id) REFERENCES sys_role (id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role-Permission mapping
CREATE TABLE IF NOT EXISTS sys_role_permission (
  role_id BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_sys_role_permission_role
    FOREIGN KEY (role_id) REFERENCES sys_role (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_sys_role_permission_permission
    FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed data (replace password_hash with your own hash)
INSERT INTO sys_user (username, password_hash, nickname, status)
VALUES ('admin', 'admin123', 'Administrator', 1)
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO sys_role (code, name, description, status)
VALUES ('ADMIN', 'Administrator', 'Full access', 1)
ON DUPLICATE KEY UPDATE code = code;

INSERT INTO sys_permission (code, name, type, method, path, sort, status)
VALUES
  ('sys:user:read', 'User Read', 'API', 'GET', '/api/users', 10, 1),
  ('sys:user:create', 'User Create', 'API', 'POST', '/api/users', 20, 1),
  ('sys:user:update', 'User Update', 'API', 'PUT', '/api/users/{id}', 30, 1),
  ('sys:user:delete', 'User Delete', 'API', 'DELETE', '/api/users/{id}', 40, 1)
ON DUPLICATE KEY UPDATE code = code;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.code = 'ADMIN'
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.code = 'ADMIN' AND p.code IN (
  'sys:user:read',
  'sys:user:create',
  'sys:user:update',
  'sys:user:delete'
)
ON DUPLICATE KEY UPDATE role_id = role_id;
