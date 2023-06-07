alter table events modify message text;
alter table events add timestamp bigint(20) default NULL;