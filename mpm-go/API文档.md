# MPM-GO API 文档

## 项目简介

MPM-GO 是一个照片管理系统的后端服务，基于 Gin 框架开发，提供照片存储、管理、人脸识别、地理位置等功能。

## 基础信息

- **框架**: Gin
- **认证方式**: Signature 签名验证（Header中的`Signature`字段）
- **响应格式**: JSON-RPC 2.0 风格
- **响应结构**:
  ```json
  {
    "code": 0,
    "data": {...}
  }
  ```

## API 接口列表

### 1. 活动管理 (Activity)

#### 1.1 获取所有活动
- **URL**: `/api/getActivities`
- **Method**: `POST`
- **请求参数**: 无
- **响应示例**:
  ```json
  {
    "code": 0,
    "data": [
      {
        "id": 1,
        "name": "活动名称",
        "description": "活动描述",
        "start_date": "2024-01-01",
        "end_date": "2024-01-05",
        "latitude": 39.9042,
        "longitude": 116.4074
      }
    ]
  }
  ```

#### 1.2 创建或更新活动
- **URL**: `/api/createOrUpdateActivity`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "activity": {
      "id": 0,  // 0 表示新建，非0表示更新
      "name": "活动名称",
      "description": "活动描述",
      "start_date": "2024-01-01",
      "end_date": "2024-01-05",
      "latitude": 39.9042,
      "longitude": 116.4074
    },
    "fromPhoto": 123  // 关联的照片ID
  }
  ```
- **响应**: 返回创建/更新后的活动对象

#### 1.3 删除活动
- **URL**: `/api/deleteActivity`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "id": 1
  }
  ```
- **响应**: 返回删除的活动对象

---

### 2. 照片日期管理 (Photos Date)

#### 2.1 获取照片日期树
- **URL**: `/api/getPicsDate`
- **Method**: `POST`
- **描述**: 获取按年月组织的照片树形结构，包含活动信息
- **请求参数**:
  ```json
  {
    "trashed": false,  // 是否查询回收站
    "star": false      // 是否只查询收藏
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 0,
    "data": [
      {
        "id": 2024,
        "year": 2024,
        "title": "2024年(120)",
        "children": [
          {
            "id": 202401,
            "year": 2024,
            "month": 1,
            "photoCount": 50,
            "title": "1月(50)",
            "children": [
              {
                "id": 1000001,  // 活动ID (1000000 + activityId)
                "title": "1月1日-春游(30)"
              }
            ]
          }
        ]
      }
    ]
  }
  ```

---

### 3. 照片管理 (Photos)

#### 3.1 获取照片列表
- **URL**: `/api/getPics`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "star": false,        // 是否收藏
    "video": false,       // 是否视频
    "trashed": false,     // 是否在回收站
    "idOnly": false,      // 是否只返回ID
    "start": 0,           // 分页起始位置
    "size": 75,           // 每页数量
    "dateKey": "",        // 日期过滤: 2024(年) / 202401(月) / 1000001(活动ID+1000000)
    "path": "",           // 路径过滤
    "tag": "",            // 标签过滤
    "face_id": 0,          // 人脸ID过滤
    "order": "id",        // 排序字段，前缀"-"表示降序
    "idRank": 0           // 获取指定ID的排名
  }
  ```
  排序字段如下：
  ```js
  const orderOptions = [
    { label: 'ID 升序', value: 'id' },
    { label: 'ID 降序', value: '-id' },
    { label: '日期 升序', value: 'taken_date' },
    { label: '日期 降序', value: '-taken_date' },
    { label: '大小 升序', value: 'size' },
    { label: '大小 降序', value: '-size' },
    { label: '宽度 升序', value: 'width' },
    { label: '宽度 降序', value: '-width' },
    { label: '高度 升序', value: 'height' },
    { label: '高度 降序', value: '-height' }
  ```

- **响应示例**:
  ```json
  {
    "code": 0,
    "data": {
      "totalRows": 1000,
      "startRow": 0,
      "endRow": 75,
      "data": [
        {
          "id": 1,
          "name": "photo_name.jpg",
          "takenDate": "2024-01-01T10:30:00Z",
          "latitude": 39.9042,
          "longitude": 116.4074,
          "address": "北京市朝阳区",
          "width": 1920,
          "height": 1080,
          "rotate": 0,
          "star": false,
          "trashed": false,
          "thumb": "small/photo_name.jpg/thumb",
          "activity": 1,
          "activityDesc": "2024-01-01 春游 活动描述",
          "tag": "风景,旅游"
        }
      ]
    }
  }
  ```

#### 3.2 根据ID获取照片详情
- **URL**: `/api/getPhotoById`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "id": 1
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 0,
    "data": {
      "id": 1,
      "name": "photo_name.jpg",
      "takenDate": "2024-01-01T10:30:00Z",
      "latitude": 39.9042,
      "longitude": 116.4074,
      "address": "北京市朝阳区",
      "width": 1920,
      "height": 1080,
      "rotate": 0,
      "star": false,
      "trashed": false,
      "thumb": "small/photo_name.jpg/thumb",
      "activity": 1,
      "activityDesc": "2024-01-01 春游 活动描述",
      "tags": "风景,旅游",
      "description": "照片描述",
      "mediaType": "image",
      "size": 2048576,
      "duration": 0
    }
  }
  ```

#### 3.3 获取照片数量
- **URL**: `/api/getCount`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "trashed": false
  }
  ```
- **响应**: 返回照片总数

#### 3.4 更新照片信息
- **URL**: `/api/updateImage`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "id": 1,
    "latitude": 39.9042,
    "longitude": 116.4074,
    "takenDate": "2024-01-01T10:30:00Z",
    "star": true,
    "activity": 1,
    "tags": "风景,旅游",
    "rotate": 90
  }
  ```
- **响应**: 返回更新后的照片对象

#### 3.5 移入/移出回收站
- **URL**: `/api/trashPhotos`
- **Method**: `POST`
- **请求参数**:
  ```json
  [1, 23, 45]
  ```
- **响应**: 返回影响的行数

#### 3.6 清空回收站
- **URL**: `/api/emptyTrash`
- **Method**: `POST`
- **描述**: 异步任务，永久删除回收站中的所有照片
- **请求参数**: 无
- **响应**:
  ```json
  {
    "code": 0,
    "data": {
      "taskId": "uuid-string"
    }
  }
  ```

---

### 4. 标签管理 (Tags)

#### 4.1 获取所有标签
- **URL**: `/api/getAllTags`
- **Method**: `POST`
- **请求参数**: 无
- **响应**: 返回标签名称数组
  ```json
  {
    "code": 0,
    "data": ["风景", "旅游", "人物", "美食"]
  }
  ```

---

### 5. 文件夹管理 (Folders)

#### 5.1 获取文件夹数据
- **URL**: `/api/getFoldersData`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "trashed": false,
    "star": false,
    "parent_id": 0  // 0或负数表示根目录
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 0,
    "data": [
      {
        "id": 1,
        "path": "/2024",
        "title": "2024(120)",
        "parentid": -1
      }
    ]
  }
  ```

#### 5.2 切换文件夹回收站状态
- **URL**: `/api/switchTrashFolder`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "to": true,           // true移入回收站，false恢复
    "path": "/2024/春游"
  }
  ```
- **响应**: 返回影响的行数

#### 5.3 更新文件夹日期
- **URL**: `/api/updateFolderDate`
- **Method**: `POST`
- **描述**: 批量更新指定路径下所有照片的拍摄日期
- **请求参数**:
  ```json
  {
    "path": "/2024/春游",
    "toDate": "2024-01-01T10:00:00Z"
  }
  ```
- **响应**: 返回影响的行数

#### 5.4 更新文件夹地理位置
- **URL**: `/api/updateFolderGis`
- **Method**: `POST`
- **描述**: 批量更新指定路径下所有照片的地理位置
- **请求参数**:
  ```json
  {
    "path": "/2024/春游",
    "latitude": 39.9042,
    "longitude": 116.4074
  }
  ```
- **响应**: 返回影响的行数（自动获取地址信息）

#### 5.5 移动文件夹
- **URL**: `/api/moveFolder`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "fromPath": "/2024/春游",
    "toId": "2",       // 目标父文件夹ID
    "merge": false     // 是否合并到目标文件夹
  }
  ```
- **响应**: 返回操作结果

---

### 6. 人脸识别管理 (Faces)

#### 6.1 获取人脸列表
- **URL**: `/api/getFaces`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "showHidden": false,    // 是否显示隐藏的人脸
    "page": 1,
    "size": 20,
    "nameFilter": ""        // 名称过滤
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 0,
    "data": {
      "total": 100,
      "faces": [
        {
          "person_id": "person_001",
          "face_id": 1,
          "name": "张三",
          "selected_face": 123,  // 选定的代表人脸信息ID
          "collected": 1,        // 是否收藏
          "hidden": 0,           // 是否隐藏
          "count": 50            // 照片数量
        }
      ]
    }
  }
  ```

#### 6.2 获取有名称的人脸列表
- **URL**: `/api/getFacesWithName`
- **Method**: `POST`
- **描述**: 获取已命名的人脸列表（用于下拉选择等）
- **请求参数**: 无
- **响应示例**:
  ```json
  {
    "code": 0,
    "data": [
      {
        "face_id": 1,
        "name": "张三"
      },
      {
        "face_id": 2,
        "name": "李四"
      }
    ]
  }
  ```

#### 6.3 获取照片中的人脸
- **URL**: `/api/getFacesForPhoto`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "id": 1  // 照片ID
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 0,
    "data": [
      {
        "id": 1,
        "face_id": 5,
        "x": 100,
        "y": 150,
        "width": 80,
        "height": 100,
        "name": "张三"
      }
    ]
  }
  ```

#### 6.4 获取人脸图片
- **URL**: `/get_face_img/:face_id/:infoId`
- **Method**: `GET`
- **参数说明**:
  - `face_id`: 人脸ID
  - `infoId`: 人脸信息ID（可选，传0则自动选择最大的人脸）
- **响应**: 直接返回图片二进制流

#### 6.5 更新人脸信息
- **URL**: `/api/updateFace`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "face_id": 1,
    "name": "张三",           // 可选
    "selected_face": 123,      // 可选，设置代表人脸
    "hidden": true,           // 可选，是否隐藏
    "collected": true         // 可选，是否收藏
  }
  ```
- **响应**: 返回操作结果（true/false）

#### 6.6 合并人脸
- **URL**: `/api/mergeFace`
- **Method**: `POST`
- **描述**: 将一个人脸合并到另一个人脸，删除源人脸
- **请求参数**:
  ```json
  {
    "from": 1,  // 源人脸ID
    "to": 2     // 目标人脸ID
  }
  ```
- **响应**: 返回操作结果

#### 6.7 删除照片中的人脸信息
- **URL**: `/api/removePhotoFaceInfo`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "id": 1  // 人脸信息ID
  }
  ```
- **响应**: 返回删除的数量

#### 6.8 重新扫描人脸
- **URL**: `/api/rescanFace`
- **Method**: `POST`
- **描述**: 重新对指定照片进行人脸识别
- **请求参数**:
  ```json
  {
    "id": 1  // 照片ID
  }
  ```
- **响应**: 返回操作结果

---

### 7. 地理信息 (GIS)

#### 7.1 加载地图标记数据
- **URL**: `/geo_json_api/loadMarkersGeoJson`
- **Method**: `GET`
- **描述**: 获取所有照片的地理位置信息（GeoJSON格式）
- **响应示例**:
  ```json
  {
    "type": "FeatureCollection",
    "features": [
      {
        "type": "Feature",
        "properties": {
          "id": 1,
          "name": "photo_name.jpg",
          "latitude": 39.9042,
          "longitude": 116.4074,
          "rotate": 0
        },
        "geometry": {
          "type": "Point",
          "coordinates": [116.4074, 39.9042]
        }
      }
    ]
  }
  ```

---

### 8. 照片上传 (Upload)

#### 8.1 上传照片
- **URL**: `/api/uploadPhoto`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **请求参数**:
  - `file`: 文件对象
  - `lastModified`: 最后修改时间（字符串）
  - `batchId`: 批次ID
- **响应**:
  ```json
  {
    "code": 0,
    "data": 0
  }
  ```

---

### 9. 用户管理 (User)

#### 9.1 验证密码/登录
- **URL**: `/api/checkPassword`
- **Method**: `POST`
- **描述**: 验证用户密码，成功后返回签名
- **请求参数**:
  ```json
  {
    "account": "admin",
    "passwd": "password123"
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 0,
    "data": {
      "id": 1,
      "account": "admin",
      "name": "管理员",
      "is_admin": true,
      "face_id": 0,
      "signature": "generated_signature_string"
    }
  }
  ```

#### 9.2 创建或更新用户
- **URL**: `/api/createOrUpdateUser`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "id": 0,              // 0表示创建，非0表示更新
    "account": "user01",
    "name": "用户一",
    "passwd": "password", // 创建时必填，更新时可选
    "is_admin": false,
    "face_id": 5
  }
  ```
- **响应**: 返回操作结果

#### 9.3 获取用户列表
- **URL**: `/api/loadUsers`
- **Method**: `POST`
- **请求参数**: 无
- **响应**: 返回用户数组（不含密码和salt）

#### 9.4 获取单个用户
- **URL**: `/api/loadUser`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "id": 1
  }
  ```
- **响应**: 返回用户对象（不含密码和salt）

#### 9.5 删除用户
- **URL**: `/api/deleteUser`
- **Method**: `POST`
- **请求参数**:
  ```json
  {
    "id": 1
  }
  ```
- **响应**: 返回操作结果

---

### 10. 任务进度查询 (Progress)

#### 10.1 获取任务进度
- **URL**: `/api/getProgress/:taskId`
- **Method**: `GET`
- **描述**: 查询异步任务的执行进度（如清空回收站）
- **参数**: `taskId` - 任务ID
- **响应示例**:
  ```json
  {
    "code": 0,
    "data": {
      "total": 100,
      "count": 50,
      "progress": 50
    }
  }
  ```

---

### 11. COS 代理 (CDN/存储)

#### 11.1 代理 COS 资源
- **URL**: `/cos/*path`
- **Method**: `GET`
- **描述**: 代理腾讯云 COS 上的资源，支持图片处理参数
- **示例**: `/cos/small/photo_name.jpg?imageMogr2/rotate/90`
- **响应**: 返回资源二进制流

---

### 12. 工具接口 (Utils)

#### 12.1 生成 TOTP 验证码
- **URL**: `/api/totp`
- **Method**: `POST`
- **描述**: 生成基于时间的一次性密码（TOTP）
- **请求参数**: 无
- **响应**:
  ```json
  {
    "code": 0,
    "data": "123456"
  }
  ```

---

## 认证机制

除了 `/api/checkPassword` 接口外，所有 `/api/*` 开头的接口都需要在 HTTP Header 中携带签名：

```
Signature: md5(account + login_token + timestamp)
Account: user_account
```

其中 `login_token` 存储在数据库的 `meta` 表中，`c_key='login_token'`。

---

## 错误处理

- **成功**: `code: 0`
- **错误**: `code: -20001`，错误信息在 `error.message` 中

错误响应示例：
```json
{
  "jsonrpc": "2.0",
  "error": {
    "message": "错误描述",
    "code": -20001
  }
}
```

---

## 特殊功能说明

### 人脸识别
系统使用腾讯云人脸识别服务（IAI），自动检测照片中的人脸并进行分组。

### 图片处理
通过 COS 万象服务进行图片处理，支持缩略图、旋转、格式转换等。

### 异步任务
部分耗时操作（如清空回收站）采用异步任务模式，通过 `taskId` 查询进度。

### 地理位置
集成腾讯地图API，自动根据经纬度获取地址信息。

---

## 数据模型

### 照片 (TPhoto)
- 包含：拍摄日期、地理位置、旋转角度、收藏状态、回收站状态、关联活动等

### 活动 (TActivity)
- 时间范围、地理位置、描述等

### 人脸 (TFace)
- 对应腾讯云的 person_id、face_id
- 支持命名、隐藏、收藏

### 文件夹 (TFile)
- 树形结构，支持文件和目录

### 用户 (TUser)
- 账号、密码（SHA256+Salt）、管理员标识、关联人脸

---

## 配置项

系统使用 Viper 进行配置管理，主要配置项包括：

- `server.port`: 服务端口
- `static.base`: 静态文件目录
- `cos.bucket`: COS 存储桶
- `cos.region`: COS 区域
- `cos.secretId`: COS 密钥ID
- `cos.secretKey`: COS 密钥
- `jdbc.username`: 数据库用户名
- `jdbc.password`: 数据库密码
- `qqlbsKey`: 腾讯地图API Key
- `qqlbsToken`: 腾讯地图API Token
- `totpSecretKey`: TOTP 密钥
- `isDev`: 是否开发环境

---

## 部署说明

1. 配置数据库连接
2. 配置腾讯云 COS 和 IAI 服务
3. 配置腾讯地图 API
4. 运行服务：`./mpm-go`
5. 默认端口从配置文件读取

---

*文档生成时间: 2025-12-10*
