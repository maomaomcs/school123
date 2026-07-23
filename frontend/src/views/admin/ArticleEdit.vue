<template>
  <div class="edit-page" v-loading="loading">
    <div class="section-title"><h2>{{ isNew ? (isAdmin ? '发布文章' : '写投稿') : '编辑文章' }}</h2></div>
    <el-alert v-if="form.status === 'rejected' && form.rejectReason" type="error" :closable="false" show-icon
      style="margin-bottom:16px;max-width:900px" :title="'上次被驳回:' + form.rejectReason" />
    <el-form :model="form" label-width="90px" style="max-width:900px">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" maxlength="200" placeholder="文章标题" />
      </el-form-item>
      <el-form-item label="栏目" required>
        <el-select v-model="form.category" placeholder="选择栏目">
          <el-option v-for="c in cats" :key="c.key" :label="c.label" :value="c.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="列表页显示的简介" />
      </el-form-item>
      <el-form-item label="封面图">
        <div class="cover-box">
          <img v-if="form.cover" :src="form.cover" class="cover-preview" />
          <el-upload :action="uploadUrl" :headers="headers" :show-file-list="false" accept="image/*" :on-success="onCover">
            <el-button>{{ form.cover ? '更换封面' : '上传封面' }}</el-button>
          </el-upload>
          <el-button v-if="form.cover" link type="danger" @click="form.cover = ''">移除</el-button>
        </div>
      </el-form-item>
      <el-form-item label="正文">
        <div class="editor-wrap">
          <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" mode="default" class="editor-toolbar" />
          <Editor v-model="form.content" :defaultConfig="editorConfig" mode="default" class="editor-body" @onCreated="handleCreated" />
        </div>
      </el-form-item>
      <el-form-item label="作者/来源">
        <el-input v-model="form.author" placeholder="作者" style="width:200px;margin-right:10px" />
        <el-input v-model="form.source" placeholder="来源" style="width:200px" />
      </el-form-item>
      <el-form-item v-if="isAdmin" label="选项">
        <el-switch v-model="form.top" active-text="置顶" />
      </el-form-item>
      <el-form-item>
        <template v-if="isAdmin">
          <el-button type="primary" :loading="saving" @click="save('publish')">直接发布</el-button>
          <el-button :loading="saving" @click="save('draft')">存草稿</el-button>
        </template>
        <template v-else>
          <el-button type="primary" :loading="saving" @click="save('submit')">提交审核</el-button>
          <el-button :loading="saving" @click="save('draft')">存草稿</el-button>
        </template>
        <el-button @click="$router.push('/admin/articles')">返回</el-button>
      </el-form-item>
      <div v-if="!isAdmin" class="editor-tip">提交审核后由校宣审核发布;审核期间可继续在列表里编辑。</div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, shallowRef, onBeforeUnmount } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { adminGetArticle, adminCreateArticle, adminUpdateArticle, getConfig, uploadUrl } from '../../api'

const route = useRoute()
const router = useRouter()
const isNew = computed(() => !route.params.id)
const loading = ref(false)
const saving = ref(false)
const cats = ref([])
const headers = { 'X-Admin-Token': localStorage.getItem('admin_token') }

// ---- 富文本编辑器(wangEditor)----
const editorRef = shallowRef()
const toolbarConfig = {}
const editorConfig = {
  placeholder: '在此撰写正文,可插入图片、设置标题/加粗/对齐/列表等…',
  MENU_CONF: {
    uploadImage: {
      // 复用后台上传接口 /api/admin/upload,返回 {url}
      async customUpload(file, insertFn) {
        const fd = new FormData()
        fd.append('file', file)
        try {
          const res = await fetch(uploadUrl, {
            method: 'POST',
            headers: { 'X-Admin-Token': localStorage.getItem('admin_token') },
            body: fd,
          })
          const data = await res.json()
          if (data && data.url) insertFn(data.url, file.name, data.url)
          else ElMessage.error('图片上传失败')
        } catch (e) {
          ElMessage.error('图片上传失败:' + e.message)
        }
      },
    },
  },
}
function handleCreated(editor) { editorRef.value = editor }
onBeforeUnmount(() => { const e = editorRef.value; if (e) e.destroy() })

const isAdmin = (localStorage.getItem('admin_role') || 'EDITOR') === 'ADMIN'

const form = reactive({
  title: '', category: '', summary: '', cover: '', content: '',
  author: '校办', source: '石室联中132', top: false, status: '', rejectReason: '',
})

// 未保存修改保护
let snapshot = ''
function snapKey() {
  const { title, category, summary, cover, content, author, source, top } = form
  return JSON.stringify({ title, category, summary, cover, content, author, source, top })
}
function markClean() { snapshot = snapKey() }
const dirty = computed(() => snapshot !== '' && snapKey() !== snapshot)

function onCover(res) { form.cover = res.url; ElMessage.success('封面已上传') }

// 保存但不跳转(供离开守卫复用)
async function persist(action) {
  if (isNew.value) await adminCreateArticle({ ...form }, action)
  else await adminUpdateArticle(route.params.id, { ...form }, action)
  markClean()
}

async function save(action) {
  if (!form.title || !form.category) { ElMessage.warning('请填写标题和栏目'); return }
  saving.value = true
  try {
    await persist(action)
    ElMessage.success(action === 'submit' ? '已提交审核' : (action === 'publish' ? '已发布' : '已保存草稿'))
    router.push('/admin/articles')
  } finally { saving.value = false }
}

onMounted(async () => {
  try { cats.value = (await getConfig()).categories } catch (e) {}
  if (!isNew.value) {
    loading.value = true
    try {
      const a = await adminGetArticle(route.params.id)
      Object.assign(form, a)
    } finally { loading.value = false }
  }
  // 等编辑器把初始 content 同步后再记录基线
  setTimeout(markClean, 300)
})

onBeforeRouteLeave(async () => {
  if (!dirty.value) return true
  // 缺标题/栏目无法存草稿,退化为放弃/继续
  if (!form.title || !form.category) {
    try {
      await ElMessageBox.confirm('有未保存的修改(缺标题或栏目,无法存草稿)。确定放弃并离开?', '提示', {
        type: 'warning', confirmButtonText: '放弃并离开', cancelButtonText: '继续编辑',
      })
      return true
    } catch (e) { return false }
  }
  // 三选一:存草稿并离开 / 放弃修改并离开 / 继续编辑(关闭)
  try {
    await ElMessageBox({
      title: '离开提示',
      message: '有未保存的修改,离开前是否存为草稿?',
      showCancelButton: true,
      distinguishCancelAndClose: true,
      confirmButtonText: '存草稿并离开',
      cancelButtonText: '放弃修改并离开',
      type: 'warning',
    })
    // 确定 → 存草稿
    saving.value = true
    try { await persist('draft'); ElMessage.success('已存草稿') } finally { saving.value = false }
    return true
  } catch (action) {
    if (action === 'cancel') return true // 放弃并离开
    return false // 关闭/ESC → 继续编辑
  }
})
</script>

<style scoped>
.edit-page { background: #fff; border-radius: 10px; padding: 24px; }
.cover-box { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.cover-preview { width: 160px; height: 100px; object-fit: cover; border-radius: 6px; border: 1px solid #eee; }
.editor-wrap { width: 100%; border: 1px solid #dcdfe6; border-radius: 6px; }
.editor-toolbar { border-bottom: 1px solid #e4e7ed; background: #fafafa; }
.editor-body { min-height: 380px; overflow-y: auto; }
.editor-tip { font-size: 12px; color: #a89e91; margin: 8px 0 0 90px; }
@media (max-width: 768px) {
  .edit-page { padding: 14px; }
  .edit-page :deep(.el-form) { max-width: 100% !important; }
  .edit-page :deep(.el-form-item__content) .el-input,
  .edit-page :deep(.el-form-item__content) .el-select { width: 100% !important; margin-right: 0 !important; }
  .editor-tip { margin-left: 0; }
}
</style>
