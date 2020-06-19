#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

conn = pymysql.connect(
        host        = os.environ.get("TGT_HOST")        or "localhost",
        user        = os.environ.get("TGT_USER")        or "user",
        password    = os.environ.get("TGT_PASSWORD")    or "password",
        database    = os.environ.get("TGT_DATABASE")    or "users",
        charset     = os.environ.get("TGT_CHARSET")     or "utf8")

cursor = conn.cursor()


print("Update users: start")
for query in """
    UPDATE users SET role_id=3 WHERE role_id=2;
    
    ALTER TABLE `users` MODIFY `account_request_demand` bit(1) DEFAULT NULL;
    ALTER TABLE `users` MODIFY `can_access_to_dicom_association` bit(1) DEFAULT NULL;
    ALTER TABLE `users` MODIFY `creation_date` date DEFAULT NULL;
    ALTER TABLE `users` MODIFY `expiration_date` date DEFAULT NULL;
    ALTER TABLE `users` MODIFY `extension_request_demand` bit(1) DEFAULT NULL;
    ALTER TABLE `users` MODIFY `extension_date` date DEFAULT NULL;
    ALTER TABLE `users` MODIFY `first_expiration_notification_sent` bit(1) DEFAULT NULL;
    ALTER TABLE `users` MODIFY `last_login` date DEFAULT NULL;
    ALTER TABLE `users` MODIFY `second_expiration_notification_sent` bit(1) DEFAULT NULL
        """.split(";"):
    cursor.execute(query)
conn.commit()
print("Update users: end")


print("Update roles: start")
cursor.execute("DELETE FROM role WHERE id=2")
cursor.execute("ALTER TABLE role DROP `access_level`")
conn.commit()
print("Update roles: end")


print("Add events: start")
cursor.execute("""
    CREATE TABLE `events` (
      `id` bigint(20) NOT NULL,
      `creation_date` datetime DEFAULT NULL,
      `event_type` varchar(255) DEFAULT NULL,
      `last_update` datetime DEFAULT NULL,
      `message` varchar(255) DEFAULT NULL,
      `object_id` varchar(255) DEFAULT NULL,
      `progress` float DEFAULT NULL,
      `status` int(11) NOT NULL,
      `user_id` bigint(20) DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `i_user_type` (`user_id`,`event_type`)
    ) ENGINE=InnoDB
""")
conn.commit()
print("Add events: end")


conn.close()
