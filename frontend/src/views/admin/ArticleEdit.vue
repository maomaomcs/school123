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
        <div style="width:100%">
          <div class="content-toolbar">
            <el-upload :action="uploadUrl" :headers="headers" :show-file-list="false" accept="image/*" :on-success="onInsertImg">
              <el-button size="small" :icon="Picture">插入图片</el-button>
            </el-upload>
            <span class="hint">支持 HTML 标签。段落用 &lt;p&gt;...&lt;/p&gt;,插入图片会追加到正文末尾。</span>
          </div>
          <el-input v-model="form.content" type="textarea" :rows="14" placeholder="<p>正文内容...</p>" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { adminGetArticle, adminCreateArticle, adminUpdateArticle, getConfig, uploadUrl } from '../../api'

const route = useRoute()
const router = useRouter()
const isNew = computed(() => !route.params.id)
const loading = ref(false)
const saving = ref(false)
const cats = ref([])
const headers = { 'X-Admin-Token': localStorage.getItem('admin_token') }

const isAdmin = (localStorage.getItem('admin_role') || 'EDITOR') === 'ADMIN'

const form = reactive({
  title: '', category: '', summary: '', cover: '', content: '',
  author: '校办', source: '石室联中132', top: false, status: '', rejectReason: '',
})

function onCover(res) { form.cover = res.url; ElMessage.success('封面已上传') }
function onInsertImg(res) {
  form.content += `\n<p style="text-align:center"><img src="${res.url}" /></p>`
  ElMessage.success('图片已插入正文末尾')
}

async function save(action) {
  if (!form.title || !form.category) { ElMessage.warning('请填写标题和栏目'); return }
  saving.value = true
  try {
    if (isNew.value) await adminCreateArticle({ ...form }, action)
    else await adminUpdateArticle(route.params.id, { ...form }, action)
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
})
</script>

<style scoped>
.edit-page { background: #fff; border-radius: 10px; padding: 24px; }
.cover-box { display: flex; align-items: center; gap: 14px; }
.cover-preview { width: 160px; height: 100px; object-fit: cover; border-radius: 6px; border: 1px solid #eee; }
.content-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.hint { font-size: 12px; color: #a89e91; }
.editor-tip { font-size: 12px; color: #a89e91; margin: 8px 0 0 90px; }
</style>
