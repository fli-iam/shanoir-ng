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
        user        = os.environ.get("TGT_USER")        or "studies",
        password    = os.environ.get("TGT_PASSWORD")    or "password",
        database    = os.environ.get("TGT_DATABASE")    or "studies",
        charset     = os.environ.get("TGT_CHARSET")     or "utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()


print("######## CLEANING OF SOURCE DB SHANOIR_OLD_OFSEP: START ###################")
######## REL_STUDY_SUBJECT ###################
print("Delete duplicate entries (study_id, subject_id) in rel_study_subject: start")
query = """DELETE FROM REL_SUBJECT_STUDY WHERE REL_SUBJECT_STUDY_ID IN (
	SELECT REL_SUBJECT_STUDY_ID FROM (
		SELECT A.REL_SUBJECT_STUDY_ID FROM REL_SUBJECT_STUDY A JOIN (
			SELECT STUDY_ID, SUBJECT_ID, COUNT(*) FROM REL_SUBJECT_STUDY GROUP BY STUDY_ID, SUBJECT_ID HAVING COUNT(*) > 1
		) B ON A.STUDY_ID = B.STUDY_ID AND A.SUBJECT_ID = B.SUBJECT_ID ORDER BY A.REL_SUBJECT_STUDY_ID
	) AS C
)"""
sourceCursor.execute(query)
sourceConn.commit()
print("Delete duplicate entries in rel_study_subject: end")
print("######## CLEANING OF SOURCE DB SHANOIR_OLD_OFSEP: END ###################")






print("######## CLEANING OF TARGET DB MS STUDIES: START ###################")
######## CENTER ###################
print("Delete study_center: start")
query = "DELETE FROM study_center"
targetCursor.execute(query)
targetConn.commit()
print("Delete study_center: end")


print("Delete coil: start")
query = "DELETE FROM coil"
targetCursor.execute(query)
targetConn.commit()
print("Delete coil: end")


print("Delete acquisition_equipment: start")
query = "DELETE FROM acquisition_equipment"
targetCursor.execute(query)
targetConn.commit()
print("Delete acquisition_equipment: end")


print("Delete manufacturer model: start")
query = "DELETE FROM manufacturer_model"
targetCursor.execute(query)
targetConn.commit()
print("Delete manufacturer model: end")


print("Delete manufacturer: start")
query = "DELETE FROM manufacturer"
targetCursor.execute(query)
targetConn.commit()
print("Delete manufacturer: end")


print("Delete center: start")
query = "DELETE FROM center"
targetCursor.execute(query)
targetConn.commit()
print("Delete center: end")


######## STUDY ###################
print("Delete timepoint: start")
query = "DELETE FROM timepoint"
targetCursor.execute(query)
targetConn.commit()
print("Delete timepoint: end")


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


print("Delete study_examination: start")
query = "DELETE FROM study_examination"
targetCursor.execute(query)
targetConn.commit()
print("Delete study_examination: end")


print("Delete protocol_file_path: start")
query = "DELETE FROM protocol_file_path"
targetCursor.execute(query)
targetConn.commit()
print("Delete protocol_file_path: end")


######## SUBJECT ###################
print("Delete subject_study: start")
query = "DELETE FROM subject_study"
targetCursor.execute(query)
targetConn.commit()
print("Delete subject_study: end")


print("Delete subject_group_of_subjects: start")
query = "DELETE FROM subject_group_of_subjects"
targetCursor.execute(query)
targetConn.commit()
print("Delete subject_group_of_subjects: end")


print("Delete group_of_subjects: start")
query = "DELETE FROM group_of_subjects"
targetCursor.execute(query)
targetConn.commit()
print("Delete group_of_subjects: end")


print("Delete user_personal_comment_subject: start")    
query = "DELETE FROM user_personal_comment_subject"
targetCursor.execute(query)
targetConn.commit()
print("Delete user_personal_comment_subject: end")


print("Delete subject: start")    
query = "DELETE FROM subject"
targetCursor.execute(query)
targetConn.commit()
print("Delete subject: end")


print("Delete pseudonymus_hash_values: start")
query = "DELETE FROM pseudonymus_hash_values"
targetCursor.execute(query)
targetConn.commit()
print("Delete pseudonymus_hash_values: end")


print("Delete studies: start")    
query = "DELETE FROM study"
targetCursor.execute(query)
targetConn.commit()
print("Delete studies: end")
print("######## CLEANING OF TARGET DB MS STUDIES: FINISHED ###################")






print("######## IMPORTING OF TARGET DB MS STUDIES: START ###################")
print("Import center: start")
sourceCursor.execute("SELECT CENTER_ID, TOWN, COUNTRY, NAME, PHONE_NUMBER, POSTCODE, STREET, WEBSITE FROM CENTER")
query = "INSERT INTO center (id, city, country, name, phone_number, postal_code, street, website) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import center: end")


print("Import manufacturer: start")
sourceCursor.execute("SELECT MANUFACTURER_ID, NAME FROM MANUFACTURER")
query = "INSERT INTO manufacturer (id, name) VALUES (%s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import manufacturer: end")


print("Import manufacturer model: start")    
sourceCursor.execute("""SELECT mm.MANUFACTURER_MODEL_ID, REF_DATASET_MODALITY_TYPE_ID, NAME, MAGNETIC_FIELD, MANUFACTURER_ID
    FROM MANUFACTURER_MODEL mm LEFT JOIN MANUFACTURER_MR_MODEL mmm ON mm.MANUFACTURER_MODEL_ID = mmm.MANUFACTURER_MODEL_ID""")
query = "INSERT INTO manufacturer_model (id, dataset_modality_type, name, magnetic_field, manufacturer_id) VALUES (%s, %s, %s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import manufacturer model: end")


print("Import acquisition equipments: start")
sourceCursor.execute("SELECT ACQUISITION_EQUIPMENT_ID, CENTER_ID, MANUFACTURER_MODEL_ID, SERIAL_NUMBER FROM ACQUISITION_EQUIPMENT")
query = "INSERT INTO acquisition_equipment (id, center_id, manufacturer_model_id, serial_number) VALUES (%s, %s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import acquisition equipments: end")


print("Reimport studies: start")    
sourceCursor.execute("""SELECT STUDY_ID, IS_CLINICAL, COORDINATOR_ID, IS_DOWNLOADABLE_BY_DEFAULT, END_DATE, IS_MONO_CENTER, NAME,
    START_DATE, REF_STUDY_STATUS_ID, REF_STUDY_TYPE_ID, IS_VISIBLE_BY_DEFAULT, IS_WITH_EXAMINATION FROM STUDY""")
query = """INSERT INTO study
    (id, clinical, coordinator_id, downloadable_by_default, end_date, mono_center, name, start_date, study_status, study_type, visible_by_default, with_examination, challenge)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 0)"""
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
sourceCursor.execute("SELECT REL_STUDY_USER_ID, IS_RECEIVE_ANONYMIZATION_REPORT, IS_RECEIVE_NEW_IMPORT_REPORT, STUDY_ID, rsu.USER_ID, u.USERNAME FROM REL_STUDY_USER rsu JOIN USERS u WHERE rsu.USER_ID = u.USER_ID")
query = "INSERT INTO study_user (id, receive_anonymization_report, receive_new_import_report, study_id, user_id, user_name) VALUES (%s, %s, %s, %s, %s, %s)"
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
print("######## IMPORTING OF TARGET DB MS STUDIES: FINISHED ###################")


sourceConn.close()
targetConn.close()
