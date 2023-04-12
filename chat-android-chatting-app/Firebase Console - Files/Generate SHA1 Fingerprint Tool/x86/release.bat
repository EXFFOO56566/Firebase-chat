@ECHO OFF
set /p rka="Enter Release Key Alias: "
set /p rkp="Enter Release Key Path: "
keytool -exportcert -list -alias %rka% -keystore %rkp%
pause
