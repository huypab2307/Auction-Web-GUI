-- Active: 1776779132143@@gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com@4000@auction
CREATE DATABASE auction;
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('BIDDER','SELLER','ADMIN') NOT NULL DEFAULT 'BIDDER'
);
SELECT * FROM user;
