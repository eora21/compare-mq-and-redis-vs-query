DROP DATABASE IF EXISTS compare;

CREATE DATABASE compare;

use compare;

CREATE TABLE `coupons`
(
    `coupon_code` BINARY(16) PRIMARY KEY NOT NULL,
    `account_id`  BIGINT                 NULL
);
