---- MORTALITY
-- Create:
CREATE TABLE mortality(
 year INTEGER NOT NULL,
 month SMALLINT DEFAULT 0 NOT NULL,
 gender SMALLINT NOT NULL,
 age INTEGER NOT NULL,
 settlement INTEGER NOT NULL,
 diagnosis_1 VARCHAR (5) DEFAULT '00000' NOT NULL,
 diagnosis_2 VARCHAR (5) DEFAULT '00000' NOT NULL,
 diagnosis_3 VARCHAR (5) DEFAULT '00000' NOT NULL,
 diagnosis_4 VARCHAR (5) DEFAULT '00000' NOT NULL,
 diagnosis_5 VARCHAR (5) DEFAULT '00000' NOT NULL);

-- Index:
CREATE INDEX mortality ON mortality(year, gender, age, settlement, month);

-- Load:
INSERT INTO mortality SELECT * FROM CSVREAD('F:/ELV/data/mortality.csv');

-- Unload:
CALL CSVWRITE('F:/ELV/data/mortality.csv', 'SELECT * FROM mortality', 'writeColumnHeader=false fieldDelimiter=');

---- POPULATION
-- Create:
CREATE TABLE population(
 year INTEGER NOT NULL,
 gender SMALLINT NOT NULL,
 age INTEGER NOT NULL,
 settlement INTEGER NOT NULL,
 population INTEGER NOT NULL);

-- Index:
CREATE INDEX population ON population(year, gender, age, settlement);

-- Load:
INSERT INTO population SELECT * FROM CSVREAD('F:/ELV/data/population.csv');

-- Unload:
CALL CSVWRITE('F:/ELV/data/population.csv', 'SELECT * FROM population', 'writeColumnHeader=false fieldDelimiter=');
