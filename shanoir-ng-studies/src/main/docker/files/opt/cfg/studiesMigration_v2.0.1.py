#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(host="mysql", user="shanoir", password="shanoir", database="shanoirdb", charset="utf8")
targetConn = pymysql.connect(host="localhost", user="shanoir", password="shanoir", database="shanoir_ng_studies", charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("Import protocol file path: start")
    
sourceCursor.execute("SELECT PATH, STUDY_ID FROM PROTOCOL_FILE_PATH")

query = "INSERT INTO protocol_file_path (path, study_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import protocol file path: end")

print("Import study_examination: start")
    
sourceCursor.execute("""SELECT EXAMINATION_ID, STUDY_ID FROM EXAMINATION""")

query = """INSERT INTO study_examination (examination_id, study_id) VALUES (%s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import study_examination: end")

print("Import group_of_subjects: start")
    
sourceCursor.execute("SELECT gof.GROUP_OF_SUBJECTS_ID, gof.GROUP_NAME, egof.STUDY_ID FROM GROUP_OF_SUBJECTS gof, EXPERIMENTAL_GROUP_OF_SUBJECTS egof")

query = "INSERT INTO group_of_subjects (id, dtype, group_name, study_id) VALUES (%s, 'EXPERIMENTAL', %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import group_of_subjects: end")

print("Import subject_group_of_subjects: start")
    
sourceCursor.execute("SELECT REL_SUBJECT_GROUP_OF_SUBJECTS_ID, GROUP_OF_SUBJECTS_ID, SUBJECT_ID FROM REL_SUBJECT_GROUP_OF_SUBJECTS")

query = "INSERT INTO subject_group_of_subjects (id, group_of_subjects_id, subject_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import subject_group_of_subjects: end")

print("Import coil: start")
    
sourceCursor.execute("SELECT COIL_ID, CENTER_ID, REF_COIL_TYPE_ID, MANUFACTURER_MODEL_ID, NAME, NUMBER_OF_CHANNELS, SERIAL_NUMBER FROM COIL")

query = "INSERT INTO coil (id, center_id, coil_type, manufacturer_model_id, name, number_of_channels, serial_number) VALUES (%s, %s, %s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import coil: end")

print("Import timepoint: start")
    
sourceCursor.execute("SELECT TIMEPOINT_ID, COMMENT, DAYS, NAME, RANK, STUDY_ID FROM TIMEPOINT")

query = "INSERT INTO timepoint (id, comment, days, name, rank, study_id) VALUES (%s, %s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import timepoint: end")

sourceConn.close()
targetConn.close()
