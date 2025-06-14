
CREATE INDEX idx_woowuser_username ON woow_user (user_name);
CREATE INDEX idx_woowuser_email ON woow_user (email);


CREATE INDEX idx_consultation_consultationid ON consultation (consultation_id);


CREATE INDEX idx_consultationsession_sessionid ON consultation_session (consultation_session_id);

CREATE INDEX idx_consultationmessage_sessionid ON Consultation_message_entity (consultation_session_id);
