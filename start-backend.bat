@echo off
title SmartSpend - Spring Boot Backend (Java 21)
echo ====================================================================
echo Starting SmartSpend Java 21 Spring Boot Backend...
echo ====================================================================

set JAVA_HOME=C:\Users\Sonaa.R\tools\jdk-21.0.6+7
set M2_HOME=C:\Users\Sonaa.R\tools\apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%

cd /d "%~dp0backend"

echo Using Java:
java -version
echo.
echo Starting Spring Boot on http://localhost:8080/api ...
call mvn spring-boot:run
pause
