#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(host="mysql", user="shanoir", password="shanoir", database="shanoirdb", charset="utf8")
targetConn = pymysql.connect(host="localhost", user="shanoir", password="shanoir", database="studies", charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("Import centers: start")
    
sourceCursor.execute("SELECT CENTER_ID, TOWN, COUNTRY, NAME, PHONE_NUMBER, POSTCODE, STREET, WEBSITE FROM CENTER")

query = "INSERT INTO center (id, city, country, name, phone_number, postal_code, street, website) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import centers: end")

print("Import studies: start")
    
sourceCursor.execute("""SELECT STUDY_ID, IS_CLINICAL, COORDINATOR_ID, IS_DOWNLOADABLE_BY_DEFAULT, END_DATE, IS_MONO_CENTER, NAME,
    START_DATE, REF_STUDY_STATUS_ID, REF_STUDY_TYPE_ID, IS_VISIBLE_BY_DEFAULT, IS_WITH_EXAMINATION FROM STUDY""")

query = """INSERT INTO study
    (id, clinical, coordinator_id, downloadable_by_default, end_date, mono_center, name, start_date, study_status, study_type, visible_by_default, with_examination, challenge)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 0)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import studies: end")

print("Import study_center: start")
    
sourceCursor.execute("SELECT REL_STUDY_CENTER_ID, CENTER_ID, STUDY_ID FROM REL_STUDY_CENTER")

query = "INSERT INTO study_center (id, center_id, study_id) VALUES (%s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import study_center: end")

print("Import study_user: start")
    
sourceCursor.execute("""SELECT rsu.REL_STUDY_USER_ID, rsu.IS_RECEIVE_ANONYMIZATION_REPORT, rsu.IS_RECEIVE_NEW_IMPORT_REPORT, rsu.STUDY_ID, rsu.USER_ID, u.USERNAME 
	FROM REL_STUDY_USER rsu JOIN USERS u ON rsu.USER_ID = u.USER_ID""")

# private List<Integer> studyUserRights;
query = "INSERT INTO study_user (id, receive_anonymization_report, receive_new_import_report, study_id, user_id, user_name) VALUES (%s, %s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import study_user: end")

print("Import manufacturers: start")
    
sourceCursor.execute("SELECT MANUFACTURER_ID, NAME FROM MANUFACTURER")

query = "INSERT INTO manufacturer (id, name) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import manufacturers: end")

print("Import manufacturer models: start")
    
sourceCursor.execute("""SELECT mm.MANUFACTURER_MODEL_ID, REF_DATASET_MODALITY_TYPE_ID, NAME, MAGNETIC_FIELD, MANUFACTURER_ID
    FROM MANUFACTURER_MODEL mm LEFT JOIN MANUFACTURER_MR_MODEL mmm ON mm.MANUFACTURER_MODEL_ID = mmm.MANUFACTURER_MODEL_ID""")

query = "INSERT INTO manufacturer_model (id, dataset_modality_type, name, magnetic_field, manufacturer_id) VALUES (%s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import manufacturer models: end")

print("Import acquisition equipments: start")
    
sourceCursor.execute("SELECT ACQUISITION_EQUIPMENT_ID, CENTER_ID, MANUFACTURER_MODEL_ID, SERIAL_NUMBER FROM ACQUISITION_EQUIPMENT")

query = "INSERT INTO acquisition_equipment (id, center_id, manufacturer_model_id, serial_number) VALUES (%s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import acquisition equipments: end")

print("Import protocol file path: start")
    
sourceCursor.execute("SELECT PATH, STUDY_ID FROM PROTOCOL_FILE_PATH")

query = "INSERT INTO protocol_file_path (path, study_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import protocol file path: end")

print("Import study_examination: start")
    
sourceCursor.execute("""SELECT EXAMINATION_ID, STUDY_ID FROM EXAMINATION""")

query = "INSERT INTO study_examination (examination_id, study_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import study_examination: end")

print("Import group_of_subjects: start")
    
sourceCursor.execute("""SELECT gof.GROUP_OF_SUBJECTS_ID, gof.GROUP_NAME, egof.STUDY_ID FROM GROUP_OF_SUBJECTS gof LEFT JOIN EXPERIMENTAL_GROUP_OF_SUBJECTS egof
	ON gof.GROUP_OF_SUBJECTS_ID = egof.GROUP_OF_SUBJECTS_ID""")

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
