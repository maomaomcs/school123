<template>
  <div>
    <div class="bar">
      <el-select v-model="category" placeholder="全部栏目" clearable style="width:150px" @change="reload">
        <el-option v-for="c in cats" :key="c.key" :label="c.label" :value="c.key" />
      </el-select>
      <el-select v-model="status" placeholder="全部状态" clearable style="width:140px" @change="reload">
        <el-option label="待审核" value="pending" />
        <el-option label="已发布" value="published" />
        <el-option label="草稿" value="draft" />
        <el-option label="已驳回" value="rejected" />
      </el-select>
      <el-button type="primary" :icon="Plus" @click="$router.push('/admin/articles/new')">
        {{ isAdmin ? '发布文章' : '写投稿' }}
      </el-button>
    </div>

    <el-alert v-if="isAdmin && pendingCount > 0" type="warning" :closable="false" show-icon style="margin-bottom:14px"
      :title="`有 ${pendingCount} 篇投稿待审核`" />

    <el-table :data="data.list" v-loading="loading" border stripe>
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column label="栏目" width="110">
        <template #default="{ row }">{{ label(row.category) }}</template>
      </el-table-column>
      <el-table-column v-if="isAdmin" prop="authorName" label="投稿人" width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="stType(row.status)" size="small">{{ stLabel(row.status) }}</el-tag>
          <el-tooltip v-if="row.status === 'rejected' && row.rejectReason" :content="'驳回理由:' + row.rejectReason">
            <el-icon style="margin-left:4px;color:#e6a23c;vertical-align:middle"><Warning /></el-icon>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="64">
        <template #default="{ row }"><el-tag v-if="row.top" type="danger" size="small">顶</el-tag></template>
      </el-table-column>
      <el-table-column prop="views" label="浏览" width="72" />
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ fmt(row.publishedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <template v-if="isAdmin && row.status === 'pending'">
            <el-button link type="success" @click="approve(row)">通过</el-button>
            <el-button link type="warning" @click="reject(row)">驳回</el-button>
          </template>
          <el-button link type="primary" @click="$router.push(`/admin/articles/${row.id}`)">编辑</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="prev, pager, next, total" :total="data.total"
        :page-size="10" :current-page="page" @current-change="onPage" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Warning } from '@element-plus/icons-vue'
import { adminArticles, adminDeleteArticle, approveArticle, rejectArticle, getConfig } from '../../api'

const data = ref({ list: [], total: 0 })
const loading = ref(false)
const page = ref(1)
const category = ref('')
const status = ref('')
const cats = ref([])
const pendingCount = ref(0)
const isAdmin = (localStorage.getItem('admin_role') || 'EDITOR') === 'ADMIN'

function label(k) { return cats.value.find(c => c.key === k)?.label || k }
function fmt(s) { return s ? String(s).slice(0, 16).replace('T', ' ') : '' }
function stLabel(s) { return { draft: '草稿', pending: '待审核', published: '已发布', rejected: '已驳回' }[s] || s }
function stType(s) { return { draft: 'info', pending: 'warning', published: 'success', rejected: 'danger' }[s] || 'info' }

async function load() {
  loading.value = true
  try {
    data.value = await adminArticles({ category: category.value, status: status.value, page: page.value, size: 10 })
    pendingCount.value = data.value.pendingCount || 0
  } finally { loading.value = false }
}
function reload() { page.value = 1; load() }
function onPage(p) { page.value = p; load() }

async function approve(row) {
  try { await ElMessageBox.confirm(`通过并发布「${row.title}」?`, '审核', { type: 'success' }) } catch (e) { return }
  await approveArticle(row.id); ElMessage.success('已发布'); load()
}
async function reject(row) {
  let value
  try {
    ({ value } = await ElMessageBox.prompt('请填写驳回理由(投稿人可看到)', '驳回投稿', {
      inputType: 'textarea', inputPlaceholder: '如:标题需修改、内容需补充配图…',
      inputValidator: (v) => (v && v.trim() ? true : '请填写理由'),
    }))
  } catch (e) { return }
  await rejectArticle(row.id, value.trim()); ElMessage.success('已驳回'); load()
}
async function del(row) {
  try { await ElMessageBox.confirm(`确定删除「${row.title}」?`, '提示', { type: 'warning' }) } catch (e) { return }
  await adminDeleteArticle(row.id); ElMessage.success('已删除'); load()
}

onMounted(async () => {
  try { cats.value = (await getConfig()).categories } catch (e) {}
  load()
})
</script>

<style scoped>
.bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.bar .el-button { margin-left: auto; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
@media (max-width: 768px) {
  .bar .el-button { margin-left: 0; }
  .pager { justify-content: center; }
}
</style>
