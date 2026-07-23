<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="brand">
        <span class="seal serif">石</span>
        <div>
          <div class="serif title">石室联中132 · 官网管理</div>
          <div class="sub">爱国利民 · 内容管理系统</div>
        </div>
      </div>
      <el-form :model="form" @keyup.enter="login">
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="管理员账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="remember">记住密码</el-checkbox>
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="login">登 录</el-button>
      </el-form>
      <div class="tip">仅限授权管理员登录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { adminLogin } from '../../api'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const remember = ref(false)
const form = reactive({ username: '', password: '' })

const SAVE_KEY = 'saved_admin_login'

onMounted(() => {
  try {
    const raw = localStorage.getItem(SAVE_KEY)
    if (raw) {
      const { u, p } = JSON.parse(decodeURIComponent(atob(raw)))
      form.username = u || ''
      form.password = p || ''
      remember.value = true
    }
  } catch (e) { /* ignore */ }
})

async function login() {
  if (!form.username || !form.password) { ElMessage.warning('请输入账号和密码'); return }
  loading.value = true
  try {
    const res = await adminLogin({ ...form })
    localStorage.setItem('admin_token', res.token)
    localStorage.setItem('admin_role', res.role || 'EDITOR')
    localStorage.setItem('admin_name', res.displayName || res.username)
    if (remember.value) {
      localStorage.setItem(SAVE_KEY, btoa(encodeURIComponent(JSON.stringify({ u: form.username, p: form.password }))))
    } else {
      localStorage.removeItem(SAVE_KEY)
    }
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/admin/articles')
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #7a1519, #a4232a);
}
.login-card { background: #fff; border-radius: 14px; padding: 40px 40px 28px; width: 380px; box-shadow: 0 12px 40px rgba(0,0,0,.25); }
.brand { display: flex; align-items: center; gap: 14px; margin-bottom: 26px; }
.brand .title { font-size: 21px; font-weight: 700; color: var(--shishi-red-deep); }
.brand .sub { font-size: 12px; color: #a89e91; margin-top: 2px; }
.tip { text-align: center; font-size: 12px; color: #a89e91; margin-top: 16px; }
</style>
