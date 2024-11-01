nohup ./mpm-go >> /logs/mpm-go.log 2>> /logs/mpm-go-error.log &
java --add-opens java.base/java.lang=ALL-UNNAMED -jar mpm.jar >> /logs/mpm.log 2>> /logs/error.log
