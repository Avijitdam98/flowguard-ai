@REM ----------------------------------------------------------------------------
@REM FlowGuard Backend Maven Execution Script
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "CACHED_MVN=C:\Users\aviji\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd"

if exist "%CACHED_MVN%" (
    call "%CACHED_MVN%" %*
) else (
    where mvn >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        call mvn %*
    ) else (
        echo [ERROR] Maven not found.
        exit /b 1
    )
)

