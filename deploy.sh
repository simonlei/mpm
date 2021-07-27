git pull
cd app
# install flutter first https://snapcraft.io/install/flutter/centos, after install, flutter at /snap/bin
flutter build web --dart-define=remote_addr=
cd ../mpm-server
cp ../../config/application.properties src/main/resources
mvn clean package -DskipTests
# stop jetty
killall -9 java
cd ../../running
mkdir -p bak
mv mpm.jar bak/mpm.jar.`date "+%Y.%m.%d"`
mv ../mpm/mpm-server/target/mpm-server-*.jar .
cp -R ../mpm/app/build/web/ web
# start jetty
java -jar `ls -1 mpm-server-*.jar | head -n1` > nohup.log &
#nohup java -Djetty.http.port=27277 -DSTOP.PORT=27077 -DSTOP.KEY=stop123 -jar start.jar  > nohup.log &
