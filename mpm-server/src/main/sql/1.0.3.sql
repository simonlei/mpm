alter table `t_files`
drop
column `description`;

alter table `t_photos`
    add column `tags` varchar(500) null comment '标签';