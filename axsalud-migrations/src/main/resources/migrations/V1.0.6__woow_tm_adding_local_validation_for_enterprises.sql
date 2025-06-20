CREATE TABLE local_service_provider_user_entity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    hid VARCHAR(20) NULL,
    name VARCHAR(100) NULL,
    last_name VARCHAR(100) NULL,
    service_provider_id BIGINT NULL,
    user_valid INT NULL,
    created_at DATETIME NULL
);