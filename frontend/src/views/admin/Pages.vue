<template>
  <div class="pages" v-loading="loading">
    <div class="section-title"><h2>单页内容管理</h2></div>
    <el-tabs v-model="active" tab-position="left" class="tabs">
      <el-tab-pane v-for="p in known" :key="p.key" :label="p.label" :name="p.key">
        <el-form label-width="70px">
          <el-form-item label="标题">
            <el-input v-model="editing.title" />
          </el-form-item>
          <el-form-item label="内容">
            <div style="width:100%">
              <div class="toolbar">
                <el-upload :action="uploadUrl" :headers="headers" :show-file-list="false" accept="image/*" :on-success="onImg">
                  <el-button size="small" :icon="Picture">插入图片</el-button>
                </el-upload>
                <span class="hint">支持 HTML,段落用 &lt;p&gt;...&lt;/p&gt;</span>
              </div>
              <el-input v-model="editing.content" type="textarea" :rows="16" />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="save">保存</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { adminPages, adminSavePage, uploadUrl } from '../../api'

const known = [
  { key: 'intro', label: '学校简介' },
  { key: 'principal', label: '校长寄语' },
  { key: 'campus', label: '校园环境' },
  { key: 'contact', label: '联系我们' },
]
const active = ref('intro')
const loading = ref(false)
const saving = ref(false)
const all = ref({})
const headers = { 'X-Admin-Token': localStorage.getItem('admin_token') }
const editing = reactive({ title: '', content: '' })

function sync() {
  const p = all.value[active.value] || {}
  editing.title = p.title || known.find(k => k.key === active.value)?.label || ''
  editing.content = p.content || ''
}
function onImg(res) { editing.content += `\n<p style="text-align:center"><img src="${res.url}" /></p>` }

async function load() {
  loading.value = true
  try {
    const arr = await adminPages()
    const map = {}
    arr.forEach(p => { map[p.pageKey] = p })
    all.value = map
    sync()
  } finally { loading.value = false }
}
async function save() {
  saving.value = true
  try {
    await adminSavePage(active.value, { title: editing.title, content: editing.content })
    ElMessage.success('保存成功')
    all.value[active.value] = { pageKey: active.value, title: editing.title, content: editing.content }
  } finally { saving.value = false }
}
watch(active, sync)
onMounted(load)
</script>

<style scoped>
.pages { background: #fff; border-radius: 10px; padding: 24px; }
.tabs { min-height: 480px; }
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.hint { font-size: 12px; color: #a89e91; }
</style>
