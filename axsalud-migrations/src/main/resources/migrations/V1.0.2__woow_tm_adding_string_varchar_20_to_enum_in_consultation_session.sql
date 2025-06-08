-- ax_salud_woo_user
ALTER TABLE ax_salud_woo_user
MODIFY COLUMN location_offices VARCHAR(10);

ALTER TABLE ax_salud_woo_user
MODIFY COLUMN state VARCHAR(20);

ALTER TABLE ax_salud_woo_user
MODIFY COLUMN user_type VARCHAR(30);

-- consultation
ALTER TABLE consultation
MODIFY COLUMN status VARCHAR(30);

-- consultation_document
ALTER TABLE consultation_document
MODIFY COLUMN uploader_role VARCHAR(30);

-- consultation_message_entity
ALTER TABLE consultation_message_entity
MODIFY COLUMN status VARCHAR(30);

-- consultation_session
ALTER TABLE consultation_session
MODIFY COLUMN doctor_status VARCHAR(20);

ALTER TABLE consultation_session
MODIFY COLUMN patient_status VARCHAR(20);

ALTER TABLE consultation_session
MODIFY COLUMN status VARCHAR(30);
