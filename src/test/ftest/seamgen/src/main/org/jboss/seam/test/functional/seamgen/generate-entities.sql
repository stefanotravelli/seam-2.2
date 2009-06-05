# This script is executed prior to "generate-entitiesTest" phase of a testsuite to feed the database with test data.
#
# by Jozef Hartinger
#

# drop itentity management tables from previous run
DROP TABLE IF EXISTS user_account;
DROP TABLE IF EXISTS user_account_role;
DROP TABLE IF EXISTS user_permission;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS user_role_group;

# empty testing tables
DELETE FROM Vehicle;
DELETE FROM Person;

# Person table inserts
INSERT INTO Person (username, name, birthdate, address) values ('johny', 'John Doe', '2009-01-01', 'test address'); # updatePersonTest
INSERT INTO Person (username, name, birthdate, address) values ('jane', 'Jane Doe', '2009-01-01', 'test address'); # removePersonTest
INSERT INTO Person (username, name, birthdate, address) values ('jharting', 'Jozef Hartinger', '1987-01-01', 'Purkynova 99, Brno'); # selectVehicleTest

# Vehicle table inserts
INSERT INTO Vehicle (make, model, year, registration, state) values ('Honda', 'Civic', '2008', '11111111', 'CZ'); # updateVehicleTest
INSERT INTO Vehicle (make, model, year, registration, state) values ('Nissan', '350z', '2006', '22222222', 'CZ'); # removeVehicleTest
INSERT INTO Vehicle (make, model, year, registration, state) values ('Audi', 'A5', '2009', '99999991', 'CZ'); # searchTest
INSERT INTO Vehicle (make, model, year, registration, state) values ('Audi', 'A4', '2009', '99999992', 'CZ'); # searchTest