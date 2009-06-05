# This script is executed prior to "identityManagement" phase of a testsuite to feed the database with test data.
#
# by Jozef Hartinger
#

DELETE FROM user_account;
DELETE FROM user_account_role;
DELETE FROM user_permission;
DELETE FROM user_role;
DELETE FROM user_role_group;

# test users
insert into user_account (id, username, password_hash, enabled) values (100, 'shadowman', 'Ss/jICpf9c9GeJj8WKqx1hUClEE=', 1);
insert into user_account (id, username, password_hash, enabled) values (101, 'tester', 'Ss/jICpf9c9GeJj8WKqx1hUClEE=', 1); # used by userDeletingTest
insert into user_account (id, username, password_hash, enabled) values (102, 'demo', 'Ss/jICpf9c9GeJj8WKqx1hUClEE=', 1);

# test groups
insert into user_role (id, name, conditional) values (100, 'commiter', false); # used by roleDeletingTest
insert into user_role (id, name, conditional) values (101, 'tester', false); # used by several tests, do not delete or modify this role
insert into user_role (id, name, conditional) values (102, 'QA', false);
insert into user_role (id, name, conditional) values (103, 'designer', false); # used by several tests, do not delete or modify this role
insert into user_role (id, name, conditional) values (104, 'pilot', false); # used by several tests, do not delete or modify this role
insert into user_role (id, name, conditional) values (105, 'student', false); # used by several tests, do not delete or modify this role

#insert into user_account_role (account_id, member_of_role) values (1, 1);
#insert into user_role_group (role_id, member_of_role) values (2, 102);