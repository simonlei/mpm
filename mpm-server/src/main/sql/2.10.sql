create table t_users
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `account`     varchar(50)  NOT NULL,
    `name` varchar(100) NOT NULL,
    `faceId`      bigint NULL COMMENT '对应的 faceid',
    `salt`        varchar(50)  NOT NULL,
    `passwd`      varchar(100) NOT NULL,
    `isAdmin`     int          NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

insert into t_users ( account, name, salt, passwd, isAdmin)
values( 'admin', 'admin', 'salt', 'F9A81477552594C79F2ABC3FC099DAA896A6E3A3590A55FFA392B6000412E80B', true);