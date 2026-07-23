<template>
  <div>
    <div class="bar">
      <div class="section-title" style="flex:1;margin:0;border:none"><h2>轮播图管理</h2></div>
      <el-button type="primary" :icon="Plus" @click="openNew">新增轮播</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column label="预览" width="180">
        <template #default="{ row }"><img :src="row.imageUrl" class="thumb" /></template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="subtitle" label="副标题" min-width="160" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '显示' : '隐藏' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑轮播' : '新增轮播'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="图片" required>
          <div class="cover-box">
            <img v-if="form.imageUrl" :src="form.imageUrl" class="cover-preview" />
            <el-upload :action="uploadUrl" :headers="headers" :show-file-list="false" accept="image/*" :on-success="onImg">
              <el-button>上传图片</el-button>
            </el-upload>
          </div>
          <div class="hint">建议尺寸 1600×600</div>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="form.link" placeholder="点击跳转,可留空" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="显示"><el-switch v-model="form.enabled" /></el-form-item>
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
import { adminBanners, adminCreateBanner, adminUpdateBanner, adminDeleteBanner, uploadUrl } from '../../api'

const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const headers = { 'X-Admin-Token': localStorage.getItem('admin_token') }
const form = reactive({ id: null, title: '', subtitle: '', imageUrl: '', link: '', sort: 0, enabled: true })

async function load() {
  loading.value = true
  try { list.value = await adminBanners() } finally { loading.value = false }
}
function reset() { Object.assign(form, { id: null, title: '', subtitle: '', imageUrl: '', link: '', sort: 0, enabled: true }) }
function openNew() { reset(); dialog.value = true }
function openEdit(row) { Object.assign(form, row); dialog.value = true }
function onImg(res) { form.imageUrl = res.url }

async function save() {
  if (!form.imageUrl) { ElMessage.warning('请上传图片'); return }
  saving.value = true
  try {
    if (form.id) await adminUpdateBanner(form.id, { ...form })
    else await adminCreateBanner({ ...form })
    ElMessage.success('保存成功'); dialog.value = false; load()
  } finally { saving.value = false }
}
async function del(row) {
  try { await ElMessageBox.confirm('确定删除该轮播图?', '提示', { type: 'warning' }) } catch (e) { return }
  await adminDeleteBanner(row.id); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>

<style scoped>
.bar { display: flex; align-items: center; margin-bottom: 16px; }
.thumb { width: 150px; height: 56px; object-fit: cover; border-radius: 4px; }
.cover-box { display: flex; align-items: center; gap: 14px; }
.cover-preview { width: 160px; height: 60px; object-fit: cover; border-radius: 6px; border: 1px solid #eee; }
.hint { font-size: 12px; color: #a89e91; margin-top: 6px; }
</style>
