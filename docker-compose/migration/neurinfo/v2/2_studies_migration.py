#!/usr/bin/python3

import os
import pymysql

sourceConn = pymysql.connect(host="mysql", user="shanoir", password="shanoir", database="shanoirdb", charset="utf8")
targetConn = pymysql.connect(host="localhost", user="shanoir", password="shanoir", database="studies", charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("Delete study_user: start")
    
query = "DELETE FROM study_user"

targetCursor.execute(query)
targetConn.commit()

print("Delete study_user: end")

print("Drop study_study_card: start")
    
query = "DROP TABLE study_study_card"

targetCursor.execute(query)
targetConn.commit()

print("Drop study_study_card: end")

print("Delete study_center: start")
    
query = "DELETE FROM study_center"

targetCursor.execute(query)
targetConn.commit()

print("Delete study_center: end")

print("Delete studies: start")
    
query = "DELETE FROM study"

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

print("Import pseudonymus_hash_values: start")
    
sourceCursor.execute("""SELECT PSEUDONYMUS_HASH_VALUES_ID, BIRTH_DATE_HASH, BIRTH_NAME_HASH_1, BIRTH_NAME_HASH_2, BIRTH_NAME_HASH_3, FIRST_NAME_HASH_1, 
	FIRST_NAME_HASH_2, FIRST_NAME_HASH_3, LAST_NAME_HASH_1, LAST_NAME_HASH_2, LAST_NAME_HASH_3 FROM PSEUDONYMUS_HASH_VALUES""")

query = "INSERT INTO pseudonymus_hash_values (id, birth_date_hash, birth_name_hash1, birth_name_hash2, birth_name_hash3, first_name_hash1, first_name_hash2, first_name_hash3, last_name_hash1, last_name_hash2, last_name_hash3) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import pseudonymus_hash_values: end")

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

print("Import subject: start")
    
sourceCursor.execute("""SELECT s.SUBJECT_ID, s.BIRTH_DATE, s.SUBJECT_IDENTIFIER, s.REF_IMAGED_OBJECT_CATEGORY_ID, s.REF_LANGUAGE_HEMISPHERIC_DOMINANCE_ID, 
	s.REF_MANUAL_HEMISPHERIC_DOMINANCE_ID, s.NAME, s.REF_SEX_ID, phv.PSEUDONYMUS_HASH_VALUES_ID FROM SUBJECT s LEFT JOIN PSEUDONYMUS_HASH_VALUES phv ON s.SUBJECT_ID = phv.SUBJECT_ID""")

query = "INSERT INTO subject (id, birth_date, identifier, imaged_object_category, language_hemispheric_dominance, manual_hemispheric_dominance, name, sex, pseudonymus_hash_values_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import subject: end")

print("Import subject_group_of_subjects: start")
    
sourceCursor.execute("SELECT REL_SUBJECT_GROUP_OF_SUBJECTS_ID, GROUP_OF_SUBJECTS_ID, SUBJECT_ID FROM REL_SUBJECT_GROUP_OF_SUBJECTS")

query = "INSERT INTO subject_group_of_subjects (id, group_of_subjects_id, subject_id) VALUES (%s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import subject_group_of_subjects: end")

print("Import subject_study: start")
    
sourceCursor.execute("SELECT REL_SUBJECT_STUDY_ID, IS_PHYSICALLY_INVOLVED, SUBJECT_STUDY_IDENTIFIER, REF_SUBJECT_TYPE_ID, STUDY_ID, SUBJECT_ID FROM REL_SUBJECT_STUDY")

query = "INSERT INTO subject_study (id, physically_involved, subject_study_identifier, subject_type, study_id, subject_id) VALUES (%s, %s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import subject_study: end")

print("Import user_personal_comment_subject: start")
    
sourceCursor.execute("SELECT USER_COMMENT_ON_SUBJECT_ID, COMMENT, SUBJECT_ID FROM USER_COMMENT_ON_SUBJECT")

# private User user
query = "INSERT INTO user_personal_comment_subject (id, comment, subject_id) VALUES (%s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import user_personal_comment_subject: end")

sourceConn.close()
targetConn.close()
