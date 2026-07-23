@echo off
chcp 65001 >nul
cd /d "%~dp0"
title 石室联中官网 - 开发模式

set "TOOLING=d:\security agent\ticket-system-pro\tooling"

echo 开发模式:前端热更新(改代码即时生效),用于二次开发。
echo.

echo [1/3] 启动 MySQL 数据库...
start "MySQL数据库-请勿关闭" /min "%TOOLING%\mysql-8.0.29-winx64\bin\mysqld.exe" --defaults-file="%TOOLING%\my.ini" --console
timeout /t 6 /nobreak >nul

echo [2/3] 启动后端(端口 8083)...
set "JAVA_HOME=%TOOLING%\jdk-17.0.19+10"
start "官网后端-请勿关闭" "%TOOLING%\jdk-17.0.19+10\bin\java.exe" -jar "%~dp0backend\target\site-0.0.1-SNAPSHOT.jar"
timeout /t 8 /nobreak >nul

echo [3/3] 启动前端开发服务器(端口 5174)...
cd /d "%~dp0frontend"
start "前端开发服务器-请勿关闭" cmd /k node node_modules\vite\bin\vite.js --host
timeout /t 4 /nobreak >nul

echo.
echo 开发访问地址: http://localhost:5174/  (改前端代码自动刷新,接口自动代理到 8083)
start http://localhost:5174/
pause
