# PowerShell compiler and execution script for Student Performance Analytics System
Clear-Host
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Student Performance Analytics System - Compiler & Runner  " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host ""

if (!(Test-Path bin)) {
    Write-Host "Creating build output directory: bin/..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path bin | Out-Null
}

Write-Host "Compiling Java code using OpenJDK 26..." -ForegroundColor Gray

# Compile directly using relative paths
& "C:\Users\Computer solution\.jdks\openjdk-26.0.1\bin\javac.exe" -encoding UTF-8 -d bin Main.java model/*.java dao/*.java service/*.java analytics/*.java util/*.java view/*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Compilation failed! Please inspect compilation errors above." -ForegroundColor Red
    Read-Host "Press Enter to exit..."
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "  Launch Successful! Running Student Performance Analytics..." -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""

& "C:\Users\Computer solution\.jdks\openjdk-26.0.1\bin\java.exe" -cp bin Main

Write-Host ""
Write-Host "Application closed." -ForegroundColor Gray
Read-Host "Press Enter to exit..."
