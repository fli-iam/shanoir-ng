#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(
        host        = os.environ.get("SRC_HOST")        or "localhost",
        user        = os.environ.get("SRC_USER")        or "root",
        password    = os.environ.get("SRC_PASSWORD")    or "",
        database    = os.environ.get("SRC_DATABASE")    or "neurinfo",
        charset     = os.environ.get("SRC_CHARSET")     or "utf8")
targetConn = pymysql.connect(
        host        = os.environ.get("TGT_HOST")        or "localhost",
        user        = os.environ.get("TGT_USER")        or "import",
        password    = os.environ.get("TGT_PASSWORD")    or "password",
        database    = os.environ.get("TGT_DATABASE")    or "import",
        charset     = os.environ.get("TGT_CHARSET")     or "utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()


print("Delete study_user_study_user_rights: start")
query = "DELETE FROM study_user_study_user_rights"
targetCursor.execute(query)
targetConn.commit()
print("Delete study_user_study_user_rights: end")


print("Delete study_user: start")
query = "DELETE FROM study_user"
targetCursor.execute(query)
targetConn.commit()
print("Delete study_user: end")


print("Import study_user: start")    
sourceCursor.execute("""SELECT rsu.REL_STUDY_USER_ID, rsu.IS_RECEIVE_ANONYMIZATION_REPORT, rsu.IS_RECEIVE_NEW_IMPORT_REPORT, rsu.STUDY_ID, rsu.USER_ID, u.USERNAME 
	FROM REL_STUDY_USER rsu JOIN USERS u ON rsu.USER_ID = u.USER_ID""")
query = "INSERT INTO study_user (id, receive_anonymization_report, receive_new_import_report, study_id, user_id, user_name) VALUES (%s, %s, %s, %s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import study_user: end")


print("Import study_user_study_user_rights: start")    
sourceCursor.execute("SELECT REL_STUDY_USER_ID, REF_STUDY_USER_TYPE_ID FROM REL_STUDY_USER rsu JOIN USERS u ON rsu.USER_ID = u.USER_ID")
study_user_rights_list = list()
for row in sourceCursor.fetchall():
    study_user_right = list(row);
    new_study_user_right1 = list(study_user_right)
    new_study_user_right2 = list(study_user_right)
    new_study_user_right3 = list(study_user_right)
    new_study_user_right4 = list(study_user_right)
    # 4 equals no right in sh-old, can not see or download, so do nothing here
    if 4 != study_user_right[1]:
        new_study_user_right1[1] = 3 # 3=can download in ng
        study_user_rights_list.append(new_study_user_right1)
        new_study_user_right2[1] = 4 # 4=can see all in ng
        study_user_rights_list.append(new_study_user_right2)
        # 5 equals can-see-download in sh-old
        if 5 != study_user_right[1]:
            new_study_user_right3[1] = 2 # 2=can import in ng
            study_user_rights_list.append(new_study_user_right3)
            # equals 1: is responsible
            if 3 != study_user_right[1]:
                new_study_user_right4[1] = 1 # 1=can administrate
                study_user_rights_list.append(new_study_user_right4)
query = "INSERT INTO study_user_study_user_rights (study_user_id, study_user_rights) VALUES (%s, %s)"
targetCursor.executemany(query, study_user_rights_list)
targetConn.commit()
print("Import study_user_study_user_rights: end")

sourceConn.close()
targetConn.close()
