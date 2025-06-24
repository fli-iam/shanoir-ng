create table dua_draft (
    id varchar(36) not null,
    study_id bigint(20) not null,
    url tinytext,
    funding text,
    thanks text,
    papers text,
    primary key (id)
);