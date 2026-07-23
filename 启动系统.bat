@echo off
chcp 65001 >nul
cd /d "%~dp0"
title 石室联中官网 - 启动器

set "TOOLING=d:\security agent\ticket-system-pro\tooling"

echo ============================================
echo   成都石室联合中学 官网 启动中...
echo ============================================
echo.

echo [1/2] 启动 MySQL 数据库(与工单系统共用便携库,若已启动会自动复用)...
start "MySQL数据库-请勿关闭" /min "%TOOLING%\mysql-8.0.29-winx64\bin\mysqld.exe" --defaults-file="%TOOLING%\my.ini" --console
echo     等待数据库就绪...
timeout /t 6 /nobreak >nul

echo [2/2] 启动官网后端(端口 8083,新窗口,请勿关闭)...
set "JAVA_HOME=%TOOLING%\jdk-17.0.19+10"
start "石室联中官网后端-请勿关闭" "%TOOLING%\jdk-17.0.19+10\bin\java.exe" -jar "%~dp0backend\target\site-0.0.1-SNAPSHOT.jar"
echo     等待后端就绪...
timeout /t 10 /nobreak >nul

echo.
echo ============================================
echo   官网已启动!请在浏览器打开:
echo.
echo   官网首页      : http://localhost:8083/
echo   后台管理入口  : http://localhost:8083/admin/login
echo   默认管理账号  : admin / Shishi@2026
echo ============================================
echo.
start http://localhost:8083/
pause
