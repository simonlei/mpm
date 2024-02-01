git pull
cd mpm-vue3
# install flutter first https://snapcraft.io/install/flutter/centos, after install, flutter at /snap/bin
npm run build
cd ../mpm-server
cp ../../config/application.properties src/main/resources
mvn clean package -DskipTests
# stop jetty
killall -9 java
cd ../../running
mkdir -p bak
mv `ls -1 mpm-server-*.jar | head -n1` bak/mpm.jar.`date "+%Y.%m.%d"`
mv web bak/web.`date "+%Y.%m.%d"`
mv ../mpm/mpm-server/target/mpm-server-*.jar .
cp -R ../mpm/mpm-vue3/dist/ web
# start jetty
java -jar `ls -1 mpm-server-*.jar | head -n1` > nohup.log &
#nohup java -Djetty.http.port=27277 -DSTOP.PORT=27077 -DSTOP.KEY=stop123 -jar start.jar  > nohup.log &
