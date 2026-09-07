@echo off
REM Build script for Bluetooth Manager EXE
REM Run this on Windows with Python installed

echo ============================================
echo   Bluetooth Manager - Build Script
echo ============================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo Please install Python 3.8+ from https://python.org
    pause
    exit /b 1
)

echo [1/4] Checking Python installation...
python --version

echo.
echo [2/4] Installing dependencies...
pip install -r requirements.txt
if errorlevel 1 (
    echo WARNING: Some dependencies may have failed to install
)

echo.
echo [3/4] Building executable with PyInstaller...
pyinstaller --onefile --windowed --name "BluetoothManager" --clean bluetooth_manager.py

if exist "dist\BluetoothManager.exe" (
    echo.
    echo ============================================
    echo   BUILD SUCCESSFUL!
    echo ============================================
    echo.
    echo Executable created: dist\BluetoothManager.exe
    echo.
    echo You can now copy this file to any Windows computer.
    echo No Python installation required on target machines.
    echo.
) else (
    echo.
    echo ============================================
    echo   BUILD FAILED
    echo ============================================
    echo.
    echo Check the error messages above for details.
    echo.
)

echo [4/4] Cleanup...
if exist "build" (
    rmdir /s /q build
)
if exist "BluetoothManager.spec" (
    del BluetoothManager.spec
)

echo.
echo Done!
pause
