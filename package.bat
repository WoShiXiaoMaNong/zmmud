@echo off
setlocal enabledelayedexpansion

:: 1. 执行 Maven 打包（使用 call 确保后续脚本继续执行）
echo [INFO] Starting Maven build...
call mvn clean package
if errorlevel 1 (
    echo [ERROR] Maven build failed!
    pause
    exit /b
)

:: 2. 判断并设置基础存放路径
:: %1 表示脚本接收到的第一个参数
if "%~1"=="" (
    :: 如果参数为空，使用当前目录 %cd%
    set "BASE_DIR=%cd%"
    echo [INFO] No parameter provided. Using current directory.
) else (
    :: 如果提供了参数，去除可能存在的双引号，并使用该路径
    set "BASE_DIR=%~1"
    echo [INFO] Destination parameter provided: !BASE_DIR!
)

:: 设置 zmmud 最终的目标完整路径
set "TARGET_DIR=%BASE_DIR%\zmmud"
echo [INFO] Target Directory: %TARGET_DIR%

:: 3. 如果 zmmud 文件夹不存在，则自动创建它
if not exist "%TARGET_DIR%" (
    echo [INFO] Creating directory: %TARGET_DIR%
    mkdir "%TARGET_DIR%"
)

:: 4. 复制 JAR 包
if exist ".\target\zmmud-2.0-SNAPSHOT.jar" (
    copy /Y ".\target\zmmud-2.0-SNAPSHOT.jar" "%TARGET_DIR%\"
) else (
    echo [ERROR] JAR file not found in target directory!
)

:: 5. 复制 conf 目录
if exist ".\target\conf" (
    xcopy ".\target\conf" "%TARGET_DIR%\conf\" /E /I /Y
) else (
    echo [WARNING] 'target/conf' directory not found!
)

echo [SUCCESS] All done!
pause
