#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

targetConn = pymysql.connect(host="localhost", user="shanoir", password="shanoir", database="users", charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("Create roles: start")

query = "INSERT INTO role (display_name, name) VALUES ('Administrator', 'ROLE_ADMIN'), ('Expert', 'ROLE_EXPERT'), ('User', 'ROLE_USER')"

targetCursor.executemany(query, roles)
targetConn.commit()

print("Create roles: end")

print("Create users: start")
    
# TODO: fill query with admin info
query = """INSERT INTO users
    (can_access_to_dicom_association, creation_date, email, first_name, last_login, last_name, username, role_id,
    expiration_date, first_expiration_notification_sent, second_expiration_notification_sent, account_request_demand)
    VALUES (%s, CURRENT_TIMESTAMP, %s, %s, %s, %s, %s, 1, %s, %s, 0, 0, 0)"""

targetCursor.execute(query)
targetConn.commit()

print("Create users: end")

sourceConn.close()
targetConn.close()
