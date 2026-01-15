-- 数据库字段命名规范变更脚本
-- 将所有CamelCase字段改为snake_case
-- 执行日期: 2026-01-15

-- ========================================
-- 1. 修改 t_users 表
-- ========================================
ALTER TABLE `t_users` 
  CHANGE COLUMN `faceId` `face_id` BIGINT(20) DEFAULT NULL,
  CHANGE COLUMN `isAdmin` `is_admin` TINYINT(1) DEFAULT NULL;

-- ========================================
-- 2. 修改 t_face 表
-- ========================================
ALTER TABLE `t_face` 
  CHANGE COLUMN `personId` `person_id` VARCHAR(100) DEFAULT NULL,
  CHANGE COLUMN `faceId` `face_id` BIGINT(20) DEFAULT NULL,
  CHANGE COLUMN `selectedFace` `selected_face` BIGINT(20) DEFAULT NULL COMMENT '选择用的头像，默认是空，用最大的头像';

-- ========================================
-- 3. 修改 photo_face_info 表
-- ========================================
ALTER TABLE `photo_face_info` 
  CHANGE COLUMN `photoId` `photo_id` BIGINT(20) DEFAULT NULL,
  CHANGE COLUMN `faceId` `face_id` BIGINT(20) DEFAULT NULL;

-- ========================================
-- 4. 修改 t_files 表
-- ========================================
ALTER TABLE `t_files` 
  CHANGE COLUMN `isFolder` `is_folder` TINYINT(1) DEFAULT 0,
  CHANGE COLUMN `parentId` `parent_id` BIGINT(20) DEFAULT NULL,
  CHANGE COLUMN `photoId` `photo_id` BIGINT(20) DEFAULT NULL,
  CHANGE COLUMN `photoName` `photo_name` VARCHAR(255) DEFAULT NULL,
  CHANGE COLUMN `createdAt` `created_at` DATETIME DEFAULT NULL;

-- ========================================
-- 5. 修改 t_activity 表
-- ========================================
ALTER TABLE `t_activity` 
  CHANGE COLUMN `startDate` `start_date` DATE NOT NULL COMMENT '开始日期',
  CHANGE COLUMN `endDate` `end_date` DATE NOT NULL COMMENT '结束日期';

-- ========================================
-- 注意事项：
-- 1. 执行前请先备份数据库
-- 2. 建议在低峰期执行
-- 3. 执行后需要重启应用服务
-- 4. 如有外键约束，请先检查并处理
-- ========================================

-- 验证脚本 - 执行后可用于确认字段已正确更改
-- SELECT 
--   TABLE_NAME, 
--   COLUMN_NAME, 
--   DATA_TYPE 
-- FROM 
--   INFORMATION_SCHEMA.COLUMNS 
-- WHERE 
--   TABLE_SCHEMA = DATABASE() 
--   AND TABLE_NAME IN ('t_users', 't_face', 'photo_face_info', 't_files', 't_activity')
--   AND COLUMN_NAME LIKE '%\_%'
-- ORDER BY 
--   TABLE_NAME, ORDINAL_POSITION;
