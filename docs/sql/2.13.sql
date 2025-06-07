alter table t_files
    add column `createdAt` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP;

update t_files set parentId=-1 where parentId=0;

update t_files set parentId=-1 where parentId is null;