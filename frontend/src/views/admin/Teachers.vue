<template>
  <div>
    <div class="bar">
      <div class="section-title" style="flex:1;margin:0;border:none"><h2>师资队伍管理</h2></div>
      <el-button type="primary" :icon="Plus" @click="openNew">新增教师</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column label="照片" width="90">
        <template #default="{ row }"><img :src="row.photo || '/img/avatar.svg'" class="avatar" /></template>
      </el-table-column>
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="title" label="职称/荣誉" min-width="160" />
      <el-table-column prop="subject" label="学科" width="100" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑教师' : '新增教师'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="照片">
          <div class="cover-box">
            <img :src="form.photo || '/img/avatar.svg'" class="avatar-lg" />
            <el-upload :action="uploadUrl" :headers="headers" :show-file-list="false" accept="image/*" :on-success="onImg">
              <el-button>上传照片</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="职称/荣誉"><el-input v-model="form.title" placeholder="如 特级教师、高级教师" /></el-form-item>
        <el-form-item label="任教学科"><el-input v-model="form.subject" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.intro" type="textarea" :rows="3" maxlength="2000" show-word-limit /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminTeachers, adminCreateTeacher, adminUpdateTeacher, adminDeleteTeacher, uploadUrl } from '../../api'

const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const headers = { 'X-Admin-Token': localStorage.getItem('admin_token') }
const form = reactive({ id: null, name: '', title: '', subject: '', photo: '', intro: '', sort: 0 })

async function load() {
  loading.value = true
  try { list.value = await adminTeachers() } finally { loading.value = false }
}
function reset() { Object.assign(form, { id: null, name: '', title: '', subject: '', photo: '', intro: '', sort: 0 }) }
function openNew() { reset(); dialog.value = true }
function openEdit(row) { Object.assign(form, row); dialog.value = true }
function onImg(res) { form.photo = res.url }

async function save() {
  if (!form.name) { ElMessage.warning('请填写姓名'); return }
  saving.value = true
  try {
    if (form.id) await adminUpdateTeacher(form.id, { ...form })
    else await adminCreateTeacher({ ...form })
    ElMessage.success('保存成功'); dialog.value = false; load()
  } finally { saving.value = false }
}
async function del(row) {
  await ElMessageBox.confirm(`确定删除教师「${row.name}」?`, '提示', { type: 'warning' })
  await adminDeleteTeacher(row.id); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>

<style scoped>
.bar { display: flex; align-items: center; margin-bottom: 16px; }
.avatar { width: 50px; height: 50px; border-radius: 50%; object-fit: cover; }
.cover-box { display: flex; align-items: center; gap: 14px; }
.avatar-lg { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; border: 2px solid var(--shishi-gold); }
</style>
