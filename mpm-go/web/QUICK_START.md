# 快速启动指南

## 一、环境准备

### 1. 检查 Node.js 版本

```bash
node -v  # 需要 >= 16
npm -v   # 需要 >= 7
```

如果版本不符，请安装最新的 LTS 版本：
- 官网下载：https://nodejs.org/
- 或使用 nvm：`nvm install --lts`

## 二、安装依赖

```bash
cd /root/mpm/mpm-go/web
npm install
```

如果安装较慢，已配置国内镜像源（.npmrc），也可以手动设置：

```bash
npm config set registry https://registry.npmmirror.com
```

## 三、启动开发服务器

### 1. 启动后端服务

确保 Go 后端服务已启动：

```bash
cd /root/mpm/mpm-go
./mpm-go
```

后端默认运行在 http://localhost:8080

### 2. 启动前端开发服务

```bash
cd /root/mpm/mpm-go/web
npm run dev
```

前端将运行在 http://localhost:3000

### 3. 访问应用

在浏览器中打开：http://localhost:3000

## 四、登录系统

使用后端配置的账号密码登录。

默认账号（根据后端数据库配置）：
- 账号：admin
- 密码：（请联系管理员获取）

## 五、功能说明

登录成功后，您可以：

1. **照片** - 浏览所有照片，支持过滤、搜索、收藏
2. **时间线** - 按时间查看照片
3. **地图** - 在地图上查看照片位置
4. **人脸** - 管理人脸识别结果
5. **活动** - 创建和管理照片活动
6. **文件夹** - 批量管理照片
7. **上传** - 上传新照片
8. **回收站** - 管理已删除的照片

## 六、构建生产版本

```bash
npm run build
```

构建产物在 `dist` 目录，可部署到任何静态文件服务器。

### 部署示例

#### 使用 Nginx

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/mpm-go/web/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
    }
    
    location /cos {
        proxy_pass http://localhost:8080;
    }
    
    location /geo_json_api {
        proxy_pass http://localhost:8080;
    }
    
    location /get_face_img {
        proxy_pass http://localhost:8080;
    }
}
```

#### 使用后端服务静态文件

修改后端 `main.go` 中的静态文件路径：

```go
base := "/path/to/mpm-go/web/dist"
```

然后重启后端服务，直接通过 http://localhost:8080 访问。

## 七、常见问题

### Q1: npm install 失败

**解决方案**：
```bash
# 清理缓存
npm cache clean --force

# 删除 node_modules 和 lock 文件
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### Q2: 端口被占用

**解决方案**：
修改 `vite.config.ts` 中的端口：
```typescript
server: {
  port: 3001  // 改为其他端口
}
```

### Q3: API 请求 403 错误

**原因**：签名验证失败

**解决方案**：
1. 检查是否已登录
2. 清除浏览器缓存和 localStorage
3. 重新登录

### Q4: 图片不显示

**可能原因**：
1. 后端 COS 配置不正确
2. 图片路径错误
3. 网络问题

**解决方案**：
1. 检查后端日志
2. 检查浏览器控制台网络请求
3. 确认图片 URL 是否可访问

### Q5: 地图不显示

**可能原因**：
1. Leaflet CSS 未正确加载
2. 无法访问 OpenStreetMap

**解决方案**：
1. 检查网络连接
2. 尝试更换地图瓦片源

### Q6: 上传失败

**可能原因**：
1. 文件过大
2. 后端存储空间不足
3. COS 配置错误

**解决方案**：
1. 检查文件大小限制
2. 查看后端日志
3. 确认 COS 配置

## 八、开发建议

### 代码规范

项目使用 TypeScript + ESLint，建议：

1. 安装 VSCode 插件：
   - Volar (Vue 3 支持)
   - ESLint
   - Prettier

2. 启用自动格式化：
   - 设置 -> Format On Save

### 调试技巧

1. **Vue DevTools**
   - 安装浏览器插件
   - 查看组件状态和事件

2. **Network 面板**
   - 查看 API 请求
   - 检查响应数据

3. **Console 日志**
   - 使用 `console.log` 调试
   - 查看错误堆栈

### 性能优化

1. **图片懒加载**
   - 已在照片列表中实现
   - 使用 `loading="lazy"` 属性

2. **分页加载**
   - 避免一次加载过多数据
   - 合理设置每页数量

3. **缓存策略**
   - 使用 localStorage 缓存用户信息
   - 合理使用 Pinia 状态管理

## 九、技术支持

如遇到问题：

1. 查看 README.md 文档
2. 查看 API文档.md
3. 检查后端日志
4. 检查浏览器控制台

## 十、更新日志

### v1.0.0 (2025-12-10)
- ✨ 初始版本发布
- 📸 照片浏览和管理
- 👤 人脸识别功能
- 🗺️ 地图展示
- 🎯 活动管理
- ⬆️ 批量上传
- 🗑️ 回收站功能

---

祝您使用愉快！🎉
