UPDATE users SET first_expiration_notification_sent = false, second_expiration_notification_sent = false
WHERE first_expiration_notification_sent = true
AND second_expiration_notification_sent = true
AND expiration_date > now();