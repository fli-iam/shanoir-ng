-- Database schema creation script

-- drop table SCORE;
-- drop table PATIENT;
-- drop table CHALLENGER;
-- drop table METRIC_STUDY_REL;
-- drop table METRIC;
-- drop table STUDY;

create table SCORE (
	SCORE_VALUE float,
    METRIC_ID bigint not null,
    OWNER_ID bigint not null,
    PATIENT_ID bigint not null,
	primary key (METRIC_ID, OWNER_ID, PATIENT_ID)
) ENGINE=InnoDB;

create table METRIC (
	ID bigint not null auto_increment,
    NAME varchar(31) unique,
    NAN varchar(31),
    POS_INF varchar(31),
    NEG_INF varchar(31),
	primary key (ID)
) ENGINE=InnoDB;

create table METRIC_STUDY_REL (
    METRIC_ID bigint not null,
    STUDY_ID bigint not null,
	primary key (METRIC_ID, STUDY_ID)
) ENGINE=InnoDB;

create table CHALLENGER (
	ID bigint not null auto_increment,
    NAME varchar(31),
	primary key (ID)
) ENGINE=InnoDB;

create table PATIENT (
	ID bigint not null auto_increment,
    NAME varchar(31),
	primary key (ID)
) ENGINE=InnoDB;

create table STUDY (
	ID bigint not null auto_increment,
    NAME varchar(31),
	primary key (ID)
) ENGINE=InnoDB;

alter table SCORE
	add index SCORE_METRIC_ID_INDEX (METRIC_ID),
	add constraint SCORE_METRIC_ID_INDEX
		foreign key (METRIC_ID)
		references METRIC (ID);

alter table SCORE
	add index SCORE_OWNER_ID_INDEX (OWNER_ID),
	add constraint SCORE_OWNER_ID_INDEX
		foreign key (OWNER_ID)
		references CHALLENGER (ID);

alter table SCORE
	add index SCORE_PATIENT_ID_INDEX (PATIENT_ID),
	add constraint SCORE_PATIENT_ID_INDEX
		foreign key (PATIENT_ID)
		references PATIENT (ID);

alter table METRIC_STUDY_REL
	add index METRIC_STUDY_REL_METRIC_ID_INDEX (METRIC_ID),
	add constraint METRIC_STUDY_REL_METRIC_ID_INDEX
		foreign key (METRIC_ID)
		references METRIC (ID);

alter table METRIC_STUDY_REL
	add index METRIC_STUDY_REL_STUDY_ID_INDEX (STUDY_ID),
	add constraint METRIC_STUDY_REL_STUDY_ID_INDEX
		foreign key (STUDY_ID)
		references STUDY (ID);
