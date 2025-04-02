--DROP DATABASE IF EXISTS userapp_read_db;
--CREATE DATABASE userapp_read_db;
DROP TABLE IF EXISTS usersapp;

CREATE TABLE usersapp(
    user_app_id uuid PRIMARY KEY,
	email VARCHAR(255) NOT NULL UNIQUE,
	name VARCHAR(255) NOT NULL,
	lastname VARCHAR(255) NOT NULL
);