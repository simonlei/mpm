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
