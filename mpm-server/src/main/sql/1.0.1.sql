alter table t_photos
add column mediaType enum( 'photo', 'video') NOT NULL DEFAULT 'photo' COMMENT '类型，照片还是视频';