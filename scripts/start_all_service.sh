#!/bin/sh
for i in {1..4}; do nohup java -Dcom.zipkin.serviceName=service$i -Dserver.port=909$i -jar ..\target\service.jar; done;
