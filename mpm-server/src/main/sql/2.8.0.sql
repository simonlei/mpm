create table photo_face_info
(
    `id`       bigint NOT NULL AUTO_INCREMENT,
    `photo_id` bigint NOT NULL,
    `face_id`  bigint NOT NULL,
    `x`        int    NOT NULL,
    `y`        int    NOT NULL,
    `width`    int    NOT NULL,
    `height`   int    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table t_face
(
    `id`        bigint       NOT NULL AUTO_INCREMENT,
    `name`      varchar(100) DEFAULT NULL,
    `person_id` bigint DEFAULT NULL,
    `face_id`   bigint DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
