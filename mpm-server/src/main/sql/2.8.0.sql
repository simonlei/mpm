create table photo_face_info
(
    `id`      bigint NOT NULL AUTO_INCREMENT,
    `photoId` bigint NOT NULL,
    `faceId`  bigint DEFAULT NULL,
    `x`       int    NOT NULL,
    `y`       int    NOT NULL,
    `width`   int    NOT NULL,
    `height`  int    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table t_face
(
    `id`       bigint NOT NULL AUTO_INCREMENT,
    `name`     varchar(100) DEFAULT NULL,
    `personId` varchar(100) DEFAULT NULL,
    `faceId`   bigint       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

alter table t_face
    add column selectedFace bigint DEFAULT NULL COMMENT '选择用的头像，默认是空，用最大的头像';

alter table t_face
    add column collected int DEFAULT 0 COMMENT '是否收藏';

alter table t_face
    add column hidden int DEFAULT 0 COMMENT '是否隐藏';
