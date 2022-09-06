set foreign_key_checks = 0;
alter table subject modify id bigint(20) not null auto_increment;
set foreign_key_checks = 1;
set @max = (select max(id) + 1 from subject);
set @s = concat('alter table subject auto_increment = ', @max);
prepare stmt from @s;
execute stmt;
deallocate prepare stmt;
