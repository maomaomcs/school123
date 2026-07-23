<template>
  <div class="pages" v-loading="loading">
    <div class="section-title"><h2>单页内容管理</h2></div>
    <el-tabs v-model="active" tab-position="left" class="tabs" :before-leave="beforeLeaveTab">
      <el-tab-pane v-for="p in known" :key="p.key" :label="p.label" :name="p.key">
        <el-form label-width="70px">
          <el-form-item label="标题">
            <el-input v-model="editing.title" />
          </el-form-item>
          <el-form-item label="内容">
            <div class="editor-wrap">
              <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" mode="default" class="editor-toolbar" />
              <Editor v-model="editing.content" :defaultConfig="editorConfig" mode="default" class="editor-body" @onCreated="handleCreated" />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="save">保存</el-button>
            <span class="hint" v-if="dirty">· 有未保存的修改</span>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onBeforeUnmount, shallowRef, computed } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
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
const editing = reactive({ title: '', content: '' })
let snapshot = ''

// ---- 富文本编辑器 ----
const editorRef = shallowRef()
const toolbarConfig = {}
const editorConfig = {
  placeholder: '在此编辑页面内容,可插入图片、设置标题/加粗/对齐/列表等…',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        const fd = new FormData()
        fd.append('file', file)
        try {
          const res = await fetch(uploadUrl, { method: 'POST', headers: { 'X-Admin-Token': localStorage.getItem('admin_token') }, body: fd })
          const data = await res.json()
          if (data && data.url) insertFn(data.url, file.name, data.url)
          else ElMessage.error('图片上传失败')
        } catch (e) { ElMessage.error('图片上传失败:' + e.message) }
      },
    },
  },
}
function handleCreated(editor) { editorRef.value = editor }
onBeforeUnmount(() => { const e = editorRef.value; if (e) e.destroy() })

const dirty = computed(() => JSON.stringify({ t: editing.title, c: editing.content }) !== snapshot)
function markClean() { snapshot = JSON.stringify({ t: editing.title, c: editing.content }) }

function sync() {
  const p = all.value[active.value] || {}
  editing.title = p.title || known.find(k => k.key === active.value)?.label || ''
  editing.content = p.content || ''
  markClean()
}

async function beforeLeaveTab() {
  if (!dirty.value) return true
  try {
    await ElMessageBox({
      title: '切换提示', message: '当前页面有未保存的修改,切换前是否保存?',
      showCancelButton: true, distinguishCancelAndClose: true,
      confirmButtonText: '保存并切换', cancelButtonText: '放弃修改并切换', type: 'warning',
    })
    await save() // 确定 → 先保存
    return true
  } catch (action) {
    if (action === 'cancel') return true // 放弃并切换
    return false // 关闭/ESC → 继续编辑
  }
}

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
    markClean()
  } finally { saving.value = false }
}
watch(active, sync)

onBeforeRouteLeave(async () => {
  if (!dirty.value) return true
  try {
    await ElMessageBox({
      title: '离开提示', message: '有未保存的修改,离开前是否保存?',
      showCancelButton: true, distinguishCancelAndClose: true,
      confirmButtonText: '保存并离开', cancelButtonText: '放弃修改并离开', type: 'warning',
    })
    await save() // 确定 → 保存
    return true
  } catch (action) {
    if (action === 'cancel') return true // 放弃并离开
    return false // 关闭/ESC → 继续编辑
  }
})

onMounted(load)
</script>

<style scoped>
.pages { background: #fff; border-radius: 10px; padding: 24px; }
.tabs { min-height: 480px; }
.editor-wrap { width: 100%; border: 1px solid #dcdfe6; border-radius: 6px; }
.editor-toolbar { border-bottom: 1px solid #e4e7ed; background: #fafafa; }
.editor-body { min-height: 360px; overflow-y: auto; }
.hint { font-size: 12px; color: var(--shishi-gold); margin-left: 10px; }
</style>
