@ECHO OFF
SETLOCAL

SET APP_HOME=%~dp0

SET DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

IF NOT "%JAVA_HOME%"=="" (
  SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
) ELSE (
  SET JAVA_EXE=java.exe
)

%JAVA_EXE% %DEFAULT_JVM_OPTS% -cp "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

ENDLOCAL
