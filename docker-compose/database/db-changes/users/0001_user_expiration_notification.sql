UPDATE users SET first_expiration_notification_sent = false WHERE first_expiration_notification_sent IS null;
UPDATE users SET second_expiration_notification_sent = false WHERE second_expiration_notification_sent IS null;

ALTER TABLE users MODIFY first_expiration_notification_sent bit(1) NOT NULL DEFAULT b'0';
ALTER TABLE users MODIFY second_expiration_notification_sent bit(1) NOT NULL DEFAULT b'0';

UPDATE users SET first_expiration_notification_sent = false, second_expiration_notification_sent = false
WHERE first_expiration_notification_sent = true
AND second_expiration_notification_sent = true
AND expiration_date > now();