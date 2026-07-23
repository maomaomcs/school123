<template>
  <el-container class="admin-wrap">
    <el-aside width="220px" class="admin-aside">
      <div class="logo">
        <span class="seal serif">石</span>
        <span class="serif">官网管理</span>
      </div>
      <el-menu :default-active="route.path" router text-color="#d8cdbc" active-text-color="#fff" background-color="transparent">
        <el-menu-item index="/admin/articles"><el-icon><Document /></el-icon><span>{{ isAdmin ? '文章 / 审核' : '我的投稿' }}</span></el-menu-item>
        <template v-if="isAdmin">
          <el-menu-item index="/admin/banners"><el-icon><PictureFilled /></el-icon><span>轮播图</span></el-menu-item>
          <el-menu-item index="/admin/teachers"><el-icon><Avatar /></el-icon><span>师资队伍</span></el-menu-item>
          <el-menu-item index="/admin/pages"><el-icon><Files /></el-icon><span>单页内容</span></el-menu-item>
          <el-menu-item index="/admin/messages"><el-icon><ChatDotRound /></el-icon><span>留言管理</span></el-menu-item>
          <el-menu-item index="/admin/accounts"><el-icon><UserFilled /></el-icon><span>账号管理</span></el-menu-item>
        </template>
        <el-menu-item index="/admin/password"><el-icon><Key /></el-icon><span>修改密码</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="left">
          <a href="/" target="_blank">查看官网 <el-icon><TopRight /></el-icon></a>
        </div>
        <div class="right">
          <span class="user">
            <el-icon><UserFilled /></el-icon> {{ displayName }}
            <el-tag size="small" :type="isAdmin ? 'danger' : 'info'" effect="plain" style="margin-left:6px">{{ isAdmin ? '校宣' : '投稿' }}</el-tag>
          </span>
          <el-button text @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, PictureFilled, Avatar, Files, ChatDotRound, Key, TopRight, UserFilled } from '@element-plus/icons-vue'
import { adminMe, adminLogout } from '../api'

const route = useRoute()
const router = useRouter()
const displayName = ref(localStorage.getItem('admin_name') || '')
const role = ref(localStorage.getItem('admin_role') || 'EDITOR')
const isAdmin = computed(() => role.value === 'ADMIN')

onMounted(async () => {
  try {
    const me = await adminMe()
    displayName.value = me.displayName || me.username
    role.value = me.role || 'EDITOR'
    localStorage.setItem('admin_role', role.value)
    localStorage.setItem('admin_name', displayName.value)
  } catch (e) {}
})

async function logout() {
  try { await adminLogout() } catch (e) {}
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_role')
  localStorage.removeItem('admin_name')
  router.replace('/admin/login')
}
</script>

<style scoped>
.admin-wrap { height: 100vh; }
.admin-aside { display: flex; flex-direction: column; }
.logo { display: flex; align-items: center; gap: 10px; color: #f6f1e7; font-size: 18px; padding: 18px 20px; border-bottom: 1px solid #40382f; }
.logo .seal { width: 36px; height: 36px; font-size: 22px; }
.admin-header { background: #fff; box-shadow: 0 1px 6px rgba(0,0,0,.06); display: flex; align-items: center; justify-content: space-between; }
.admin-header .user { margin-right: 14px; color: #666; display: inline-flex; align-items: center; gap: 4px; }
.admin-main { background: #f2ede3; padding: 20px; }
</style>
