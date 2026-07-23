<template>
  <div>
    <div class="section-title"><h2>留言管理</h2></div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="称呼" width="120" />
      <el-table-column prop="contact" label="联系方式" width="180" />
      <el-table-column prop="content" label="留言内容" min-width="300" show-overflow-tooltip />
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }"><el-tag :type="row.handled ? 'success' : 'warning'" size="small">{{ row.handled ? '已处理' : '待处理' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="toggle(row)">{{ row.handled ? '标为未处理' : '标为已处理' }}</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminMessages, adminHandleMessage, adminDeleteMessage } from '../../api'

const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try { list.value = await adminMessages() } finally { loading.value = false }
}
function fmt(s) { return s ? String(s).slice(0, 16).replace('T', ' ') : '' }
async function toggle(row) { await adminHandleMessage(row.id, !row.handled); ElMessage.success('已更新'); load() }
async function del(row) {
  await ElMessageBox.confirm('确定删除该留言?', '提示', { type: 'warning' })
  await adminDeleteMessage(row.id); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>
