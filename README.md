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
mvn package -DskipTests
或者
mvn package -Dgwt.style=PRETTY -DskipTests
```

然后用jetty-runner 运行打包后的war包即可。

#### 部署

1. 下载jetty9，并展开
2. clone 源代码，放在 mpm 目录下
3. 在 ~/config/ 目录下创建 application.properties，参考源码中的 application.properties.template
4. 进入 mpm 目录，运行 sh ./deploy.sh

#### Release:

v1.0.0 - Apr.12,2021
- [x] 删除图片后更新计数
- [x] 支持批量删除图片
- [x] 双击/回车放大图片
- [x] 图片展示时支持上一张、下一张、删除等操作
- [x] 当前图片删除后，自动加载下一张
- [x] if it's last one, get prev one.
- [x] 如果所有图片都删除了，退出大图片
- [x] esc 退出大图片
- [x] 展示每次导入的左边树
- [x] 使用smartgwtee
- [x] 把物理删除变成逻辑删除
- [x] 修复获取总数的问题
- [x] 左边日期树数量按是否删除过滤
- [x] 左边日期树选中后过滤图片
- [x] 左边目录树选中后过滤图片
- [x] 目录树层级貌似还不太对，需要优化
- [x] 导入时，有相同图片不要直接抛弃，而是建立关联
- [x] 图片默认按id倒序展示
- [x] 可以选择图片排序的方式
- [x] 导入的树也改成用smartgwtee的ds
- [x] 支持清空回收站
- [x] 导入完成后刷新页面
- [x] 左侧树支持"全部"
- [x] 支持放大/缩小图片
- [x] 支持在放大的情况下用 HJKL 来移动图片
- [x] 详情时展示图片信息
- [x] 列表时展示图片信息
- [x] 物理删除图片时，相关的File都要一起删除
- [x] Progress 可以做成统一的
- [x] 长远看，要考虑数据库同步机制
- [x] 尝试一下使用浏览器上传文件夹的功能
- [x] 上传到cos的，要支持按目录结构上传
- [x] 上传到cos时，过滤非照片和视频的文件
- [x] 上传到cos之后，刷数据库
- [x] 上传的时候要控制一下，避免重复创建
- [x] 上传完成后要刷新当前页面的内容
- [x] 身份认证之后才能访问
- [x] 数据库做一个同步备份
- [x] 定时任务，检查日期/geo地址信息等

v1.0.1 - Apr.17,2021
- [x] 支持视频上传
- [x] 支持视频展示
- [x] 部署脚本和文档
- [x] 日志输出到文件
- [x] 视频直接用万象API来截图，不需要配置cos上的流程，也不需要循环等待
- [x] 记录视频时长
- [x] cos上传出错之后，上报到后台并记录
- [x] tile中的视频加上一个视频的标记
- [x] tile中的视频加上一个视频的时长
- [x] bug: 如果takenDate是null，那么就用当天的日期 
- [x] bug: cos 版本和 万象版本不一致导致video不正常
- [x] bug: tile会复用，要删掉不必要的视频标签

v1.0.2
- [x] 重构代码，删除不必要的代码
- [ ] 支持删除某个目录下的所有照片
- [x] 年份后面也展示照片数
- [x] 文件目录后面也展示照片数
- [x] 按目录展示，子目录下的照片也展示出来
- [x] 照片排序也要按新的datasource来支持
- [x] 支持修改图片的日期
- [x] 支持拷贝图片的gis信息 
- [x] 支持修改图片的gis信息
- [x] 图片gis信息被修改之后，要重新调用一下address接口
- [x] 支持按目录修改图片的日期
- [x] 支持按目录修改图片的gis信息
- [ ] 如果某个目录下所有照片都没了（包括回收站），把目录也删了
- [x] 支持照片的旋转
- [x] 全屏状态下也支持旋转

- [ ] 上传文件的时候，左侧的提醒消退的太慢了
- [ ] 加上删除/修改时间/修改gis的日志
- [ ] 可以undo 删除/修改时间/修改gis 的操作
- [ ] 记住上一次看图片的位置
- [ ] 跳转到对应的图片位置
- [ ] 数据库密码去掉
- [ ] 用更复杂的用户名/密码来登陆
- [ ] 使用cookie来记录当前登陆的用户名
- [ ] 自动调整照片的方向
- [ ] 导入的动作也改成用DMI
- [ ] 可以支持按导入的目录来进行授权让其他人看
- [ ] 研究一下部署模式，如何方便的设置各类参数
- [ ] 手机上upload可以选择文件，要考虑如何兼容
- [ ] 加上star标记
- [ ] 应该有办法获取到video的拍摄时间吧
- [ ] 支持地图
- [ ] 支持往年今日

#### 更长远计划

- [ ] 人脸识别？
- [ ] 圈人？

