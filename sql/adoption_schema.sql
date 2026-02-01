-- Adoption schema for MySQL

USE pet_life;

CREATE TABLE IF NOT EXISTS adoption (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  pet_id BIGINT UNSIGNED NOT NULL,
  applicant_user_id BIGINT UNSIGNED NULL,
  applicant_name VARCHAR(100) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  id_number VARCHAR(30) NULL,
  city VARCHAR(100) NOT NULL,
  address VARCHAR(255) NOT NULL,
  experience TEXT NULL,
  remark TEXT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  reviewer_id BIGINT UNSIGNED NULL,
  review_remark VARCHAR(255) NULL,
  reviewed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_adoption_pet_id (pet_id),
  KEY idx_adoption_status (status),
  KEY idx_adoption_applicant_user_id (applicant_user_id),
  CONSTRAINT fk_adoption_pet_id
    FOREIGN KEY (pet_id) REFERENCES pet (id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_permission (code, name, type, method, path, sort, status)
VALUES
  ('adoption:read', 'Adoption Read', 'API', 'GET', '/api/adoptions', 210, 1),
  ('adoption:create', 'Adoption Create', 'API', 'POST', '/api/adoptions', 220, 1),
  ('adoption:review', 'Adoption Review', 'API', 'POST', '/api/adoptions/{id}/review', 230, 1)
ON DUPLICATE KEY UPDATE code = code;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.code = 'ADMIN' AND p.code IN (
  'adoption:read',
  'adoption:create',
  'adoption:review'
)
ON DUPLICATE KEY UPDATE role_id = role_id;
