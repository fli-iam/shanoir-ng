#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(host="mysql", user="shanoir", password="shanoir", database="shanoirdb", charset="utf8")
targetConn = pymysql.connect(host="localhost", user="shanoir", password="shanoir", database="shanoir_ng_studies", charset="utf8")

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

print("Import study_study_card: start")
    
sourceCursor.execute("SELECT STUDY_ID, STUDY_CARD_ID FROM STUDY_CARD")

query = "INSERT INTO study_study_card (study_id, study_card_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import study_study_card: end")

print("Import study_user: start")
    
sourceCursor.execute("SELECT REL_STUDY_USER_ID, IS_RECEIVE_ANONYMIZATION_REPORT, IS_RECEIVE_NEW_IMPORT_REPORT, STUDY_ID, REF_STUDY_USER_TYPE_ID, USER_ID FROM REL_STUDY_USER")

query = "INSERT INTO study_user (id, receive_anonymization_report, receive_new_import_report, study_id, study_user_type, user_id) VALUES (%s, %s, %s, %s, %s, %s)"

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
    FROM MANUFACTURER_MODEL mm, MANUFACTURER_MR_MODEL mmm
    WHERE mm.MANUFACTURER_MODEL_ID = mmm.MANUFACTURER_MODEL_ID""")

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

sourceConn.close()
targetConn.close()
