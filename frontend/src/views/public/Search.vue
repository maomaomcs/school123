<template>
  <div class="container list-page">
    <div class="crumb">
      <router-link to="/">首页</router-link> &nbsp;/&nbsp; <span>搜索结果</span>
    </div>
    <div class="section-title">
      <h2>搜索:{{ keyword }}</h2>
    </div>

    <div class="search-bar">
      <el-input v-model="kw" placeholder="输入关键词搜索文章标题或正文" size="large" clearable
        @keyup.enter="doSearch">
        <template #append>
          <el-button :icon="SearchIcon" @click="doSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div v-loading="loading">
      <p v-if="keyword" class="result-tip">共找到 <b>{{ data.total }}</b> 条与「{{ keyword }}」相关的内容</p>
      <ul class="art-list" v-if="data.list && data.list.length">
        <li v-for="a in data.list" :key="a.id" class="art-item card-hover">
          <router-link :to="`/article/${a.id}`" class="art-cover">
            <img :src="a.cover || '/img/news-default.svg'" :alt="a.title" />
          </router-link>
          <div class="art-body">
            <router-link :to="`/article/${a.id}`" class="art-title serif">
              <el-tag size="small" effect="plain" style="margin-right:6px">{{ a.categoryLabel }}</el-tag>{{ a.title }}
            </router-link>
            <p class="art-sum">{{ a.summary }}</p>
            <div class="art-meta">
              <span><el-icon><Calendar /></el-icon> {{ fmt(a.publishedAt) }}</span>
              <span><el-icon><View /></el-icon> {{ a.views }} 次浏览</span>
            </div>
          </div>
        </li>
      </ul>
      <el-empty v-else-if="keyword" description="没有找到相关内容,换个关键词试试" />
      <el-empty v-else description="请输入关键词开始搜索" />
    </div>

    <div class="pager" v-if="data.total > pageSize">
      <el-pagination background layout="prev, pager, next" :total="data.total"
        :page-size="pageSize" :current-page="page" @current-change="onPage" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchArticles } from '../../api'
import { Calendar, View, Search as SearchIcon } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const data = ref({ list: [], total: 0 })
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const kw = ref(route.query.q || '')
const keyword = ref(route.query.q || '')

async function load() {
  if (!keyword.value) { data.value = { list: [], total: 0 }; return }
  loading.value = true
  try {
    data.value = await searchArticles({ q: keyword.value, page: page.value, size: pageSize })
  } finally { loading.value = false }
}
function doSearch() {
  const q = kw.value.trim()
  if (!q) return
  router.push({ name: 'search', query: { q } })
}
function onPage(p) { page.value = p; load() }
function fmt(s) { return s ? String(s).slice(0, 10) : '' }

watch(() => route.query.q, (v) => { keyword.value = v || ''; kw.value = v || ''; page.value = 1; load() })
onMounted(load)
</script>

<style scoped>
.list-page { padding: 24px 16px 40px; }
.crumb { font-size: 13px; color: #8a7f72; margin-bottom: 14px; }
.search-bar { max-width: 560px; margin: 0 0 20px; }
.result-tip { color: #7a7167; font-size: 14px; margin: 0 0 14px; }
.result-tip b { color: var(--shishi-red); }
.art-list { list-style: none; margin: 0; padding: 0; }
.art-item { display: flex; gap: 18px; background: #fff; border-radius: 10px; padding: 16px; margin-bottom: 16px; box-shadow: 0 2px 10px rgba(0,0,0,.05); }
.art-cover { flex: 0 0 200px; height: 130px; border-radius: 8px; overflow: hidden; }
.art-cover img { width: 100%; height: 100%; object-fit: cover; }
.art-body { flex: 1; min-width: 0; }
.art-title { font-size: 19px; font-weight: 700; color: var(--shishi-ink); display: block; }
.art-title:hover { color: var(--shishi-red); }
.art-sum { color: #7a7167; font-size: 14px; line-height: 1.7; margin: 10px 0; }
.art-meta { display: flex; gap: 20px; color: #a89e91; font-size: 13px; }
.art-meta span { display: inline-flex; align-items: center; gap: 4px; }
.pager { display: flex; justify-content: center; margin-top: 24px; }
@media (max-width: 860px) {
  .art-item { flex-direction: column; }
  .art-cover { flex: none; width: 100%; height: 180px; }
}
</style>
