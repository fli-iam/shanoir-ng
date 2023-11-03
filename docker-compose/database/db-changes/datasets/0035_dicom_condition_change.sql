update study_card_condition set cardinality = 1 where cardinality is NULL;
alter table study_card_condition modify scope varchar(47);
update study_card_condition set scope = 'StudyCardDICOMConditionOnDatasets' where scope like 'StudyCardDICOMCondition';