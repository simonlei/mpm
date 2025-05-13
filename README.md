# mpm

## 介绍

My Photo Manager，个人相册管理

## 软件架构

- 后端使用 go + gin + gorm
- 前端使用 vue3 + TDesign
- 图片存储采用腾讯云 COS，需要自行购买

## 开发

1. 准备好数据库，按/mpm-server/src/main/sql目录下的版本次序执行sql语句
2. 在本地 resources 目录下建立 application.properties 文件，参考 /mpm-server/src/main/resources/application.properties.template
3. 其中的 isDev要设置为true
4. 后端使用 go run . 来运行
5. 前端使用 npm run dev 来运行

## 本地部署

1. 准备好数据库，按/mpm-server/src/main/sql目录下的版本次序执行sql语句
2. 在源码的上一级目录下创建 config 目录，并创建 application.properties，参考源码中的 /mpm-server/src/main/resources/application.properties.template
3. 运行 deploy.sh 来进行本地构建和部署
4. 浏览器访问 application.properties 配置的端口

## Docker 部署

1. 准备好数据库，按/mpm-server/src/main/sql目录下的版本次序执行sql语句
2. 创建 config 目录，并创建 application.properties，参考源码中的 /mpm-server/src/main/resources/application.properties.template
3. 使用 docker run  --net=host --name mpm -v /home/simon/config:/config -v /home/simon/logs:/logs -d ghcr.io/simonlei/mpm:xxx 来启动docker镜像，其中 xxx 代表的版本号参见 https://github.com/simonlei/mpm/pkgs/container/mpm
4. 浏览器访问 application.properties 配置的端口

## 初始账号密码

默认账号密码都是 admin，建议部署完成后立即改密码，或者另外创建一个管理员账号，删除 admin 账号

## mysql 可能的问题

``` references column 'photohome.t_photos.takenDate' which is not in SELECT list```

那么需要把 sql-mode 当中的 ONLY_FULL_GROUP_BY 去掉

## 腾讯云的收费问题

1. cos 的存储费用
2. 图片的缩放等万象接口的费用（照片量不大的话，在每个月的免费额度范围内）
3. 如果需要使用avif高效压缩，在配置当中打开 smallphoto.format=avif
4. 图片当中的人脸识别费用（照片量不大的话，在每个月的免费额度范围内）

## Release:

[RELEASE 情况](RELEASE.md)
