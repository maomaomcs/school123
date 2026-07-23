<template>
  <div class="pwd">
    <div class="section-title"><h2>修改密码</h2></div>
    <el-form :model="form" label-width="90px" style="max-width:420px">
      <el-form-item label="原密码">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password placeholder="至少6位" />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input v-model="form.confirm" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="submit">确认修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword } from '../../api'

const router = useRouter()
const saving = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirm: '' })

async function submit() {
  if (!form.oldPassword || !form.newPassword) { ElMessage.warning('请填写完整'); return }
  if (form.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
  if (form.newPassword !== form.confirm) { ElMessage.warning('两次输入的新密码不一致'); return }
  saving.value = true
  try {
    const res = await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success(res.message || '修改成功,请重新登录')
    localStorage.removeItem('admin_token')
    router.replace('/admin/login')
  } finally { saving.value = false }
}
</script>

<style scoped>
.pwd { background: #fff; border-radius: 10px; padding: 24px; }
</style>
