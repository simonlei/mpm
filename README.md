# mpm

#### 介绍
My Photo Manager，个人相册管理

#### 软件架构

后端使用 nutzboot

前端使用 smartgwt

#### 开发

先启动gwt codeserver
```
mvn gwt:codeserver -am -pl *-client
```
再启动后台服务
```
mvn jetty:run -Pdev -pl *-server
```

然后访问 localhost:8080 即可

#### 运行

先打包
```
mvn package
```

然后用jetty-runner 运行打包后的war包即可。

TODO:

- [x] 删除图片后更新计数
- [x] 支持批量删除图片
- [x] 双击/回车放大图片
- [x] 图片展示时支持上一张、下一张、删除等操作
- [x] 当前图片删除后，自动加载下一张
- [x] if it's last one, get prev one.
- [x] 如果所有图片都删除了，退出大图片
- [x] esc 退出大图片
- [ ] 支持清空回收站
- [ ] 长远看，要考虑数据库同步机制
- [ ] 导入完成后刷新页面
- [x] 展示每次导入的左边树
- [ ] 支持放大/缩小图片
- [ ] 详情时展示图片信息
- [ ] 列表时展示图片信息
- [ ] 导入时，有相同图片不要直接抛弃，而是建立关联
- [ ] 物理删除图片时，相关的File都要一起删除
- [x] 使用smartgwtee
- [x] 把物理删除变成逻辑删除
- [x] 修复获取总数的问题
- [ ] 数据库密码去掉
- [x] 左边日期树数量按是否删除过滤
- [x] 左边日期树选中后过滤图片
- [x] 左边目录树选中后过滤图片
- [x] 目录树层级貌似还不太对，需要优化
- [ ] 图片默认按id倒序展示

