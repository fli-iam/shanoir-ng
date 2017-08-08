#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(host=os.environ['DOCKER_PREFIX']+"-mysql", user="shanoir", password="shanoir", database="shanoirdb", charset="utf8")
targetConn = pymysql.connect(host="localhost", user="shanoir", password="shanoir", database="shanoir_ng_users", charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("Import roles: start")
    
sourceCursor.execute("SELECT ROLE_ID, ACCESS_LEVEL, DISPLAY_NAME, NAME FROM ROLE")

def changeRoleName(x):
    return {
        'adminRole': 'ROLE_ADMIN',
        'expertRole': 'ROLE_EXPERT',
        'guestRole': 'ROLE_GUEST',
        'userRole': 'ROLE_USER'
    }[x]

roles = []
medicalRoleId = -1
userRoleId = -1
for row in sourceCursor.fetchall():
    role = list(row);
    if "medicalRole" != role[3]:
        if "userRole" == role[3]: userRoleId = role[0]
        role[3] = changeRoleName(role[3])
        roles.append(role)
    else:
        medicalRoleId = role[0]

query = "INSERT INTO role (id, access_level, display_name, name) VALUES (%s, %s, %s, %s)"

targetCursor.executemany(query, roles)
targetConn.commit()

print("Import roles: end")

print("Import users: start")
    
sourceCursor.execute("""SELECT USER_ID, CAN_ACCESS_TO_DICOM_ASSOCIATION, CREATED_ON, EMAIL, FIRST_NAME, LAST_LOGIN_ON, LAST_NAME, USERNAME, ROLE_ID,
    EXPIRATION_DATE, IS_FIRST_EXPIRATION_NOTIFICATION_SENT, IS_SECOND_EXPIRATION_NOTIFICATION_SENT, false FROM USERS""")

users = []
emails= []
for row in sourceCursor.fetchall():
    user = list(row);
    if medicalRoleId == user[8]:
        user[8] = userRoleId
    if row[3] not in emails:
        emails.append(row[3])
        users.append(user)

query = """INSERT INTO users
    (id, can_access_to_dicom_association, creation_date, email, first_name, last_login, last_name, username, role_id,
    expiration_date, is_first_expiration_notification_sent, is_second_expiration_notification_sent, account_request_demand)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""

targetCursor.executemany(query, users)
targetConn.commit()

print("Import users: end")

sourceConn.close()
targetConn.close()
