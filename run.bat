@echo off
title Student Performance Analytics System - Build & Run
cd /d "%~dp0"
echo ==========================================================
echo  Student Performance Analytics System - Compiler & Runner
echo ==========================================================
echo.

if not exist bin (
    echo Creating build output directory: bin/...
    mkdir bin
)

echo Compiling Java code using OpenJDK 26...
"C:\Users\Computer solution\.jdks\openjdk-26.0.1\bin\javac.exe" -encoding UTF-8 -d bin Main.java model/*.java dao/*.java service/*.java analytics/*.java util/*.java view/*.java

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed! Please inspect compilation errors above.
    pause
    exit /b %errorlevel%
)

echo.
echo ==========================================================
echo  Launch Successful! Running Student Performance Analytics System...
echo ==========================================================
echo.

"C:\Users\Computer solution\.jdks\openjdk-26.0.1\bin\java.exe" -cp bin Main

echo.
echo Application closed.
pause
