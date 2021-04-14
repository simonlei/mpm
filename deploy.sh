git pull
mvn package -DskipTests
mv mpm-server/target/mpm-server-1.0-SNAPSHOT.war ../jetty9/webapps/mpm.war
# stop jetty
cd ../jetty9/
java -DSTOP.PORT=27077 -DSTOP.KEY=stop123 -jar start.jar --stop
cd ../jetty9/webapps/
mv ROOT ROOT_bak
mkdir ROOT
mv mpm.war ROOT
cd ROOT
jar xf mpm.war
# ../jetty9
cd ../..
cp ~/config/application.properties webapps/ROOT/WEB-INF/classes/
# start jetty
nohup java -Djetty.http.port=27277 -DSTOP.PORT=27077 -DSTOP.KEY=stop123 -jar start.jar  > nohup.log &
rm -rf ROOT_bak