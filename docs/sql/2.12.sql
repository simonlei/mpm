alter table t_photos
    change column mediaType `media_type` enum ('photo','video') NOT NULL DEFAULT 'photo' COMMENT '类型，照片还是视频';

alter table t_photos
    change column takenDate `taken_date` datetime DEFAULT NULL;

