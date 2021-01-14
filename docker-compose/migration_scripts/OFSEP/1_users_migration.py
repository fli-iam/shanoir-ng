#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(
        host        = os.environ.get("SRC_HOST")        or "localhost",
        user        = os.environ.get("SRC_USER")        or "root",
        password    = os.environ.get("SRC_PASSWORD")    or "",
        database    = os.environ.get("SRC_DATABASE")    or "ofsep",
        charset     = os.environ.get("SRC_CHARSET")     or "utf8")
targetConn = pymysql.connect(
        host        = os.environ.get("TGT_HOST")        or "localhost",
        user        = os.environ.get("TGT_USER")        or "users",
        password    = os.environ.get("TGT_PASSWORD")    or "password",
        database    = os.environ.get("TGT_DATABASE")    or "users",
        charset     = os.environ.get("TGT_CHARSET")     or "utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("######## CLEANING OF TARGET DB MS USERS: START ###################")
print("Delete users: start")
query = "DELETE FROM users"
targetCursor.execute(query)
targetConn.commit()
print("Delete users: end")


print("Delete role: start")
query = "DELETE FROM role"
targetCursor.execute(query)
targetConn.commit()
print("Delete role: end")
print("######## CLEANING OF TARGET DB MS USERS: STOP ###################")


print("######## IMPORTING INTO TARGET DB MS USERS: START ###################")
print("Import roles: start")    
sourceCursor.execute("SELECT ROLE_ID, DISPLAY_NAME, NAME FROM ROLE")

def changeRoleName(x):
    return {
        'adminRole': 'ROLE_ADMIN',
        'expertRole': 'ROLE_EXPERT',
        'userRole': 'ROLE_USER'
    }[x]

roles = []
guestRoleId = -1
medicalRoleId = -1
userRoleId = -1
for row in sourceCursor.fetchall():
    role = list(row);
    if "medicalRole" == role[2]:
        medicalRoleId = role[0]
    elif "guestRole" == role[2]:
        guestRoleId = role[0]
    else:
        if "userRole" == role[2]: userRoleId = role[0]
        role[2] = changeRoleName(role[2])
        roles.append(role)
query = "INSERT INTO role (id, display_name, name) VALUES (%s, %s, %s)"
targetCursor.executemany(query, roles)
targetConn.commit()
print("Import roles: end")


print("Import users: start")    
sourceCursor.execute("""SELECT USER_ID, CAN_ACCESS_TO_DICOM_ASSOCIATION, CREATED_ON, EMAIL, FIRST_NAME, LAST_LOGIN_ON, LAST_NAME, USERNAME, ROLE_ID,
    EXPIRATION_DATE, IS_FIRST_EXPIRATION_NOTIFICATION_SENT, IS_SECOND_EXPIRATION_NOTIFICATION_SENT FROM USERS""")
users = []
emails= []
for row in sourceCursor.fetchall():
    user = list(row);
    if medicalRoleId == user[8] or guestRoleId == user[8]:
        user[8] = userRoleId
    if row[3] not in emails: #clean up here on using email addresses only once
        emails.append(row[3])
        users.append(user)
query = """INSERT INTO users
    (id, can_access_to_dicom_association, creation_date, email, first_name, last_login, last_name, username, role_id,
    expiration_date, first_expiration_notification_sent, second_expiration_notification_sent, account_request_demand)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 0)"""
targetCursor.executemany(query, users)
targetConn.commit()
print("Import users: end")


sourceConn.close()
targetConn.close()
print("######## IMPORTING INTO TARGET DB MS USERS: FINISHED ###################")