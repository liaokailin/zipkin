echo off
for /l %%i in (1,1,4) do start "service%%i" /i java -Dcom.zipkin.serviceName=service%%i -Dserver.port=909%%i -jar ..\target\service.jar
