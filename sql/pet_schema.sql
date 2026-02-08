-- Pet schema for MySQL

USE pet_life;

CREATE TABLE IF NOT EXISTS pet (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nickname VARCHAR(100) NOT NULL,
  breed VARCHAR(100) NULL,
  type VARCHAR(20) NOT NULL,
  gender VARCHAR(20) NULL,
  age INT NULL,
  city VARCHAR(100) NULL,
  address VARCHAR(255) NULL,
  detail TEXT NULL,
  image VARCHAR(255) NULL,
  image_urls TEXT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_pet_type (type),
  KEY idx_pet_status (status),
  KEY idx_pet_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_permission (code, name, type, method, path, sort, status)
VALUES
  ('pet:read', 'Pet Read', 'API', 'GET', '/api/pets', 110, 1),
  ('pet:create', 'Pet Create', 'API', 'POST', '/api/pets', 120, 1),
  ('pet:update', 'Pet Update', 'API', 'PUT', '/api/pets/{id}', 130, 1),
  ('pet:delete', 'Pet Delete', 'API', 'DELETE', '/api/pets/{id}', 140, 1),
  ('pet:upload', 'Pet Upload Image', 'API', 'POST', '/api/pets/images', 150, 1)
ON DUPLICATE KEY UPDATE code = code;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.code = 'ADMIN' AND p.code IN (
  'pet:read',
  'pet:create',
  'pet:update',
  'pet:delete',
  'pet:upload'
)
ON DUPLICATE KEY UPDATE role_id = role_id;
