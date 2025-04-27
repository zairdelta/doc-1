INSERT INTO service_provider (name, created_at, service_expiration, endpoint)
VALUES (
    'HealthConnect',
    NOW(),
    '2025-12-31',
    'https://localhost:8080/external_provider'
);

INSERT INTO woow_user (
    user_id,
    birth, name, user_name, last_name, country, password, email,
    accept_terms_and_conditions, mobile_phone, city, state, cp,
    address_line1, address_line2, user_active, phone_number_confirm,
    email_confirm, created_at, is_user_blocked, login_attempts, mfa
)
VALUES (
    1,
    '1995-06-15', 'master@example.com', 'master@example.com', 'masterLastName', 'MX',
     '$2a$10$nOyz3qX1lLYv9GOZcwBieeO1KSYDT6funrQx322uHIuX8LWY9XQQW',
      'master@example.com',
    'yes', '5551234567', 'CDMX', 'CDMX', '01234',
    'Av Reforma 123', 'Int 5', true, false,
    true, NOW(), 0, 0, 1
);

INSERT INTO ax_salud_woo_user
VALUES('1', NULL, 'HID-123', NULL, '21', 'OFFLINE', 'PATIENT', 1, NULL, NULL);

INSERT INTO user_roles (user_id, role)
VALUES
    (1, 'ADMIN');
