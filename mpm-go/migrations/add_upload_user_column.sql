-- 为 t_files 表添加 upload_user 字段
-- 用于记录文件的上传用户
-- 执行日期: 2026-01-19

-- ========================================
-- 添加 upload_user 字段到 t_files 表
-- ========================================
ALTER TABLE `t_files` 
  ADD COLUMN `upload_user` VARCHAR(100) DEFAULT NULL COMMENT '上传用户账号' AFTER `path`;

-- ========================================
-- 注意事项：
-- 1. 执行前请先备份数据库
-- 2. 该字段可为空，因为历史数据可能没有上传用户信息
-- 3. 新上传的文件会从 HTTP Header 的 Account 字段获取用户名
-- 4. 执行后需要重启应用服务
-- ========================================

-- 验证脚本 - 执行后可用于确认字段已正确添加
-- SELECT 
--   TABLE_NAME, 
--   COLUMN_NAME, 
--   DATA_TYPE,
--   COLUMN_COMMENT
-- FROM 
--   INFORMATION_SCHEMA.COLUMNS 
-- WHERE 
--   TABLE_SCHEMA = DATABASE() 
--   AND TABLE_NAME = 't_files'
--   AND COLUMN_NAME = 'upload_user';
