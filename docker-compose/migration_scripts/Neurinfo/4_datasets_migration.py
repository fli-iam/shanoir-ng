#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(host="localhost", user="root", password="", database="neurinfo", charset="utf8")
targetConn = pymysql.connect(host="localhost", user="root", password="", database="datasets", charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

query = "SET FOREIGN_KEY_CHECKS=0"
targetCursor.execute(query)
query = "SET UNIQUE_CHECKS=0"
targetCursor.execute(query)
query = "SET AUTOCOMMIT=0"
targetCursor.execute(query)
query = "SET SQL_LOG_BIN=0"
targetCursor.execute(query)
targetConn.commit()

print("######## CLEANING OF TARGET DB MS DATASETS: START ###################")
print("Delete study_cards: start")
query = "DELETE FROM study_cards"
targetCursor.execute(query)
targetConn.commit()
print("Delete study_cards: end")


print("Delete mr_dataset_acquisition: start")
query = "DELETE FROM mr_dataset_acquisition"
targetCursor.execute(query)
targetConn.commit()
print("Delete mr_dataset_acquisition: end")


print("Delete inversion_time: start")
query = "DELETE FROM inversion_time"
targetCursor.execute(query)
targetConn.commit()
print("Delete inversion_time: end")


print("Delete echo_time: start")
query = "DELETE FROM echo_time"
targetCursor.execute(query)
targetConn.commit()
print("Delete echo_time: end")


print("Delete repetition_time: start")
query = "DELETE FROM repetition_time"
targetCursor.execute(query)
targetConn.commit()
print("Delete repetition_time: end")


print("Delete flip_angle: start")
query = "DELETE FROM flip_angle"
targetCursor.execute(query)
targetConn.commit()
print("Delete flip_angle: end")


print("Delete mr_dataset: start")
query = "DELETE FROM mr_dataset"
targetCursor.execute(query)
targetConn.commit()
print("Delete mr_dataset: end")


print("Delete pet_dataset: start")
query = "DELETE FROM pet_dataset"
targetCursor.execute(query)
targetConn.commit()
print("Delete pet_dataset: end")


print("Delete ct_dataset: start")
query = "DELETE FROM ct_dataset"
targetCursor.execute(query)
targetConn.commit()
print("Delete ct_dataset: end")


print("Delete dataset_file: start")
query = "DELETE FROM dataset_file"
targetCursor.execute(query)
targetConn.commit()
print("Delete dataset_file: end")


print("Delete dataset_expression: start")
query = "DELETE FROM dataset_expression"
targetCursor.execute(query)
targetConn.commit()
print("Delete dataset_expression: end")


print("Delete dataset: start")
query = "DELETE FROM dataset"
targetCursor.execute(query)
targetConn.commit()
print("Delete dataset: end")


print("Delete pet_dataset_acquisition: start")
query = "DELETE FROM pet_dataset_acquisition"
targetCursor.execute(query)
targetConn.commit()
print("Delete pet_dataset_acquisition: end")


print("Delete mr_dataset_acquisition: start")
query = "DELETE FROM mr_dataset_acquisition"
targetCursor.execute(query)
targetConn.commit()
print("Delete mr_dataset_acquisition: end")


print("Delete ct_dataset_acquisition: start")
query = "DELETE FROM ct_dataset_acquisition"
targetCursor.execute(query)
targetConn.commit()
print("Delete ct_dataset_acquisition: end")


print("Delete dataset_acquisition: start")
query = "DELETE FROM dataset_acquisition"
targetCursor.execute(query)
targetConn.commit()
print("Delete dataset_acquisition: end")


print("Delete extra_data_file_path: start")
query = "DELETE FROM extra_data_file_path"
targetCursor.execute(query)
targetConn.commit()
print("Delete extra_data_file_path: end")


print("Delete score: start")
query = "DELETE FROM score"
targetCursor.execute(query)
targetConn.commit()
print("Delete score: end")


print("Delete variable_assessment: start")
query = "DELETE FROM variable_assessment"
targetCursor.execute(query)
targetConn.commit()
print("Delete variable_assessment: end")


print("Delete instrument_based_assessment: start")
query = "DELETE FROM instrument_based_assessment"
targetCursor.execute(query)
targetConn.commit()
print("Delete instrument_based_assessment: end")


print("Delete examination: start")
query = "DELETE FROM examination"
targetCursor.execute(query)
targetConn.commit()
print("Delete examination: end")


print("Delete instrument_variable: start")
query = "DELETE FROM instrument_variable"
targetCursor.execute(query)
targetConn.commit()
print("Delete instrument_variable: end")


print("Delete instrument_domains: start")
query = "DELETE FROM instrument_domains"
targetCursor.execute(query)
targetConn.commit()
print("Delete instrument_domains: end")


print("Delete instrument: start")
query = "DELETE FROM instrument"
targetCursor.execute(query)
targetConn.commit()
print("Delete instrument: end2")


print("Delete mr_protocol: start")
query = "DELETE FROM mr_protocol"
targetCursor.execute(query)
targetConn.commit()
print("Delete mr_protocol: end")


print("Delete mr_protocol_metadata_mr_scanning_sequence: start")
query = "DELETE FROM mr_protocol_metadata_mr_scanning_sequence"
targetCursor.execute(query)
targetConn.commit()
print("Delete mr_protocol_metadata_mr_scanning_sequence: end")


print("Delete mr_protocol_metadata_mr_sequence_variant: start")
query = "DELETE FROM mr_protocol_metadata_mr_sequence_variant"
targetCursor.execute(query)
targetConn.commit()
print("Delete mr_protocol_metadata_mr_sequence_variant: end")


print("Delete mr_protocol_metadata: start")
query = "DELETE FROM mr_protocol_metadata"
targetCursor.execute(query)
targetConn.commit()
print("Delete mr_protocol_metadata: end")


print("Delete dataset_processing: start")
query = "DELETE FROM dataset_processing"
targetCursor.execute(query)
targetConn.commit()
print("Delete dataset_processing: end")


print("Delete mr_dataset_metadata: start")
query = "DELETE FROM mr_dataset_metadata"
targetCursor.execute(query)
targetConn.commit()
print("Delete mr_dataset_metadata: end")


print("Delete dataset_metadata: start")
query = "DELETE FROM dataset_metadata"
targetCursor.execute(query)
targetConn.commit()
print("Delete dataset_metadata: end")
print("######## CLEANING OF TARGET DB MS DATASETS: FINISHED ###################")






print("######## IMPORTING OF TARGET DB MS DATASETS: START ###################")
print("Import study cards: start")
sourceCursor.execute("SELECT STUDY_CARD_ID, ACQUISITION_EQUIPMENT_ID, CENTER_ID, IS_DISABLED, NAME, NIFTI_CONVERTER_ID, STUDY_ID FROM STUDY_CARD")
query = "INSERT INTO study_cards (id, acquisition_equipment_id, center_id, disabled, name, nifti_converter_id, study_id) VALUES (%s, %s, %s, %s, %s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import study cards: end")


print("Import scientific_article: start")

sourceCursor.execute("SELECT SCIENTIFIC_ARTICLE_ID, SCIENTIFIC_ARTICLE_REFERENCE, REF_SCIENTIFIC_ARTICLE_TYPE_ID FROM SCIENTIFIC_ARTICLE")

query = "INSERT INTO scientific_article (id, scientific_article_reference, scientific_article_type) VALUES (%s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import scientific_article: end")


print("Import instrument: start")
sourceCursor.execute("""SELECT INSTRUMENT_ID, ACRONYM, REF_INSTRUMENT_TYPE_ID, IS_MONO_DOMAIN, NAME, REF_PASSATION_MODE_ID, 
	SCIENTIFIC_ARTICLE_ID, PARENT_INSTRUMENT_ID FROM INSTRUMENT""")
query = """INSERT INTO instrument
	(id, acronym, instrument_type, mono_domain, name, passation_mode, instrument_definition_article_id, parent_instrument_id)
	VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"""
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import instrument: end")


print("Import instrument_variable: start")

sourceCursor.execute("""SELECT INSTRUMENT_VARIABLE_ID, IS_AGE_DEPENDENT, IS_CULTURAL_SKILL_DEPENDENT, REF_DOMAIN_ID, IS_MAIN,
	NAME, REF_QUALITY_ID, IS_SEX_DEPENDENT, IS_STANDARDIZED, INSTRUMENT_ID FROM INSTRUMENT_VARIABLE""")

query = """INSERT INTO instrument_variable
	(id, age_dependent, cultural_skill_dependent, domain, main, name, quality, sex_dependent, standardized, instrument_id)
	VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import instrument_variable: end")

print("Import instrument_domains: start")

sourceCursor.execute("SELECT INSTRUMENT_ID, REF_DOMAIN_ID FROM REL_INSTRUMENT_REF_DOMAIN")

query = "INSERT INTO instrument_domains (instrument_id, domain) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import instrument_domains: end")

print("Import examination: start")

sourceCursor.execute("""SELECT EXAMINATION_ID, CENTER_ID, COMMENT, EXAMINATION_DATE, GROUP_OF_SUBJECTS_ID, INVESTIGATOR_CENTER_ID,
	IS_INVESTIGATOR_EXTERNAL, INVESTIGATOR_ID, NOTE, STUDY_ID, SUBJECT_ID, SUBJECT_WEIGHT, TIMEPOINT_ID, 
	REF_WEIGHT_UNIT_OF_MEASURE_ID FROM EXAMINATION""")

query = """INSERT INTO examination
	(id, center_id, comment, examination_date, experimental_group_of_subjects_id, investigator_center_id, investigator_external, investigator_id, note, study_id, subject_id, subject_weight, timepoint_id, weight_unit_of_measure)
	VALUES (%s, %s, %s, DATE(%s), %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import examination: end")

print("Import instrument_based_assessment: start")

sourceCursor.execute("SELECT INSTRUMENT_BASED_ASSESSMENT_ID, EXAMINATION_ID, INSTRUMENT_ID FROM INSTRUMENT_BASED_ASSESSMENT")

query = "INSERT INTO instrument_based_assessment (id, examination_id, instrument_id) VALUES (%s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import instrument_based_assessment: end")

print("Import variable_assessment: start")

sourceCursor.execute("SELECT VARIABLE_ASSESSMENT_ID, INSTRUMENT_BASED_ASSESSMENT_ID, INSTRUMENT_VARIABLE_ID FROM VARIABLE_ASSESSMENT")

query = "INSERT INTO variable_assessment (id, instrument_based_assessment_id, instrument_variable_id) VALUES (%s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import variable_assessment: end")

print("Import score: start")

sourceCursor.execute("SELECT SCORE_ID, VARIABLE_ASSESSMENT_ID FROM SCORE")

query = "INSERT INTO score (id, variable_assessment_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import score: end")

print("Import extra_data_file_path: start")

sourceCursor.execute("SELECT EXAMINATION_ID, PATH FROM EXTRA_DATA_FILE_PATH")

query = "INSERT INTO extra_data_file_path (examination_id, path) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import extra_data_file_path: end")

print("Import dataset_processing: start")

sourceCursor.execute("""SELECT DATASET_PROCESSING_ID, COMMENT, REF_DATASET_PROCESSING_ID, DATASET_PROCESSING_DATE, STUDY_ID
	FROM DATASET_PROCESSING""")

query = "INSERT INTO dataset_processing (id, comment, dataset_processing_type, processing_date, study_id) VALUES (%s, %s, %s, %s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import dataset_processing: end")


print("Import dataset_acquisition: start")
sourceCursor.execute("""SELECT DATASET_ACQUISITION_ID, ACQUISITION_EQUIPMENT_ID, RANK, SOFTWARE_RELEASE, SORTING_INDEX, EXAMINATION_ID
	FROM DATASET_ACQUISITION""")
query = """INSERT INTO dataset_acquisition (id, acquisition_equipment_id, rank, software_release, sorting_index, examination_id)
	VALUES (%s, %s, %s, %s, %s, %s)"""
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import dataset_acquisition: end")


print("Import ct_protocol: start")

sourceCursor.execute("SELECT CT_PROTOCOL_ID FROM CT_PROTOCOL")

query = "INSERT INTO ct_protocol (id) VALUES (%s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import ct_protocol: end")

print("Import ct_dataset_acquisition: start")

sourceCursor.execute("SELECT cda.DATASET_ACQUISITION_ID, cp.CT_PROTOCOL_ID FROM CT_DATASET_ACQUISITION cda, CT_PROTOCOL cp")

query = "INSERT INTO ct_dataset_acquisition (id, ct_protocol_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import ct_dataset_acquisition: end")

print("Import mr_protocol_metadata: start")

sourceCursor.execute("""SELECT MR_PROTOCOL_ID, REF_ACQUISITION_CONTRAST_ID, REF_AXIS_ORIENTATION_AT_ACQUISITION_ID,
	COMMENT, CONTRAST_AGENT_CONCENTRATION, CONTRAST_AGENT_PRODUCT, REF_CONTRAST_AGENT_USED_ID, INJECTED_VOLUME,
	REF_MR_SEQUENCE_APPLICATION_ID, REF_MR_SEQUENCE_K_SPACE_FILL_ID, MR_SEQUENCE_NAME, REF_MR_SEQUENCE_PHYSICS_ID, PROTOCOL_NAME,
	REF_PARALLEL_ACQUISITION_TECHNIQUE_ID, RECEIVING_COIL_ID, REF_SLICE_ORDER_ID, REF_SLICE_ORIENTATION_AT_ACQUISITION_ID,
	TIME_REDUCTION_FACTOR_FOR_THE_IN_PLANE_DIRECTION, TIME_REDUCTION_FACTOR_FOR_THE_OUT_OF_PLANE_DIRECTION, TRANSMITTING_COIL_ID
	FROM MR_PROTOCOL""")

query = """INSERT INTO mr_protocol_metadata
	(id, dtype, acquisition_contrast, axis_orientation_at_acquisition, comment, contrast_agent_concentration, contrast_agent_product,
	contrast_agent_used, injected_volume, mr_sequence_application, mr_sequencekspace_fill, mr_sequence_name, mr_sequence_physics,
	name, parallel_acquisition_technique, receiving_coil_id, slice_order, slice_orientation_at_acquisition,
	time_reduction_factor_for_the_in_plane_direction, time_reduction_factor_for_the_out_of_plane_direction, transmitting_coil_id)
	VALUES (%s, 1, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

sourceCursor.execute("SELECT MR_PROTOCOL_ID FROM MR_PROTOCOL WHERE MAGNETIZATION_TRANSFER_ID = 1")

query = "UPDATE mr_protocol_metadata SET magnetization_transfer = 1 WHERE id = %s"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

sourceCursor.execute("SELECT MR_PROTOCOL_ID FROM MR_PROTOCOL WHERE PARALLEL_ACQUISITION_ID = 1")

query = "UPDATE mr_protocol_metadata SET parallel_acquisition = 1 WHERE id = %s"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import mr_protocol_metadata: end")

print("Import mr_protocol: start")

sourceCursor.execute("""SELECT MR_PROTOCOL_ID, ACQUISITION_DURATION, ACQUISITION_RESOLUTION_X, ACQUISITION_RESOLUTION_Y,
	ECHO_TRAIN_LENGTH, FILTERS, FOV_X, FOV_Y, REF_IMAGED_NUCLEUS_ID, IMAGING_FREQUENCY, NUMBER_OF_AVERAGES,
	NUMBER_OF_PHASE_ENCODING_STEPS, NUMBER_OF_TEMPORAL_POSITIONS, REF_PATIENT_POSITION_ID, PERCENT_PHASE_FOV, PERCENT_SAMPLING,
	PIXEL_BANDWITH, PIXEL_SPACING_X, PIXEL_SPACING_Y, SLICE_SPACING, SLICE_THICKNESS, TEMPORAL_RESOLUTION,
	MR_PROTOCOL_ID FROM MR_PROTOCOL""")

query = """INSERT INTO mr_protocol
	(id, acquisition_duration, acquisition_resolutionx, acquisition_resolutiony, echo_train_length, filters, fovx, fovy,
	imaged_nucleus, imaging_frequency, number_of_averages, number_of_phase_encoding_steps, number_of_temporal_positions,
	patient_position, percent_phase_fov, percent_sampling, pixel_bandwidth, pixel_spacingx, pixel_spacingy, slice_spacing,
	slice_thickness, temporal_resolution, updated_metadata_id)
	VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import mr_protocol: end")

print("Import mr_dataset_acquisition: start")

sourceCursor.execute("SELECT DATASET_ACQUISITION_ID, MR_PROTOCOL_ID FROM MR_DATASET_ACQUISITION")

query = "INSERT INTO mr_dataset_acquisition (id, mr_protocol_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import mr_dataset_acquisition: end")

print("Import dataset_metadata: start")

sourceCursor.execute("""SELECT DATASET_ID, REF_CARDINALITY_OF_RELATED_SUBJECTS_ID, COMMENT, 
	REF_DATASET_MODALITY_TYPE_ID, REF_EXPLORED_ENTITY_ID, NAME, REF_PROCESSED_DATASET_TYPE_ID FROM DATASET""")

query = """INSERT INTO dataset_metadata
	(id, cardinality_of_related_subjects, comment, dataset_modality_type, explored_entity, name, processed_dataset_type)
	VALUES (%s, %s, %s, %s, %s, %s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import dataset_metadata: end")


print("Import dataset: start")
sourceCursor.execute("""SELECT DATASET_ID, DATASET_CREATION_DATE, GROUP_OF_SUBJECTS_ID, STUDY_ID, SUBJECT_ID,
	DATASET_ACQUISITION_ID, DATASET_PROCESSING_ID, REFERENCED_DATASET_FOR_SUPERIMPOSITION_DATASET_ID, DATASET_ID FROM DATASET""")
query = """INSERT INTO dataset
	(id, creation_date, group_of_subjects_id, study_id, subject_id, dataset_acquisition_id, dataset_processing_id,
	referenced_dataset_for_superimposition_id, updated_metadata_id)
	VALUES (%s, DATE(%s), %s, %s, %s, %s, %s, %s, %s)"""
while True:
  many_rows = sourceCursor.fetchmany(10000)
  if not many_rows:
    break
  print("Fetching 10.000")
  targetCursor.executemany(query, many_rows)
targetConn.commit()
print("Import dataset: end")


print("Import calibration_dataset: start")

sourceCursor.execute("SELECT DATASET_ID, REF_CALIBRATION_DATASET_TYPE_ID FROM CALIBRATION_DATASET")

query = "INSERT INTO calibration_dataset (id, calibration_dataset_type) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import calibration_dataset: end")

print("Import ct_dataset: start")

sourceCursor.execute("SELECT DATASET_ID FROM CT_DATASET")

query = "INSERT INTO ct_dataset (id) VALUES (%s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import ct_dataset: end")

print("Import eeg_dataset: start")

sourceCursor.execute("SELECT DATASET_ID FROM EEG_DATASET")

query = "INSERT INTO eeg_dataset (id) VALUES (%s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import eeg_dataset: end")

print("Import meg_dataset: start")

sourceCursor.execute("SELECT DATASET_ID FROM MEG_DATASET")

query = "INSERT INTO meg_dataset (id) VALUES (%s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import meg_dataset: end")

print("Import mesh_dataset: start")

sourceCursor.execute("SELECT DATASET_ID FROM MESH_DATASET")

query = "INSERT INTO mesh_dataset (id) VALUES (%s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import mesh_dataset: end")

print("Import mr_dataset_metadata: start")

sourceCursor.execute("SELECT DATASET_ID, REF_MR_DATASET_NATURE_ID FROM MR_DATASET")

query = "INSERT INTO mr_dataset_metadata (id, mr_dataset_nature) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import mr_dataset_metadata: end")

print("Import mr_dataset: start")
sourceCursor.execute("SELECT DATASET_ID, REF_MR_QUALITY_PROCEDURE_TYPE_ID, DATASET_ID FROM MR_DATASET")
query = "INSERT INTO mr_dataset (id, mr_quality_procedure_type, updated_mr_metadata_id) VALUES (%s, %s, %s)"
while True:
  many_rows = sourceCursor.fetchmany(10000)
  if not many_rows:
    break
  print("Fetching 10.000")
  targetCursor.executemany(query, many_rows)
targetConn.commit()
print("Import mr_dataset: end")


print("Import echo_time: start")
# Echo times linked to only one dataset
sourceCursor.execute("""SELECT et.ECHO_TIME_ID, et.ECHO_NUMBER, et.ECHO_TIME_VALUE, COUNT(md.DATASET_ID) as countids 
	FROM ECHO_TIME et JOIN mr_dataset md on et.ECHO_TIME_ID = md.ECHO_TIME_ID 
	GROUP BY et.ECHO_TIME_ID HAVING countids = 1""")
query = "INSERT INTO echo_time (id, echo_number, echo_time_value, mr_dataset_id) VALUES (%s, %s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
# Get last echo time id
sourceCursor.execute("SELECT MAX(ECHO_TIME_ID) FROM ECHO_TIME")
et_next_id = sourceCursor.fetchone()[0] + 1
# Echo times linked to many dataset
sourceCursor.execute("""SELECT et.ECHO_TIME_ID, et.ECHO_NUMBER, et.ECHO_TIME_VALUE, md.DATASET_ID 
	FROM ECHO_TIME et JOIN mr_dataset md on et.ECHO_TIME_ID = md.ECHO_TIME_ID 
	WHERE et.ECHO_TIME_ID IN 
	(SELECT et.ECHO_TIME_ID FROM ECHO_TIME et JOIN mr_dataset md on et.ECHO_TIME_ID = md.ECHO_TIME_ID GROUP BY et.ECHO_TIME_ID HAVING count(md.DATASET_ID) > 1)""")
echo_times = []
et_ids = []
for row in sourceCursor.fetchall():
	echo_time = list(row);
	if echo_time[0] in et_ids:
		echo_time[0] = et_next_id
		et_next_id+=1
	echo_times.append(echo_time)
	et_ids.append(echo_time[0])
targetCursor.executemany(query, echo_times)
targetConn.commit()
print("Import echo_time: end")


print("Import flip_angle: start")
# Flip angles linked to only one dataset
sourceCursor.execute("""SELECT fa.FLIP_ANGLE_ID, fa.FLIP_ANGLE_VALUE, COUNT(md.DATASET_ID) as countids
	FROM FLIP_ANGLE fa JOIN mr_dataset md on fa.FLIP_ANGLE_ID = md.FLIP_ANGLE_ID 
	GROUP BY fa.FLIP_ANGLE_ID HAVING countids = 1""")
query = "INSERT INTO flip_angle (id, flip_angle_value, mr_dataset_id) VALUES (%s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
# Get last flip angle id
sourceCursor.execute("SELECT MAX(FLIP_ANGLE_ID) FROM FLIP_ANGLE")
fa_next_id = sourceCursor.fetchone()[0] + 1
# Flip angles linked to many dataset
sourceCursor.execute("""SELECT fa.FLIP_ANGLE_ID, fa.FLIP_ANGLE_VALUE, md.DATASET_ID 
	FROM FLIP_ANGLE fa JOIN mr_dataset md on fa.FLIP_ANGLE_ID = md.FLIP_ANGLE_ID 
	WHERE fa.FLIP_ANGLE_ID IN 
	(SELECT fa.FLIP_ANGLE_ID FROM FLIP_ANGLE fa JOIN mr_dataset md on fa.FLIP_ANGLE_ID = md.FLIP_ANGLE_ID GROUP BY fa.FLIP_ANGLE_ID HAVING count(md.DATASET_ID) > 1)""")
flip_angles = []
fa_ids = []
for row in sourceCursor.fetchall():
	flip_angle = list(row);
	if flip_angle[0] in fa_ids:
		flip_angle[0] = fa_next_id
		fa_next_id+=1
	flip_angles.append(flip_angle)
	fa_ids.append(flip_angle[0])
targetCursor.executemany(query, flip_angles)
targetConn.commit()
print("Import flip_angle: end")


print("Import inversion_time: start")
# Inversion times linked to only one dataset
sourceCursor.execute("""SELECT it.INVERSION_TIME_ID, it.INVERSION_TIME_VALUE, COUNT(md.DATASET_ID) as countids
	FROM INVERSION_TIME it JOIN mr_dataset md on it.INVERSION_TIME_ID = md.INVERSION_TIME_ID 
	GROUP BY it.INVERSION_TIME_ID HAVING countids = 1""")
query = "INSERT INTO inversion_time (id, inversion_time_value, mr_dataset_id) VALUES (%s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
# Get last inversion time id
sourceCursor.execute("SELECT MAX(INVERSION_TIME_ID) FROM INVERSION_TIME")
it_next_id = sourceCursor.fetchone()[0] + 1
# Inversion times linked to many dataset
sourceCursor.execute("""SELECT it.INVERSION_TIME_ID, it.INVERSION_TIME_VALUE, md.DATASET_ID 
	FROM INVERSION_TIME it JOIN mr_dataset md on it.INVERSION_TIME_ID = md.INVERSION_TIME_ID 
	WHERE it.INVERSION_TIME_ID IN 
	(SELECT it.INVERSION_TIME_ID FROM INVERSION_TIME it JOIN mr_dataset md on it.INVERSION_TIME_ID = md.INVERSION_TIME_ID GROUP BY it.INVERSION_TIME_ID HAVING count(md.DATASET_ID) > 1)""")
inversion_times = []
it_ids = []
for row in sourceCursor.fetchall():
	inversion_time = list(row);
	if inversion_time[0] in it_ids:
		inversion_time[0] = it_next_id
		it_next_id+=1
	inversion_times.append(inversion_time)
	it_ids.append(inversion_time[0])
targetCursor.executemany(query, inversion_times)
targetConn.commit()
print("Import inversion_time: end")


print("Import repetition_time: start")
# Repetition times linked to only one dataset
sourceCursor.execute("""SELECT rt.REPETITION_TIME_ID, rt.REPETITION_TIME_VALUE, COUNT(md.DATASET_ID) as countids
	FROM REPETITION_TIME rt JOIN mr_dataset md on rt.REPETITION_TIME_ID = md.REPETITION_TIME_ID 
	GROUP BY rt.REPETITION_TIME_ID HAVING countids = 1""")
query = "INSERT INTO repetition_time (id, repetition_time_value, mr_dataset_id) VALUES (%s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
# Get last repetition time id
sourceCursor.execute("SELECT MAX(REPETITION_TIME_ID) FROM REPETITION_TIME")
rt_next_id = sourceCursor.fetchone()[0] + 1
# Repetition times linked to many dataset
sourceCursor.execute("""SELECT rt.REPETITION_TIME_ID, rt.REPETITION_TIME_VALUE, md.DATASET_ID 
	FROM REPETITION_TIME rt JOIN mr_dataset md on rt.REPETITION_TIME_ID = md.REPETITION_TIME_ID 
	WHERE rt.REPETITION_TIME_ID IN 
	(SELECT rt.REPETITION_TIME_ID FROM REPETITION_TIME rt JOIN mr_dataset md on rt.REPETITION_TIME_ID = md.INVERSION_TIME_ID GROUP BY rt.REPETITION_TIME_ID HAVING count(md.DATASET_ID) > 1)""")
repetition_times = []
rt_ids = []
for row in sourceCursor.fetchall():
	repetition_time = list(row);
	if repetition_time[0] in rt_ids:
		repetition_time[0] = rt_next_id
		rt_next_id+=1
	inversion_times.append(repetition_time)
	rt_ids.append(repetition_time[0])
targetCursor.executemany(query, repetition_times)
targetConn.commit()
print("Import repetition_time: end")


print("Import parameter_quantification_dataset: start")
sourceCursor.execute("SELECT DATASET_ID, REF_PARAMETER_QUANTIFICATION_DATASET_NATURE_ID FROM PARAMETER_QUANTIFICATION_DATASET")
query = "INSERT INTO parameter_quantification_dataset (id, parameter_quantification_dataset_nature) VALUES (%s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import parameter_quantification_dataset: end")


print("Import pet_dataset: start")
sourceCursor.execute("SELECT DATASET_ID FROM PET_DATASET")
query = "INSERT INTO pet_dataset (id) VALUES (%s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import pet_dataset: end")


print("Import registration_dataset: start")
sourceCursor.execute("SELECT DATASET_ID, REF_REGISTRATION_DATASET_TYPE_ID FROM REGISTRATION_DATASET")
query = "INSERT INTO registration_dataset (id, registration_dataset_type) VALUES (%s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import registration_dataset: end")


print("Import segmentation_dataset: start")
sourceCursor.execute("SELECT DATASET_ID FROM SEGMENTATION_DATASET")
query = "INSERT INTO segmentation_dataset (id) VALUES (%s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import segmentation_dataset: end")


print("Import spect_dataset: start")
sourceCursor.execute("SELECT DATASET_ID, REF_SPECT_DATASET_NATURE_ID FROM SPECT_DATASET")
query = "INSERT INTO spect_dataset (id, spect_dataset_nature) VALUES (%s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import spect_dataset: end")


print("Import statistical_dataset: start")
sourceCursor.execute("SELECT DATASET_ID FROM STATISTICAL_DATASET")
query = "INSERT INTO statistical_dataset (id) VALUES (%s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import statistical_dataset: end")


print("Import template_dataset: start")
sourceCursor.execute("SELECT DATASET_ID, REF_TEMPLATE_DATASET_NATURE_ID FROM TEMPLATE_DATASET")
query = "INSERT INTO template_dataset (id, template_dataset_nature) VALUES (%s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import template_dataset: end")


print("Import dataset_expression: start")
sourceCursor.execute("""SELECT DATASET_EXPRESSION_ID, EXPRESSION_CREATION_DATE, REF_DATASET_EXPRESSION_FORMAT_ID,
	REF_DATASET_PROCESSING_ID, FRAME_COUNT, IS_MULTI_FRAME, NIFTI_CONVERTER_ID,	NIFTI_CONVERTER_VERSION,
	IS_ORIGINAL_NIFTI_CONVERSION, DATASET_ID, ORIGINAL_DATASET_EXPRESSION_ID FROM DATASET_EXPRESSION""")
query = """INSERT INTO dataset_expression
	(id, creation_date, dataset_expression_format, dataset_processing_type, frame_count, multi_frame, nifti_converter_id,
	nifti_converter_version, original_nifti_conversion, dataset_id, original_dataset_expression_id)
	VALUES (%s, DATE(%s), %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
while True:
  many_rows = sourceCursor.fetchmany(10000)
  if not many_rows:
    break
  print("Fetching 10.000")
  targetCursor.executemany(query, many_rows)
targetConn.commit()
print("Import dataset_expression: end")


print("Import dataset_file: start")
sourceCursor.execute("SELECT DATASET_FILE_ID, PATH, DATASET_EXPRESSION_ID FROM DATASET_FILE")
query = "INSERT INTO dataset_file (id, pacs, path, dataset_expression_id) VALUES (%s, 0, %s, %s)"
while True:
  many_rows = sourceCursor.fetchmany(100000)
  if not many_rows:
    break
  print("Fetching 100.000")
  targetCursor.executemany(query, many_rows)
targetConn.commit()
print("Import dataset_file: end")


print("Import pacs_dataset_file: start")
sourceCursor.execute("SELECT DATASET_FILE_ID FROM PACS_DATASET_FILE")
query = "UPDATE dataset_file SET pacs = 1 WHERE id = %s"
while True:
  many_rows = sourceCursor.fetchmany(100000)
  if not many_rows:
    break
  print("Fetching 100.000")
  targetCursor.executemany(query, many_rows)
targetConn.commit()
print("Import pacs_dataset_file: end")


print("Import input_of_dataset_processing: start")
sourceCursor.execute("SELECT DATASET_ID, DATASET_PROCESSING_ID FROM REL_INPUTOF_DATASET_PROCESSING")
query = "INSERT INTO input_of_dataset_processing (dataset_id, processing_id) VALUES (%s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import input_of_dataset_processing: end")


print("Import diffusion_gradient: start")
sourceCursor.execute("""SELECT DIFFUSION_GRADIENT_ID, DIFFUSION_GRADIENT_BVALUE, DIFFUSION_GRADIENT_ORIENTATION_X,
	DIFFUSION_GRADIENT_ORIENTATION_Y, DIFFUSION_GRADIENT_ORIENTATION_Z, DATASET_ID, MR_PROTOCOL_ID FROM DIFFUSION_GRADIENT""")
query = """INSERT INTO diffusion_gradient
	(id, diffusion_gradientbvalue, diffusion_gradient_orientationx, diffusion_gradient_orientationy,
	diffusion_gradient_orientationz, mr_dataset_id, mr_protocol_id)
	VALUES (%s, %s, %s, %s, %s, %s, %s)"""
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import diffusion_gradient: end")


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


query = "SET FOREIGN_KEY_CHECKS=1"
targetCursor.execute(query)
query = "SET UNIQUE_CHECKS=1"
targetCursor.execute(query)
query = "SET AUTOCOMMIT=1"
targetCursor.execute(query)
query = "SET SQL_LOG_BIN=1"
targetCursor.execute(query)
targetConn.commit()


sourceConn.close()
targetConn.close()
