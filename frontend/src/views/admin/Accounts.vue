<template>
  <div>
    <div class="bar">
      <div class="section-title" style="flex:1;margin:0;border:none"><h2>账号管理</h2></div>
      <el-button type="primary" :icon="Plus" @click="openNew">新增账号</el-button>
    </div>

    <el-alert type="info" :closable="false" style="margin-bottom:14px"
      title="校宣(ADMIN)可审核发布、管理全部内容;部门投稿(EDITOR)只能写自己的投稿并提交审核。" />

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="username" label="登录账号" min-width="140" />
      <el-table-column prop="displayName" label="显示名 / 部门" min-width="160" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">{{ row.role === 'ADMIN' ? '校宣' : '部门投稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="(v) => toggle(row, v)" inline-prompt active-text="启" inactive-text="停" style="margin-right:8px" />
          <el-button link type="primary" @click="resetPwd(row)">重置密码</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="新增账号" width="440px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="登录账号" required><el-input v-model="form.username" placeholder="3位以上,字母/数字" /></el-form-item>
        <el-form-item label="显示名/部门" required><el-input v-model="form.displayName" placeholder="如:教务处 王老师" /></el-form-item>
        <el-form-item label="初始密码" required><el-input v-model="form.password" type="password" show-password placeholder="至少6位" /></el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role">
            <el-radio value="EDITOR">部门投稿</el-radio>
            <el-radio value="ADMIN">校宣(可审核)</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="create">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminAccounts, adminCreateAccount, adminResetAccountPwd, adminSetAccountEnabled, adminDeleteAccount } from '../../api'

const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const form = reactive({ username: '', displayName: '', password: '', role: 'EDITOR' })

async function load() {
  loading.value = true
  try { list.value = await adminAccounts() } finally { loading.value = false }
}
function openNew() { Object.assign(form, { username: '', displayName: '', password: '', role: 'EDITOR' }); dialog.value = true }
async function create() {
  if (!form.username || !form.displayName || !form.password) { ElMessage.warning('请填写完整'); return }
  saving.value = true
  try {
    await adminCreateAccount({ ...form })
    ElMessage.success('账号已创建'); dialog.value = false; load()
  } finally { saving.value = false }
}
async function toggle(row, v) {
  try { await adminSetAccountEnabled(row.id, v); row.enabled = v; ElMessage.success('已' + (v ? '启用' : '停用')) }
  catch (e) { load() }
}
async function resetPwd(row) {
  const { value } = await ElMessageBox.prompt(`为「${row.displayName || row.username}」设置新密码`, '重置密码', {
    inputType: 'password', inputValidator: (v) => (v && v.length >= 6 ? true : '至少6位'),
  })
  await adminResetAccountPwd(row.id, value); ElMessage.success('密码已重置')
}
async function del(row) {
  await ElMessageBox.confirm(`确定删除账号「${row.username}」?`, '提示', { type: 'warning' })
  await adminDeleteAccount(row.id); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>

<style scoped>
.bar { display: flex; align-items: center; margin-bottom: 16px; }
</style>
