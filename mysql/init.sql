use compare;

CREATE TABLE IF NOT EXISTS `coupons`
(
    `coupon_code` BINARY(16) PRIMARY KEY NOT NULL,
    `account_id`  BIGINT                 NULL
);
