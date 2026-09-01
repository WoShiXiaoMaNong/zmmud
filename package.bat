@echo off
mvn clean package && copy /Y "./target\zmmud-2.0-SNAPSHOT.jar" "%USERPROFILE%\Desktop\"
pause
