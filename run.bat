@echo off
title Student Performance Analytics System - Build and Run
cd /d "%~dp0"

echo ==========================================================
echo  Student Performance Analytics System - Compiler and Runner
echo ==========================================================
echo.

:: 1. Try PATH
set "JAVAC_CMD=javac"
set "JAVA_CMD=java"
where javac >nul 2>nul
if %errorlevel% equ 0 (
    echo Detected 'javac' in PATH.
    goto :compile
)

:: 2. Try JAVA_HOME
if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        echo Detected JDK from JAVA_HOME: %JAVA_HOME%
        goto :compile
    )
)

:: 3. Try to look in user profile .jdks folder (e.g. IntelliJ download directory)
if exist "%USERPROFILE%\.jdks" (
    for /d %%d in ("%USERPROFILE%\.jdks\*") do (
        if exist "%%d\bin\javac.exe" (
            set "JAVAC_CMD=%%d\bin\javac.exe"
            set "JAVA_CMD=%%d\bin\java.exe"
            echo Detected JDK in user profile .jdks folder: %%d
            goto :compile
        )
    )
)

:: 4. Try common Program Files JDK directories
if exist "%ProgramFiles%\Java" (
    for /d %%d in ("%ProgramFiles%\Java\jdk*") do (
        if exist "%%d\bin\javac.exe" (
            set "JAVAC_CMD=%%d\bin\javac.exe"
            set "JAVA_CMD=%%d\bin\java.exe"
            echo Detected JDK in Program Files: %%d
            goto :compile
        )
    )
)

echo [ERROR] Java Development Kit (JDK) could not be detected.
echo Please ensure 'javac' is in your system PATH, or set the JAVA_HOME environment variable.
pause
exit /b 1

:compile
:: Create bin directory if it doesn't exist
if not exist bin (
    echo Creating build output directory: bin/...
    mkdir bin
)

echo.
echo Compiling all Java source files...
powershell -Command "Get-ChildItem -Path . -Filter *.java -Recurse | ForEach-Object { [char]34 + $_.FullName.Replace([char]92, [char]47) + [char]34 } | Out-File -FilePath sources.txt -Encoding ascii"
"%JAVAC_CMD%" -encoding UTF-8 -d bin "@sources.txt"
set COMPILE_STATUS=%errorlevel%
del sources.txt

if %COMPILE_STATUS% neq 0 (
    echo.
    echo [ERROR] Compilation failed! Please inspect compilation errors above.
    pause
    exit /b %COMPILE_STATUS%
)

echo.
echo ==========================================================
echo  Launch Successful! Running Student Performance Analytics System...
echo ==========================================================
echo.

"%JAVA_CMD%" -cp bin Main

echo.
echo Application closed.
pause
