# 数据库字段命名规范迁移指南

## 修改概述

本次修改将所有数据库表字段、API接口、SQL字符串中的命名从CamelCase改为snake_case，以符合标准的数据库命名规范。

## 修改日期
2026-01-15

## 影响范围

### 1. 数据库表结构变更

#### 1.1 t_users 表
- `faceId` → `face_id`
- `isAdmin` → `is_admin`

#### 1.2 t_face 表
- `personId` → `person_id`
- `faceId` → `face_id`
- `selectedFace` → `selected_face`

#### 1.3 photo_face_info 表
- `photoId` → `photo_id`
- `faceId` → `face_id`

#### 1.4 t_files 表
- `isFolder` → `is_folder`
- `parentId` → `parent_id`
- `photoId` → `photo_id`
- `photoName` → `photo_name`
- `createdAt` → `created_at`

#### 1.5 t_activity 表
- `startDate` → `start_date`
- `endDate` → `end_date`

### 2. Go代码修改

#### 2.1 Model层（/model目录）
- `model/t_user.go` - TUser结构体字段标签
- `model/t_face.go` - TFace结构体字段标签
- `model/t_face_info.go` - PhotoFaceInfo结构体字段标签
- `model/t_file.go` - TFile结构体字段标签
- `model/t_activity.go` - TActivity结构体字段标签

#### 2.2 Service层
- `face.go` - FaceGetRequest, FaceGetResp, FaceNameResp, FacesForPhotoResp, FaceUpdateParam 结构体及所有SQL语句
- `folder.go` - FoldersResp, UpdateFolderDateSchema, FolderActionSchema 结构体及所有SQL语句
- `pics.go` - 所有SQL语句中的字段名
- `pics_date.go` - 活动相关SQL语句
- `pics_service.go` - DELETE语句中的字段名
- `tags.go` - photo_tags表相关SQL语句
- `activity_test.go` - 测试数据中的JSON字段
- `go_java_diff_test.go` - 测试参数中的JSON字段

### 3. 前端代码修改

#### 3.1 TypeScript类型定义（web/src/api/index.ts）
- UserInfo接口: `isAdmin` → `is_admin`, `faceId` → `face_id`
- GetPicsParams接口: `faceId` → `face_id`
- FolderData接口: `parentid` → `parent_id`
- Face接口: `personId` → `person_id`, `faceId` → `face_id`, `selectedFace` → `selected_face`
- FaceWithName接口: `faceId` → `face_id`
- FaceForPhoto接口: `faceId` → `face_id`
- Activity接口: `startDate` → `start_date`, `endDate` → `end_date`
- API调用参数更新

#### 3.2 Store（web/src/stores/user.ts）
- User接口: `isAdmin` → `is_admin`, `faceId` → `face_id`

#### 3.3 Vue组件
- `web/src/views/Faces.vue` - 人脸相关字段
- `web/src/views/Activities.vue` - 活动日期字段
- `web/src/views/Folders.vue` - 文件夹相关字段
- `web/src/components/FolderTree.vue` - 父级ID字段
- `web/src/components/PhotoDetail.vue` - 人脸编辑相关字段

### 4. SQL字符串修改详情

#### 4.1 face.go 中的SQL修改
- `personId` → `person_id` (在SELECT和WHERE子句中)
- `faceId` → `face_id` (在SELECT、JOIN、WHERE子句中)
- `selectedFace` → `selected_face` (在SELECT子句中)

#### 4.2 folder.go 中的SQL修改
- `parentId` → `parent_id` (在SELECT和WHERE子句中)
- `photoId` → `photo_id` (在JOIN子句中)
- `isFolder` → `is_folder` (在WHERE子句中)

#### 4.3 pics.go 中的SQL修改
- `photoId` → `photo_id` (在JOIN子句中)
- `faceId` → `face_id` (在JOIN和WHERE子句中)
- `startDate` → `start_date` (在CONCAT函数中)

#### 4.4 pics_date.go 中的SQL修改
- `startDate` → `start_date` (在SELECT、GROUP BY、ORDER BY子句中)

#### 4.5 pics_service.go 中的SQL修改
- `photoId` → `photo_id` (在DELETE子句中)

#### 4.6 tags.go 中的SQL修改
- `photoId` → `photo_id` (在DELETE、SELECT、INSERT子句中)

## 数据库迁移步骤

### 1. 备份数据库
```bash
# 备份现有数据库
mysqldump -u username -p database_name > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 2. 执行迁移脚本
```bash
# 执行迁移SQL脚本
mysql -u username -p database_name < migrations/snake_case_migration.sql
```

### 3. 验证迁移结果
```sql
-- 验证字段是否正确更改
SELECT 
  TABLE_NAME, 
  COLUMN_NAME, 
  DATA_TYPE 
FROM 
  INFORMATION_SCHEMA.COLUMNS 
WHERE 
  TABLE_SCHEMA = 'your_database_name' 
  AND TABLE_NAME IN ('t_users', 't_face', 'photo_face_info', 't_files', 't_activity')
  AND COLUMN_NAME LIKE '%\_%'
ORDER BY 
  TABLE_NAME, ORDINAL_POSITION;
```

## 应用部署步骤

### 1. 停止现有服务
```bash
# 停止Go服务
systemctl stop mpm-go
# 或使用其他方式停止服务
```

### 2. 更新代码
```bash
# 拉取最新代码
git pull origin master

# 重新编译Go程序
go build -o mpm-go

# 编译前端代码
cd web
npm run build
cd ..
```

### 3. 启动服务
```bash
# 启动Go服务
systemctl start mpm-go
# 或使用其他方式启动服务
```

## 兼容性说明

⚠️ **重要提示**：
1. 此次修改**不向后兼容**，必须同时更新数据库结构和应用代码
2. 建议在**低峰期**执行迁移
3. 迁移前务必**备份数据库**
4. 迁移后需要**重启所有相关服务**
5. 建议先在**测试环境**验证后再在生产环境执行
6. SQL字符串中的字段名也已全部更新，确保数据库和代码同步

## 回滚方案

如果迁移失败需要回滚，可以执行以下步骤：

### 1. 恢复数据库
```bash
mysql -u username -p database_name < backup_YYYYMMDD_HHMMSS.sql
```

### 2. 回滚代码
```bash
git revert <commit-hash>
# 或
git reset --hard <previous-commit>
```

### 3. 重新编译和部署
按照正常的部署流程重新编译和启动服务

## 测试清单

迁移完成后，请测试以下功能：

- [ ] 用户登录功能
- [ ] 人脸识别和管理
- [ ] 文件夹浏览和操作
- [ ] 活动创建和编辑
- [ ] 照片上传和浏览
- [ ] 照片详情查看
- [ ] 人脸标注和命名
- [ ] 地理位置信息显示
- [ ] 标签管理
- [ ] 回收站功能
- [ ] 批量操作功能

## 修改文件清单

### Go文件（14个）
1. model/t_user.go
2. model/t_face.go
3. model/t_face_info.go
4. model/t_file.go
5. model/t_activity.go
6. face.go
7. folder.go
8. pics.go
9. pics_date.go
10. pics_service.go
11. tags.go
12. user.go
13. activity_test.go
14. go_java_diff_test.go

### 前端文件（7个）
1. web/src/api/index.ts
2. web/src/stores/user.ts
3. web/src/views/Faces.vue
4. web/src/views/Activities.vue
5. web/src/views/Folders.vue
6. web/src/components/FolderTree.vue
7. web/src/components/PhotoDetail.vue

## 联系方式

如有问题，请联系开发团队。

## 变更历史

- 2026-01-15: 初始版本 - 完成CamelCase到snake_case的迁移
- 2026-01-15: 更新版本 - 完成所有SQL字符串中的字段名修改

