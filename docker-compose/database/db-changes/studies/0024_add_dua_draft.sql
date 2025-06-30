create table dua_draft (
    id varchar(36) not null,
    study_id bigint(20) not null,
    study_name varchar(255) not null,
    url tinytext,
    funding text,
    thanks text,
    papers text,
    logo_url tinytext,
    primary key (id)
);