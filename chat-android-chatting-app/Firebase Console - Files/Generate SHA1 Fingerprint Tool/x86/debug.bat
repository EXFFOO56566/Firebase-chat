@ECHO OFF
keytool -exportcert -list -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore
pause