@echo off
chcp 65001 >nul
cd /d "%~dp0"
title 石室联中官网 - 一键部署到服务器

set "TOOLING=d:\security agent\ticket-system-pro\tooling"

echo ============================================
echo   重新构建并部署到 43.136.56.131:8083
echo ============================================
echo.

echo [1/3] 构建前端...
cd /d "%~dp0frontend"
call node node_modules\vite\bin\vite.js build
if errorlevel 1 ( echo 前端构建失败! & pause & exit /b 1 )

echo.
echo [2/3] 构建后端 jar...
set "JAVA_HOME=%TOOLING%\jdk-17.0.19+10"
cd /d "%~dp0backend"
call mvnw.cmd -s settings.xml -DskipTests clean package
if errorlevel 1 ( echo 后端构建失败! & pause & exit /b 1 )

echo.
echo [3/3] 上传并重启远程服务...
cd /d "%~dp0deploy"
call node deploy.js
if errorlevel 1 ( echo 部署失败! & pause & exit /b 1 )

echo.
echo ============================================
echo   完成!公网访问需在腾讯云安全组放行 8083。
echo   http://43.136.56.131:8083/
echo ============================================
pause
