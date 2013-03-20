---- MORTALITY
-- Create:
CREATE TABLE mortality(
 death_year INT4 DEFAULT 0 NOT NULL,
 death_month INT2 DEFAULT 0 NOT NULL,
 death_day INT2 DEFAULT 0 NOT NULL,
 death_hour INT2 DEFAULT 0 NOT NULL,
 death_minute INT2 DEFAULT 0 NOT NULL,
 birth_year INT4 DEFAULT 0 NOT NULL,
 birth_month INT2 DEFAULT 0 NOT NULL,
 birth_day INT2 DEFAULT 0 NOT NULL,
 permanent_residence INT4 DEFAULT 0 NOT NULL,
 effective_residence INT4 DEFAULT 0 NOT NULL,
 gender INT2 DEFAULT 0 NOT NULL,
 age INT4 DEFAULT 0 NOT NULL,
 diagnosis_1 VARCHAR (5) DEFAULT '' NOT NULL,
 diagnosis_2 VARCHAR (5) DEFAULT '' NOT NULL,
 diagnosis_3 VARCHAR (5) DEFAULT '' NOT NULL,
 diagnosis_4 VARCHAR (5) DEFAULT '' NOT NULL,
 diagnosis_5 VARCHAR (5) DEFAULT '' NOT NULL,
 diagnoser INT2 DEFAULT 0 NOT NULL,
 medical_treatment INT2 DEFAULT 0 NOT NULL);

-- Index:
CREATE INDEX mortality ON mortality(death_year, gender, age, effective_residence, death_month);

-- Load:
insert into mortality select * from csvread('F:/ELV/data/mortality.csv');

---- POPULATION
-- Create:
CREATE TABLE population(
 year INTEGER NOT NULL,
 settlement INTEGER NOT NULL,
 gender SMALLINT NOT NULL,
 age INTEGER NOT NULL,
 population INTEGER NOT NULL);

-- Index:
CREATE INDEX population ON population(year, gender, age, settlement);

-- Load:
insert into population select * from csvread('F:/ELV/data/population.csv');
