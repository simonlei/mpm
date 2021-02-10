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
- [ ] 支持清空回收站
- [ ] 长远看，要考虑数据库同步机制
- [x] 双击/回车放大图片
- [ ] 图片展示时支持上一张、下一张、删除等操作
- [x] 当前图片删除后，自动加载下一张
- [ ] if it's last one, get prev one.
- [ ] 如果所有图片都删除了，退出大图片
- [x] esc 退出大图片
- [ ] 导入完成后刷新页面
