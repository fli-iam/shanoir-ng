use shanoir_ng_users;
insert into role
	(id, access_level, name)
values
	(1, 1000, 'Administrator'),
    (2, 200, 'Expert'),
    (3, 100, 'User'),
    (4, 0, 'Guest');
