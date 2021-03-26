#!/usr/bin/env python
# -*- coding: utf-8 -*-

import itertools
import os
import pymysql

from dbutils import bulk_insert

sourceConn = pymysql.connect(
        host        = os.environ.get("SRC_HOST")        or "localhost",
        user        = os.environ.get("SRC_USER")        or "root",
        password    = os.environ.get("SRC_PASSWORD")    or "",
        database    = os.environ.get("SRC_DATABASE")    or "neurinfo",
        charset     = os.environ.get("SRC_CHARSET")     or "utf8")
targetConn = pymysql.connect(
        host        = os.environ.get("TGT_HOST")        or "localhost",
        user        = os.environ.get("TGT_USER")        or "datasets",
        password    = os.environ.get("TGT_PASSWORD")    or "password",
        database    = os.environ.get("TGT_DATABASE")    or "datasets",
        charset     = os.environ.get("TGT_CHARSET")     or "utf8")

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
print("Delete study_card_assignment: start")
query = "DELETE FROM study_card_assignment"
targetCursor.execute(query)
targetConn.commit()
print("Delete study_card_assignment: end")


print("Delete study_card_condition: start")
query = "DELETE FROM study_card_condition"
targetCursor.execute(query)
targetConn.commit()
print("Delete study_card_condition: end")


print("Delete study_card_rule: start")
query = "DELETE FROM study_card_rule"
targetCursor.execute(query)
targetConn.commit()
print("Delete study_card_rule: end")


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


print("Delete numerical_variable: start")
query = "DELETE FROM numerical_variable"
targetCursor.execute(query)
targetConn.commit()
print("Delete numerical_variable: end")


print("Delete coded_variable: start")
query = "DELETE FROM coded_variable"
targetCursor.execute(query)
targetConn.commit()
print("Delete coded_variable: end")


print("Delete scale_item: start")
query = "DELETE FROM scale_item"
targetCursor.execute(query)
targetConn.commit()
print("Delete scale_item: end")


print("Delete numerical_score: start")
query = "DELETE FROM numerical_score"
targetCursor.execute(query)
targetConn.commit()
print("Delete numerical_score: end")


print("Delete coded_score: start")
query = "DELETE FROM coded_score"
targetCursor.execute(query)
targetConn.commit()
print("Delete coded_score: end")


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
print("Delete instrument: end")


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

print("Delete study: start")
query = "DELETE FROM study"
targetCursor.execute(query)
targetConn.commit()
print("Delete study: end")

print("Delete subject: start")
query = "DELETE FROM subject"
targetCursor.execute(query)
targetConn.commit()
print("Delete subject: end")

print("Delete dataset_metadata: start")
query = "DELETE FROM dataset_metadata"
targetCursor.execute(query)
targetConn.commit()
print("Delete dataset_metadata: end")
print("######## CLEANING OF TARGET DB MS DATASETS: FINISHED ###################")


print("######## IMPORTING OF TARGET DB MS DATASETS: START ###################")
print("Import study cards: start")
sourceCursor.execute("SELECT STUDY_CARD_ID, ACQUISITION_EQUIPMENT_ID, IS_DISABLED, NAME, NIFTI_CONVERTER_ID, STUDY_ID FROM STUDY_CARD")
query = "INSERT INTO study_cards (id, acquisition_equipment_id, disabled, name, nifti_converter_id, study_id) VALUES (%s, %s, %s, %s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import study cards: end")

print("Import study card rules: start")
sourceCursor.execute("SELECT STUDY_CARD_RULE_ID, STUDY_CARD_ID FROM STUDY_CARD_RULE")
query = "INSERT INTO study_card_rule (id, study_card_id) VALUES (%s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import study card rules: end")

print("Import study card conditions: start")
sourceCursor.execute("SELECT STUDY_CARD_CONDITION_ID, STUDY_CARD_RULE_ID, DICOM_TAG, DICOM_VALUE, REF_COMPARISON_SIGN_ID FROM STUDY_CARD_CONDITION")
query = "INSERT INTO study_card_condition (id, rule_id, dicom_tag, dicom_value, operation) VALUES (%s, %s, %s, %s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import study card conditions: end")

print("Import study card assignments: start")
sourceCursor.execute("SELECT STUDY_CARD_ASSIGNMENT_ID, FIELD, VALUE, STUDY_CARD_RULE_ID FROM STUDY_CARD_ASSIGNMENT")
study_card_assignment_list = list()
for row in sourceCursor.fetchall():
	study_card_rule = list(row)
	if 'refDatasetModalityType' == study_card_rule[1]:
		study_card_rule[1] = 1
		study_card_rule[2] = study_card_rule[2].replace(' ', '_').upper()
		study_card_assignment_list.append(study_card_rule)
	elif 'protocolName' == study_card_rule[1]:
		study_card_rule[1] = 2
		study_card_assignment_list.append(study_card_rule)
	elif 'transmittingCoil' == study_card_rule[1]:
		study_card_rule[1] = 4
		study_card_assignment_list.append(study_card_rule)
	elif 'receivingCoil' == study_card_rule[1]:
		study_card_rule[1] = 5
		study_card_assignment_list.append(study_card_rule)
	elif 'refExploredEntity' == study_card_rule[1]:
		study_card_rule[1] = 6
		study_card_rule[2] = study_card_rule[2].replace(' ', '_').upper()
		study_card_assignment_list.append(study_card_rule)
	elif 'refAcquisitionContrast' == study_card_rule[1]:
		study_card_rule[1] = 7
		study_card_assignment_list.append(study_card_rule)
	elif 'refMrSequenceApplication' == study_card_rule[1]:
		study_card_rule[1] = 8
		study_card_rule[2] = study_card_rule[2].upper()
		study_card_assignment_list.append(study_card_rule)
	elif 'refMrSequencePhysics' == study_card_rule[1]:
		if study_card_rule[2] == 'Inversion recovery spin-echo sequence':
			study_card_rule[2] = 'INVERSION_RECOVERY_SINGLE_ECHO_SPIN_ECHO_SEQUENCE'
		else:
			study_card_rule[2] = study_card_rule[2].upper().replace(' ', '_').replace('-', '_')
		study_card_rule[1] = 9
		study_card_assignment_list.append(study_card_rule)
	elif 'name' == study_card_rule[1]:
		study_card_rule[1] = 10
		study_card_assignment_list.append(study_card_rule)
	elif 'comment' == study_card_rule[1]:
		study_card_rule[1] = 11
		study_card_assignment_list.append(study_card_rule)
	elif 'mrSequenceName' == study_card_rule[1]:
		study_card_rule[1] = 12
		study_card_assignment_list.append(study_card_rule)
	elif 'refContrastAgentUsed' == study_card_rule[1]:
		study_card_rule[1] = 13
		study_card_assignment_list.append(study_card_rule)
	elif 'refMrDatasetNature' == study_card_rule[1]:
		study_card_rule[1] = 14
		if 'T1WeightedMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'T1_WEIGHTED_MR_DATASET'
		elif 'T2WeightedMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'T2_WEIGHTED_MR_DATASET'
		elif 'T2StarWeightedMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'T2_STAR_WEIGHTED_MR_DATASET'
		elif 'ProtonDensityWeightedMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'PROTON_DENSITY_WEIGHTED_MR_DATASET'
		elif 'DiffusionWeightedMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'DIFFUSION_WEIGHTED_MR_DATASET'
		elif 'VelocityEncodedAngioMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'VELOCITY_ENCODED_ANGIO_MR_DATASET'
		elif 'TimeOfFlightMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'TIME_OF_FLIGHT_MR_DATASET'
		elif 'ContrastAgentUsedAngioMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'CONTRAST_AGENT_USED_ANGIO_MR_DATASET'
		elif 'SpinTaggingPerfusionMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'SPIN_TAGGING_PERFUSION_MR_DATASET'
		elif 'T1WeightedDCEMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'T1_WEIGHTED_DCE_MR_DATASET'
		elif 'T2WeightedDCEMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'T2_WEIGHTED_DCE_MR_DATASET'
		elif 'T2StarWeightedDCEMRDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'T2_STAR_WEIGHTED_DCE_MR_DATASET'
		elif 'FieldMapDatasetShortEchoTime'  == study_card_rule[2]:
			study_card_rule[2] = 'FIELD_MAP_DATASET_SHORT_ECHO_TIME'
		elif 'FieldMapDatasetLongEchoTime'  == study_card_rule[2]:
			study_card_rule[2] = 'FIELD_MAP_DATASET_LONG_ECHO_TIME'
		elif 'H1SinglevoxelSpectroscopyDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'H1_SINGLE_VOXEL_SPECTROSCOPY_DATASET'
		elif 'H1SpectroscopicImagingDataset'  == study_card_rule[2]:
			study_card_rule[2] = 'H1_SPECTROSCOPIC_IMAGING_DATASET'
		study_card_assignment_list.append(study_card_rule)
query = "INSERT INTO study_card_assignment (id, field, value, rule_id) VALUES (%s, %s, %s, %s)"
targetCursor.executemany(query, study_card_assignment_list)
targetConn.commit()
print("Import study card assignments: end")


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

print("Import numerical_variable: start")

sourceCursor.execute("""SELECT INSTRUMENT_VARIABLE_ID, MAX_SCORE_VALUE, MIN_SCORE_VALUE FROM NUMERICAL_VARIABLE""")

query = """INSERT INTO numerical_variable
    (instrument_variable_id, max_score_value, min_score_value)
    VALUES (%s, %s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import numerical_variable: end")

print("Import coded_variable: start")

sourceCursor.execute("""SELECT INSTRUMENT_VARIABLE_ID, MAX_SCALE_ITEM_ID, MIN_SCALE_ITEM_ID FROM CODED_VARIABLE""")

query = """INSERT INTO coded_variable
    (instrument_variable_id, max_scale_item_id, min_scale_item_id)
    VALUES (%s, %s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import coded_variable: end")

print("Import scale_item: start")

sourceCursor.execute("""SELECT SCALE_ITEM_ID,
 CORRESPONDING_NUMBER,
 QUALITATIVE_SCALE_ITEM,
 QUANTITATIVE_SCALE_ITEM,
 CODED_VARIABLE_ID,
 (SELECT LABEL_NAME FROM REF_SCALE_ITEM_TYPE r WHERE s.REF_SCALE_ITEM_TYPE_ID = r.REF_SCALE_ITEM_TYPE_ID)
 FROM SCALE_ITEM s""")

query = """INSERT INTO scale_item
    (id, corresponding_number, qualitative_scale_item, quantitative_scale_item, coded_variable_id, ref_scale_item_type)
    VALUES (%s, %s, %s, %s, %s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import scale_item: end")

print("Import instrument_domains: start")

sourceCursor.execute("SELECT INSTRUMENT_ID, REF_DOMAIN_ID FROM REL_INSTRUMENT_REF_DOMAIN")

query = "INSERT INTO instrument_domains (instrument_id, domain) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import instrument_domains: end")

print("Import examination: start")

sourceCursor.execute("""SELECT EXAMINATION_ID, CENTER_ID, COMMENT, DATE(EXAMINATION_DATE), GROUP_OF_SUBJECTS_ID, INVESTIGATOR_CENTER_ID,
	IS_INVESTIGATOR_EXTERNAL, INVESTIGATOR_ID, NOTE, STUDY_ID, SUBJECT_ID, SUBJECT_WEIGHT, TIMEPOINT_ID, 
	REF_WEIGHT_UNIT_OF_MEASURE_ID FROM EXAMINATION""")
bulk_insert(targetCursor, "examination", "id, center_id, comment, examination_date, experimental_group_of_subjects_id, investigator_center_id, investigator_external, investigator_id, note, study_id, subject_id, subject_weight, timepoint_id, weight_unit_of_measure", sourceCursor)
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

print("Import numerical_score: start")

sourceCursor.execute("SELECT n.SCORE_ID, "
 + "n.SCIENTIFIC_ARTICLE_ID, "
 + "n.SCORE_VALUE, "
 + "n.IS_SCORE_WITH_UNIT_OF_MEASURE, "
 + "(SELECT LABEL_NAME FROM REF_NUMERICAL_SCORE_TYPE r WHERE r.REF_NUMERICAL_SCORE_TYPE_ID = n.REF_NUMERICAL_SCORE_TYPE_ID), "
 + "(SELECT LABEL_NAME FROM REF_UNIT_OF_MEASURE u WHERE u.REF_UNIT_OF_MEASURE_ID = n.REF_UNIT_OF_MEASURE_ID)"
 + "FROM NUMERICAL_SCORE n"
 )

query = "INSERT INTO numerical_score (id, ";
query+= "scientific_article_id, " ;
query+= "score_value, ";
query+= "is_score_with_unit_of_measure, ";
query+= "ref_numerical_score_type, ";
query+= "ref_unit_of_measure, ";
query+= "VALUES (%s, %s, %s, %s, %s, %s)";

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import numerical_score: end")

print("Import coded_score: start")

sourceCursor.execute("SELECT SCORE_ID, SCALE_ITEM_ID FROM CODED_SCORE")

query = "INSERT INTO coded_score (score_id, scale_item_id) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import coded_score: end")

print("Import numerical_score: start")

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
bulk_insert(targetCursor, "dataset_acquisition",
	"id, acquisition_equipment_id, rank, software_release, sorting_index, examination_id", sourceCursor)
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

sourceCursor.execute("""SELECT MR_PROTOCOL_ID, 1, REF_ACQUISITION_CONTRAST_ID, REF_AXIS_ORIENTATION_AT_ACQUISITION_ID,
	COMMENT, CONTRAST_AGENT_CONCENTRATION, CONTRAST_AGENT_PRODUCT, REF_CONTRAST_AGENT_USED_ID, INJECTED_VOLUME,
	REF_MR_SEQUENCE_APPLICATION_ID, REF_MR_SEQUENCE_K_SPACE_FILL_ID, MR_SEQUENCE_NAME, REF_MR_SEQUENCE_PHYSICS_ID, PROTOCOL_NAME,
	REF_PARALLEL_ACQUISITION_TECHNIQUE_ID, RECEIVING_COIL_ID, REF_SLICE_ORDER_ID, REF_SLICE_ORIENTATION_AT_ACQUISITION_ID,
	TIME_REDUCTION_FACTOR_FOR_THE_IN_PLANE_DIRECTION, TIME_REDUCTION_FACTOR_FOR_THE_OUT_OF_PLANE_DIRECTION, TRANSMITTING_COIL_ID
	FROM MR_PROTOCOL""")
bulk_insert(targetCursor, "mr_protocol_metadata", """
	id, dtype, acquisition_contrast, axis_orientation_at_acquisition, comment, contrast_agent_concentration, contrast_agent_product,
	contrast_agent_used, injected_volume, mr_sequence_application, mr_sequencekspace_fill, mr_sequence_name, mr_sequence_physics,
	name, parallel_acquisition_technique, receiving_coil_id, slice_order, slice_orientation_at_acquisition,
	time_reduction_factor_for_the_in_plane_direction, time_reduction_factor_for_the_out_of_plane_direction,	transmitting_coil_id
        """, sourceCursor)
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

bulk_insert(targetCursor, "mr_protocol", """
	id, acquisition_duration, acquisition_resolutionx, acquisition_resolutiony, echo_train_length, filters, fovx, fovy,
	imaged_nucleus, imaging_frequency, number_of_averages, number_of_phase_encoding_steps, number_of_temporal_positions,
	patient_position, percent_phase_fov, percent_sampling, pixel_bandwidth, pixel_spacingx, pixel_spacingy, slice_spacing,
	slice_thickness, temporal_resolution, updated_metadata_id""", sourceCursor)
targetConn.commit()

print("Import mr_protocol: end")

print("Import mr_dataset_acquisition: start")

sourceCursor.execute("SELECT DATASET_ACQUISITION_ID, MR_PROTOCOL_ID FROM MR_DATASET_ACQUISITION")
bulk_insert(targetCursor, "mr_dataset_acquisition", "id, mr_protocol_id", sourceCursor)
targetConn.commit()

print("Import mr_dataset_acquisition: end")

print("Import dataset_metadata: start")

sourceCursor.execute("""SELECT DATASET_ID, REF_CARDINALITY_OF_RELATED_SUBJECTS_ID, COMMENT, 
	REF_DATASET_MODALITY_TYPE_ID, REF_EXPLORED_ENTITY_ID, NAME, REF_PROCESSED_DATASET_TYPE_ID FROM DATASET""")
bulk_insert(targetCursor, "dataset_metadata", """
	id, cardinality_of_related_subjects, comment, dataset_modality_type, explored_entity, name, processed_dataset_type
	""", sourceCursor)
targetConn.commit()

print("Import dataset_metadata: end")

print("Import related_dataset: start")

sourceCursor.execute("""SELECT STUDY_ID, DATASET_ID FROM REL_STUDY_DATASET""")
bulk_insert(targetCursor, "related_dataset", """
    study_id, dataset_id
    """, sourceCursor)
targetConn.commit()

print("Import related_dataset: end")



print("Import dataset: start")
sourceCursor.execute("""SELECT DATASET_ID, DATE(DATASET_CREATION_DATE), GROUP_OF_SUBJECTS_ID, STUDY_ID, SUBJECT_ID,
	DATASET_ACQUISITION_ID, DATASET_PROCESSING_ID, REFERENCED_DATASET_FOR_SUPERIMPOSITION_DATASET_ID, DATASET_ID FROM DATASET""")
bulk_insert(targetCursor, "dataset", """id, creation_date, group_of_subjects_id, study_id, subject_id,
	dataset_acquisition_id, dataset_processing_id, referenced_dataset_for_superimposition_id,
	updated_metadata_id""", sourceCursor)
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
bulk_insert(targetCursor, "mr_dataset_metadata", "id, mr_dataset_nature", sourceCursor)
targetConn.commit()
print("Import mr_dataset_metadata: end")


print("Import mr_dataset: start")
sourceCursor.execute("SELECT DATASET_ID, REF_MR_QUALITY_PROCEDURE_TYPE_ID, DATASET_ID FROM MR_DATASET")
bulk_insert(targetCursor, "mr_dataset", "id, mr_quality_procedure_type, updated_mr_metadata_id", sourceCursor)
targetConn.commit()
print("Import mr_dataset: end")


print("Import echo_time: start")
sourceCursor.execute("""SELECT et.ECHO_NUMBER, et.ECHO_TIME_VALUE, md.DATASET_ID
	FROM ECHO_TIME et JOIN MR_DATASET md on et.ECHO_TIME_ID = md.ECHO_TIME_ID""")
do_bulk_insert = lambda rows: bulk_insert(targetCursor, "echo_time", "echo_number, echo_time_value, mr_dataset_id", rows)
do_bulk_insert(sourceCursor)
targetConn.commit()
print("Import echo_time: end")


print("Import flip_angle: start")
sourceCursor.execute("""SELECT fa.FLIP_ANGLE_VALUE, md.DATASET_ID
	FROM FLIP_ANGLE fa JOIN MR_DATASET md on fa.FLIP_ANGLE_ID = md.FLIP_ANGLE_ID""")
do_bulk_insert = lambda rows: bulk_insert(targetCursor, "flip_angle", "flip_angle_value, mr_dataset_id", rows)
do_bulk_insert(sourceCursor)
targetConn.commit()
print("Import flip_angle: end")


print("Import inversion_time: start")
sourceCursor.execute("""SELECT it.INVERSION_TIME_VALUE, md.DATASET_ID
	FROM INVERSION_TIME it JOIN MR_DATASET md on it.INVERSION_TIME_ID = md.INVERSION_TIME_ID""")
do_bulk_insert = lambda rows: bulk_insert(targetCursor, "inversion_time", "inversion_time_value, mr_dataset_id", rows)
do_bulk_insert(sourceCursor)
targetConn.commit()
print("Import inversion_time: end")


print("Import repetition_time: start")
sourceCursor.execute("""SELECT rt.REPETITION_TIME_VALUE, md.DATASET_ID
	FROM REPETITION_TIME rt JOIN MR_DATASET md on rt.REPETITION_TIME_ID = md.REPETITION_TIME_ID""")
do_bulk_insert = lambda rows: bulk_insert(targetCursor, "repetition_time", "repetition_time_value, mr_dataset_id", rows)
do_bulk_insert(sourceCursor)
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
sourceCursor.execute("""SELECT DATASET_EXPRESSION_ID, DATE(EXPRESSION_CREATION_DATE), REF_DATASET_EXPRESSION_FORMAT_ID,
	REF_DATASET_PROCESSING_ID, FRAME_COUNT, IS_MULTI_FRAME, NIFTI_CONVERTER_ID,	NIFTI_CONVERTER_VERSION,
	IS_ORIGINAL_NIFTI_CONVERSION, DATASET_ID, ORIGINAL_DATASET_EXPRESSION_ID FROM DATASET_EXPRESSION""")
bulk_insert(targetCursor, "dataset_expression", """id, creation_date, dataset_expression_format, dataset_processing_type,
	frame_count, multi_frame, nifti_converter_id, nifti_converter_version, original_nifti_conversion, dataset_id,
	original_dataset_expression_id""", sourceCursor)
targetConn.commit()
print("Import dataset_expression: end")


print("Import dataset_file: start")
sourceCursor.execute("""
        SELECT DATASET_FILE_ID, (PACS_DATASET_FILE.DATASET_FILE_ID IS NOT NULL),
            REPLACE(PATH, "file:/vol/rw/shanoir-nifti", "file:/var/datasets-data/old") as PATH,
            DATASET_EXPRESSION_ID
            FROM DATASET_FILE LEFT JOIN PACS_DATASET_FILE USING (DATASET_FILE_ID)""")
bulk_insert(targetCursor, "dataset_file", "id, pacs, path, dataset_expression_id", sourceCursor)
targetConn.commit()
print("Import dataset_file: end")


print("Import input_of_dataset_processing: start")
sourceCursor.execute("SELECT DATASET_ID, DATASET_PROCESSING_ID FROM REL_INPUTOF_DATASET_PROCESSING")
query = "INSERT INTO input_of_dataset_processing (dataset_id, processing_id) VALUES (%s, %s)"
targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()
print("Import input_of_dataset_processing: end")


print("Import diffusion_gradient: start")
sourceCursor.execute("""SELECT DIFFUSION_GRADIENT_ID, DIFFUSION_GRADIENT_BVALUE, DIFFUSION_GRADIENT_ORIENTATION_X,
	DIFFUSION_GRADIENT_ORIENTATION_Y, DIFFUSION_GRADIENT_ORIENTATION_Z, DATASET_ID, MR_PROTOCOL_ID FROM DIFFUSION_GRADIENT""")
bulk_insert(targetCursor, "diffusion_gradient", """
	id, diffusion_gradientbvalue, diffusion_gradient_orientationx, diffusion_gradient_orientationy,
	diffusion_gradient_orientationz, mr_dataset_id, mr_protocol_id""", sourceCursor)
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

print("Import dataset_study: start")

sourceCursor.execute("""SELECT STUDY_ID, NAME FROM STUDY""")

query = """INSERT INTO study
    (id, name)
    VALUES (%s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import study: end")

print("Import dataset_subject: start")

sourceCursor.execute("""SELECT SUBJECT_ID, NAME FROM SUBJECT""")

query = """INSERT INTO subject
    (id, name)
    VALUES (%s, %s)"""

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import subject: end")

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
