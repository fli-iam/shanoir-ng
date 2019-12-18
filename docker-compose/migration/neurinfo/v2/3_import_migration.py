#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(host="mysql", user="shanoir", password="shanoir", database="shanoirdb", charset="utf8")
targetConn = pymysql.connect(host="localhost", user="shanoir", password="shanoir", database="import", charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("Import nifticonverter: start")
    
sourceCursor.execute("SELECT NIFTI_CONVERTER_ID, COMMENT, IS_ACTIVE, NAME, REF_NIFTI_CONVERTER_TYPE_ID FROM NIFTI_CONVERTER")

query = "INSERT INTO nifticonverter (id, comment, is_active, name, nifti_converter_type) VALUES (%s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import nifticonverter: end")

print("Import study_user: start")
    
sourceCursor.execute("""SELECT rsu.REL_STUDY_USER_ID, rsu.IS_RECEIVE_ANONYMIZATION_REPORT, rsu.IS_RECEIVE_NEW_IMPORT_REPORT, rsu.STUDY_ID, rsu.USER_ID, u.USERNAME 
	FROM REL_STUDY_USER rsu JOIN USERS u ON rsu.USER_ID = u.USER_ID""")

query = "INSERT INTO study_user (id, receive_anonymization_report, receive_new_import_report, study_id, user_id, user_name) VALUES (%s, %s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import study_user: end")

print("Import study_user_study_user_rights: start")
    
sourceCursor.execute("SELECT REL_STUDY_USER_ID, REF_STUDY_USER_TYPE_ID FROM REL_STUDY_USER rsu JOIN USERS u ON rsu.USER_ID = u.USER_ID")

study_user_rights = list()
for row in sourceCursor.fetchall():
    study_user_right = list(row);
    new_study_user_right = study_user_right.copy()
    if 4 != study_user_right[1]:
        new_study_user_right[1] = 3
        study_user_rights.append(new_study_user_right.copy())
        new_study_user_right[1] = 4
        study_user_rights.append(new_study_user_right.copy())
        if 5 != study_user_right[1]:
            new_study_user_right[1] = 2
            study_user_rights.append(new_study_user_right.copy())
            if 3 != study_user_right[1]:
                new_study_user_right[1] = 1
                study_user_rights.append(new_study_user_right.copy())

query = "INSERT INTO study_user_study_user_rights (study_user_id, study_user_rights) VALUES (%s, %s)"

targetCursor.executemany(query, study_user_rights)
targetConn.commit()

print("Import study_user_study_user_rights: end")

sourceConn.close()
targetConn.close()
