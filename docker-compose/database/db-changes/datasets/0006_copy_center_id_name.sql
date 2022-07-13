delete from datasets.center;
insert into datasets.center (id, name) select id, name from studies.center;