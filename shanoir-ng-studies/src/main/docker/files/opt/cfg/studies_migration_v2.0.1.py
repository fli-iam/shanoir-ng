#!/usr/bin/python3

import os
import pymysql

sourceConn = pymysql.connect(host="mysql", user="shanoir", password="shanoir", database="shanoirdb", charset="utf8")
targetConn = pymysql.connect(host="localhost", user="shanoir", password="shanoir", database="shanoir_ng_studies", charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("Delete study_user: start")
    
query = "delete from study_user"

targetCursor.execute(query)
targetConn.commit()

print("Delete study_user: end")

print("Delete study_study_card: start")
    
query = "delete from study_study_card"

targetCursor.execute(query)
targetConn.commit()

print("Delete study_study_card: end")

print("Delete study_center: start")
    
query = "delete from study_center"

targetCursor.execute(query)
targetConn.commit()

print("Delete study_center: end")

print("Delete studies: start")
    
query = "delete from study"

targetCursor.execute(query)
targetConn.commit()

print("Delete studies: end")

print("Reimport studies: start")
    
sourceCursor.execute("""SELECT STUDY_ID, IS_CLINICAL, COORDINATOR_ID, IS_DOWNLOADABLE_BY_DEFAULT, END_DATE, IS_MONO_CENTER, NAME,
    START_DATE, REF_STUDY_STATUS_ID, REF_STUDY_TYPE_ID, IS_VISIBLE_BY_DEFAULT, IS_WITH_EXAMINATION FROM STUDY""")

query = """INSERT INTO study
    (id, clinical, coordinator_id, downloadable_by_default, end_date, mono_center, name, start_date, study_status, study_type, visible_by_default, with_examination)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Reimport studies: end")

print("Reimport study_center: start")
    
sourceCursor.execute("SELECT REL_STUDY_CENTER_ID, CENTER_ID, STUDY_ID FROM REL_STUDY_CENTER")

query = "INSERT INTO study_center (id, center_id, study_id) VALUES (%s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Reimport study_center: end")

print("Reimport study_study_card: start")
    
sourceCursor.execute("SELECT STUDY_ID, STUDY_CARD_ID FROM STUDY_CARD")

query = "INSERT INTO study_study_card (study_id, study_card_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Reimport study_study_card: end")

print("Reimport study_user: start")
    
sourceCursor.execute("SELECT IS_RECEIVE_ANONYMIZATION_REPORT, IS_RECEIVE_NEW_IMPORT_REPORT, STUDY_ID, REF_STUDY_USER_TYPE_ID, USER_ID FROM REL_STUDY_USER")

query = "INSERT INTO study_user (receive_anonymization_report, receive_new_import_report, study_id, study_user_type, user_id) VALUES (%s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Reimport study_user: end")

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
    
sourceCursor.execute("""SELECT gof.GROUP_OF_SUBJECTS_ID, gof.GROUP_NAME, egof.STUDY_ID FROM GROUP_OF_SUBJECTS gof, EXPERIMENTAL_GROUP_OF_SUBJECTS egof
	WHERE gof.GROUP_OF_SUBJECTS_ID = egof.GROUP_OF_SUBJECTS_ID""")

query = "INSERT INTO group_of_subjects (id, dtype, group_name, study_id) VALUES (%s, 'EXPERIMENTAL', %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import group_of_subjects: end")

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
