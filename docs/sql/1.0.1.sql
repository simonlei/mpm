alter table t_photos
    add column mediaType enum( 'photo', 'video') NOT NULL DEFAULT 'photo' COMMENT '类型，照片还是视频';

alter table t_photos
    add column `duration` decimal(15,10) DEFAULT NULL COMMENT '视频长度';