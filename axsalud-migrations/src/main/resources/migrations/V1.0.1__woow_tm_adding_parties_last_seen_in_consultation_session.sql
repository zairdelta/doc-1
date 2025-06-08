ALTER TABLE consultation_session
ADD COLUMN doctor_last_time_ping DATETIME(6) DEFAULT NULL,
ADD COLUMN patient_last_time_ping DATETIME(6) DEFAULT NULL;