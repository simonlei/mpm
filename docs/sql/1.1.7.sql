alter table t_files
    change column `path` `path` varchar(1024) default null;

alter table t_photos
    change column `description` `description` varchar(1024) DEFAULT NULL;