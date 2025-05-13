create table t_activity
(
    `id`          bigint          NOT NULL AUTO_INCREMENT,
    `name`        varchar(200)    NOT NULL COMMENT '活动名称',
    `description` varchar(1000)   NOT NULL COMMENT '描述',
    `startDate`   date            NOT NULL COMMENT '开始日期',
    `endDate`     date            NOT NULL COMMENT '结束日期',
    `latitude`    decimal(15, 10) NULL COMMENT '纬度',
    `longitude`   decimal(15, 10) NULL COMMENT '经度',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

alter table t_photos
add column activity bigint NULL COMMENT '所属活动';

alter table t_activity
change column `description` `description` varchar(1000)   NOT NULL DEFAULT '' COMMENT '描述';