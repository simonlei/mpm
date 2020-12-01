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

