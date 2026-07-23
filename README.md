# 成都石室联合中学 官网 (school-website)

石室书院风格的中学官网,前台展示 + 后台内容管理。技术栈与工单系统一致:
**Spring Boot 3.4 + Vue 3 + Element Plus + MySQL**,前端打包进后端,生产为单个 jar(端口 **8083**)。

## 目录结构
- `backend/`  Spring Boot 后端(package `com.school.site`),前端产物打进 `src/main/resources/static`
- `frontend/` Vue 3 + Vite 前端(`node_modules` 复用自工单系统,相同依赖)
- `重新构建.bat` / `启动系统.bat` / `开发模式.bat` 一键脚本

> 便携 JDK17 / MySQL8 复用工单系统的 `d:\security agent\ticket-system-pro\tooling`,未重复占用磁盘。
> MySQL 与工单系统共用同一实例(端口 3306),本项目使用独立数据库 `shishi_site`,首次启动自动创建。

## 本地运行
1. 双击 `启动系统.bat`(会启动 MySQL + 后端并打开浏览器)。
2. 官网首页 http://localhost:8083/ ,后台 http://localhost:8083/admin/login 。
3. 默认管理员 **admin / Shishi@2026**,登录后请到「修改密码」更换。

改代码后:
- 只改后端:`重新构建.bat`(其实前端也会重编,快)。
- 前端热更新调试:`开发模式.bat`(前端 5174 端口,接口自动代理到 8083)。

## 功能
- 前台:首页(轮播/快捷入口/新闻/通知/教学/德育/校园风采/名师预览)、栏目列表页、文章详情、学校概况单页、师资队伍、联系我们(在线留言)。
- 后台:文章管理(6 栏目 + 草稿/发布/置顶 + 封面与正文配图上传)、轮播图、师资队伍、单页内容(学校简介/校长寄语/校园环境/联系我们)、留言管理、修改密码。
- 图片上传自动压缩(最长边 1600px),存于 `backend/data/uploads`(生产用 `UPLOAD_DIR` 指向绝对路径)。

## 栏目 key
`xwzx` 校园新闻 · `tzgg` 通知公告 · `jyjx` 教育教学 · `dycd` 德育天地 · `xyfc` 校园风采 · `zsks` 招生招考

## 示例内容说明
空库首次启动会自动填充石室书院风的**示例**内容(文翁石室历史、校训「爱国利民」、示例新闻/师资/轮播)。
公众号图文/真实照片受登录与反爬限制,无法自动爬取,请在后台逐条替换为学校正式内容与图片。

## 环境变量(生产可覆盖)
- `PORT`(默认 8083)、`DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD`
- `UPLOAD_DIR`、`ADMIN_INIT_USERNAME`/`ADMIN_INIT_PASSWORD`
- `SITE_NAME`/`SITE_EN`/`SITE_BEIAN`(站点名称/英文名/备案号,页脚展示)

## 部署(参考工单系统)
本地构建出 `backend/target/site-0.0.1-SNAPSHOT.jar`,上传到服务器用 JDK17 运行即可(与工单系统同机可复用 `/opt/jdk17`,数据库另建 `shishi_site`,端口用 8083,注意安全组/宝塔/防火墙放行)。

## 备注
- 管理员登录令牌存内存,后端重启需重新登录(内容管理场景够用)。
- 若要对外公网,建议为域名配 HTTPS 反向代理到 `127.0.0.1:8083`。
