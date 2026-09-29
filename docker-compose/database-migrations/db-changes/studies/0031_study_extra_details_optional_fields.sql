ALTER TABLE `study_extra_details`
  MODIFY `expected_nb_of_subjects` bigint(20) DEFAULT NULL,
  MODIFY `expected_nb_of_centers` bigint(20) DEFAULT NULL,
  MODIFY `sponsor` varchar(255) DEFAULT NULL,
  MODIFY `principal_investigator` varchar(255) DEFAULT NULL;