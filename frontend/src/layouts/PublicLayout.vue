<template>
  <div class="public-layout">
    <!-- 顶部 -->
    <header class="site-header">
      <div class="container header-inner">
        <router-link to="/" class="brand">
          <span class="seal serif">石</span>
          <div class="brand-text">
            <div class="brand-cn serif">{{ config.siteName || '成都市石室联中132学校' }}</div>
            <div class="brand-en">{{ config.siteEn || 'Chengdu Shishi Union No.132 School' }}</div>
          </div>
        </router-link>
        <div class="header-search">
          <el-input v-model="kw" placeholder="搜索校园新闻、公告…" size="default" @keyup.enter="doSearch">
            <template #append>
              <el-button :icon="Search" @click="doSearch" />
            </template>
          </el-input>
        </div>
        <div class="header-right">
          <button class="a11y-toggle serif" :class="{ on: fontLarge }" @click="toggleFont"
                  :title="fontLarge ? '恢复标准字号' : '切换大字号,方便长辈阅读'">
            {{ fontLarge ? '标准字号' : '大字版' }}
          </button>
          <template v-if="loggedIn">
            <a class="nav-auth" @click="goAdmin">进入管理</a>
            <a class="nav-auth ghost" @click="doLogout">登出</a>
          </template>
          <router-link v-else class="nav-auth" to="/admin/login">登录</router-link>
          <div class="motto serif">校训 · 爱国利民</div>
        </div>
        <el-button class="menu-toggle" :icon="Menu" text @click="drawer = true" />
      </div>

      <!-- 桌面导航 -->
      <nav class="site-nav">
        <div class="container nav-inner">
          <el-menu mode="horizontal" :ellipsis="false" router :default-active="activePath" class="nav-menu">
            <el-menu-item index="/">首页</el-menu-item>
            <el-sub-menu index="about">
              <template #title>学校概况</template>
              <el-menu-item index="/page/intro">学校简介</el-menu-item>
              <el-menu-item index="/page/principal">校长寄语</el-menu-item>
              <el-menu-item index="/page/campus">校园环境</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="news">
              <template #title>新闻中心</template>
              <el-menu-item index="/list/xwzx">校园新闻</el-menu-item>
              <el-menu-item index="/list/tzgg">通知公告</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/list/jyjx">教育教学</el-menu-item>
            <el-menu-item index="/list/dycd">德育天地</el-menu-item>
            <el-menu-item index="/teachers">师资队伍</el-menu-item>
            <el-menu-item index="/list/xyfc">校园风采</el-menu-item>
            <el-menu-item index="/list/zsks">招生招考</el-menu-item>
            <el-menu-item index="/contact">联系我们</el-menu-item>
          </el-menu>
        </div>
      </nav>
    </header>

    <!-- 移动端抽屉 -->
    <el-drawer v-model="drawer" title="导航菜单" size="72%" direction="ltr">
      <div style="padding:0 20px 12px">
        <el-input v-model="kw" placeholder="站内搜索…" @keyup.enter="doSearchMobile">
          <template #append><el-button :icon="Search" @click="doSearchMobile" /></template>
        </el-input>
      </div>
      <el-menu router :default-active="activePath" @select="drawer = false">
        <el-menu-item index="/">首页</el-menu-item>
        <el-sub-menu index="about">
          <template #title>学校概况</template>
          <el-menu-item index="/page/intro">学校简介</el-menu-item>
          <el-menu-item index="/page/principal">校长寄语</el-menu-item>
          <el-menu-item index="/page/campus">校园环境</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="news">
          <template #title>新闻中心</template>
          <el-menu-item index="/list/xwzx">校园新闻</el-menu-item>
          <el-menu-item index="/list/tzgg">通知公告</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/list/jyjx">教育教学</el-menu-item>
        <el-menu-item index="/list/dycd">德育天地</el-menu-item>
        <el-menu-item index="/teachers">师资队伍</el-menu-item>
        <el-menu-item index="/list/xyfc">校园风采</el-menu-item>
        <el-menu-item index="/list/zsks">招生招考</el-menu-item>
        <el-menu-item index="/contact">联系我们</el-menu-item>
      </el-menu>
      <div class="drawer-auth">
        <template v-if="loggedIn">
          <el-button type="primary" plain @click="goAdmin">进入管理后台</el-button>
          <el-button @click="doLogout">登出</el-button>
        </template>
        <router-link v-else to="/admin/login" @click="drawer = false">
          <el-button type="primary" plain style="width:100%">管理员登录</el-button>
        </router-link>
      </div>
    </el-drawer>

    <!-- 主体 -->
    <main class="site-main">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="container footer-inner">
        <div class="footer-brand">
          <span class="seal serif">石</span>
          <div>
            <div class="serif" style="font-size:18px">{{ config.siteName || '成都市石室联中132学校' }}</div>
            <div style="opacity:.7;font-size:13px">石室文脉 · 航空科创 · 爱国利民</div>
          </div>
        </div>
        <div class="footer-links">
          <router-link to="/page/intro">学校简介</router-link>
          <router-link to="/list/xwzx">校园新闻</router-link>
          <router-link to="/teachers">师资队伍</router-link>
          <router-link to="/contact">联系我们</router-link>
          <router-link to="/admin/login">管理登录</router-link>
        </div>
        <div class="footer-friends">
          <span class="ff-label serif">友情链接:</span>
          <a v-for="f in friendLinks" :key="f.url" :href="f.url" target="_blank" rel="noopener">{{ f.name }}</a>
        </div>
        <div class="footer-copy">
          <div>© {{ year }} {{ config.siteName || '成都市石室联中132学校' }} 版权所有</div>
          <div v-if="config.beian">
            <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener">{{ config.beian }}</a>
          </div>
        </div>
      </div>
    </footer>

    <!-- 回到顶部 -->
    <el-backtop :right="24" :bottom="24" :visibility-height="300" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Menu, Search } from '@element-plus/icons-vue'
import { getConfig, adminLogout } from '../api'

const route = useRoute()
const router = useRouter()
const drawer = ref(false)
const config = ref({})
const kw = ref('')
const year = new Date().getFullYear()
const fontLarge = ref(false)
const loggedIn = ref(false)

function refreshAuth() { loggedIn.value = !!localStorage.getItem('admin_token') }

function goAdmin() { drawer.value = false; router.push('/admin/articles') }

async function doLogout() {
  try { await adminLogout() } catch (e) { /* ignore */ }
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_role')
  localStorage.removeItem('admin_name')
  refreshAuth()
  drawer.value = false
  ElMessage.success('已登出')
}

function applyFont() {
  document.documentElement.classList.toggle('a11y-large', fontLarge.value)
}
function toggleFont() {
  fontLarge.value = !fontLarge.value
  localStorage.setItem('a11y_large', fontLarge.value ? '1' : '0')
  applyFont()
}

function doSearch() {
  const q = kw.value.trim()
  if (q) router.push({ name: 'search', query: { q } })
}
function doSearchMobile() {
  doSearch()
  drawer.value = false
}

// 友情链接(URL 请按实际核对/调整)
const friendLinks = [
  { name: '成都市青羊区人民政府', url: 'https://www.cdqingyang.gov.cn/' },
  { name: '成都市教育局', url: 'https://edu.chengdu.gov.cn/' },
  { name: '成都石室联合中学', url: 'https://www.cdsslz.cn/' },
  { name: '航空工业成飞', url: 'https://www.avic.com/' },
]

const activePath = computed(() => {
  // 列表/详情页高亮对应栏目
  return route.path
})

onMounted(async () => {
  fontLarge.value = localStorage.getItem('a11y_large') === '1'
  applyFont()
  refreshAuth()
  try {
    config.value = await getConfig()
  } catch (e) {}
})
</script>

<style scoped>
.public-layout { display: flex; flex-direction: column; min-height: 100vh; }

.site-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 14px;
  padding-bottom: 14px;
}
.brand { display: flex; align-items: center; gap: 12px; }
.brand-cn { font-size: 24px; color: var(--shishi-red-deep); font-weight: 700; letter-spacing: 2px; }
.brand-en { font-size: 12px; color: #9a8f80; letter-spacing: 1px; }
.header-right { margin-left: auto; display: flex; align-items: center; gap: 14px; }
.a11y-toggle {
  cursor: pointer;
  background: transparent;
  border: 1px solid var(--shishi-gold);
  color: var(--shishi-gold);
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 16px;
  transition: all .2s ease;
}
.a11y-toggle:hover, .a11y-toggle.on {
  background: var(--shishi-gold);
  color: #fff;
}
.nav-auth {
  cursor: pointer;
  font-size: 13px;
  padding: 5px 14px;
  border-radius: 16px;
  background: var(--shishi-red);
  color: #fff;
  white-space: nowrap;
}
.nav-auth:hover { background: var(--shishi-red-deep); color: #fff; }
.nav-auth.ghost { background: transparent; color: var(--shishi-red); border: 1px solid var(--shishi-red); }
.nav-auth.ghost:hover { background: var(--shishi-red); color: #fff; }
.drawer-auth { display: flex; gap: 10px; padding: 16px 20px; border-top: 1px solid #eee5d3; margin-top: 8px; }
.drawer-auth .el-button { flex: 1; }
.motto {
  color: var(--shishi-gold);
  font-size: 16px;
  border-left: 2px solid var(--shishi-gold);
  padding-left: 12px;
}
.menu-toggle { display: none; margin-left: auto; font-size: 24px; }

.site-nav { background: var(--shishi-red); }
.nav-menu {
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #fbeccb;
  --el-menu-hover-bg-color: var(--shishi-red-deep);
  --el-menu-active-color: #fff;
  border-bottom: none !important;
  justify-content: center;
}
.nav-menu.el-menu--horizontal > .el-menu-item,
.nav-menu.el-menu--horizontal > .el-sub-menu :deep(.el-sub-menu__title) {
  color: #fbeccb;
  font-size: 16px;
  border-bottom: 3px solid transparent;
}
.nav-menu.el-menu--horizontal > .el-menu-item.is-active {
  border-bottom-color: var(--shishi-gold) !important;
  color: #fff !important;
  background: var(--shishi-red-deep);
}
.nav-menu.el-menu--horizontal > .el-menu-item:hover,
.nav-menu.el-menu--horizontal > .el-sub-menu:hover :deep(.el-sub-menu__title) {
  background: var(--shishi-red-deep) !important;
  color: #fff !important;
}

.site-main { flex: 1; }

.site-footer { background: var(--shishi-ink); color: #d8cdbc; margin-top: 40px; }
.footer-inner { padding: 28px 16px; display: flex; flex-wrap: wrap; gap: 20px; align-items: center; }
.footer-brand { display: flex; align-items: center; gap: 12px; }
.footer-links { display: flex; gap: 18px; flex-wrap: wrap; margin-left: auto; }
.footer-links a { color: #d8cdbc; font-size: 14px; }
.footer-links a:hover { color: var(--shishi-gold); }
.footer-friends { width: 100%; border-top: 1px solid #40382f; padding-top: 14px; display: flex; flex-wrap: wrap; align-items: center; gap: 16px; }
.footer-friends .ff-label { color: var(--shishi-gold); font-size: 14px; }
.footer-friends a { color: #b7ab97; font-size: 13px; }
.footer-friends a:hover { color: var(--shishi-gold); }
.footer-copy { width: 100%; border-top: 1px solid #40382f; padding-top: 14px; font-size: 13px; opacity: .85; }

.header-search { margin-left: auto; width: 240px; }

@media (max-width: 860px) {
  .motto { display: none; }
  .a11y-toggle { display: none; }
  .header-search { display: none; }
  .menu-toggle { display: inline-flex; }
  .site-nav { display: none; }
  .brand-cn { font-size: 19px; }
  .footer-links { margin-left: 0; }
}
</style>
