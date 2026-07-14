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

# 1. Detect javac
$javacCmd = "javac"
$javaCmd = "java"
$javacInPath = Get-Command javac -ErrorAction SilentlyContinue

if ($javacInPath) {
    Write-Host "Detected 'javac' in PATH." -ForegroundColor Green
} else {
    # 2. Try JAVA_HOME
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
        $javacCmd = "$env:JAVA_HOME\bin\javac.exe"
        $javaCmd = "$env:JAVA_HOME\bin\java.exe"
        Write-Host "Detected Java from JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
    }
    # 3. Try user profile .jdks folder
    elseif (Test-Path "$env:USERPROFILE\.jdks") {
        $jdks = Get-ChildItem "$env:USERPROFILE\.jdks" -Directory
        $found = $false
        foreach ($jdk in $jdks) {
            if (Test-Path "$($jdk.FullName)\bin\javac.exe") {
                $javacCmd = "$($jdk.FullName)\bin\javac.exe"
                $javaCmd = "$($jdk.FullName)\bin\java.exe"
                Write-Host "Detected Java in user profile JDKs: $($jdk.FullName)" -ForegroundColor Green
                $found = $true
                break
            }
        }
        if (!$found) {
            Write-Error "JDK not found in PATH, JAVA_HOME, or USERPROFILE/.jdks"
            Read-Host "Press Enter to exit..."
            exit 1
        }
    }
    # 4. Try standard Program Files locations
    elseif (Test-Path "$env:ProgramFiles\Java") {
        $jdks = Get-ChildItem "$env:ProgramFiles\Java\jdk*" -Directory
        $found = $false
        foreach ($jdk in $jdks) {
            if (Test-Path "$($jdk.FullName)\bin\javac.exe") {
                $javacCmd = "$($jdk.FullName)\bin\javac.exe"
                $javaCmd = "$($jdk.FullName)\bin\java.exe"
                Write-Host "Detected Java in Program Files: $($jdk.FullName)" -ForegroundColor Green
                $found = $true
                break
            }
        }
        if (!$found) {
            Write-Error "JDK not found in PATH, JAVA_HOME, or Program Files"
            Read-Host "Press Enter to exit..."
            exit 1
        }
    } else {
        Write-Error "Java Development Kit (JDK) not found. Please install JDK 17+ and add it to your PATH."
        Read-Host "Press Enter to exit..."
        exit 1
    }
}

Write-Host "Compiling all Java source files..." -ForegroundColor Gray

# Find all Java files recursively
$javaFiles = Get-ChildItem -Path . -Filter *.java -Recurse | ForEach-Object { '"{0}"' -f $_.FullName.Replace('\', '/') }
if ($javaFiles.Count -eq 0) {
    Write-Host "[ERROR] No Java source files found!" -ForegroundColor Red
    Read-Host "Press Enter to exit..."
    exit 1
}

# Write files list to compile to avoid command line length limits
$javaFiles | Out-File -FilePath sources.txt -Encoding ascii

& $javacCmd -encoding UTF-8 -d bin "@sources.txt"
$compileStatus = $LASTEXITCODE
Remove-Item -Path sources.txt -ErrorAction SilentlyContinue

if ($compileStatus -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Compilation failed! Please inspect compilation errors above." -ForegroundColor Red
    Read-Host "Press Enter to exit..."
    exit $compileStatus
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "  Launch Successful! Running Student Performance Analytics..." -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""

& $javaCmd -cp bin Main

Write-Host ""
Write-Host "Application closed." -ForegroundColor Gray
Read-Host "Press Enter to exit..."
