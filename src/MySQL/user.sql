-- Active: 1776668276741@@bnh4szqalzeyaqls8eup-mysql.services.clever-cloud.com@3306@bnh4szqalzeyaqls8eup
CREATE DATABASE auction;
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
SELECT * FROM user;