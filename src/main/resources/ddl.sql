DROP DATABASE IF EXISTS compare;

CREATE DATABASE compare;

use compare;

CREATE TABLE `coupons`
(
    `coupon_code` BINARY(16) PRIMARY KEY NOT NULL,
    `account_id`  BIGINT                 NULL
);

INSERT INTO coupons (coupon_code)
WITH RECURSIVE cte AS (SELECT 1 AS n
                       UNION ALL
                       SELECT n + 1
                       FROM cte
                       WHERE n < 1000)
SELECT UUID_TO_BIN(UUID())
FROM cte;
