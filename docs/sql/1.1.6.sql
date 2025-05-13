alter table t_photos
    change column rotate `rotate` int DEFAULT 3600 COMMENT '旋转度数';

update t_photos
set rotate = 3600
where rotate = 0;