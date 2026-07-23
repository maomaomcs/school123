<template>
  <div class="container detail-page" v-loading="loading">
    <div class="crumb">
      <router-link to="/">首页</router-link> &nbsp;/&nbsp;
      <router-link :to="`/list/${art.category}`">{{ art.categoryLabel }}</router-link> &nbsp;/&nbsp;
      <span>正文</span>
    </div>
    <article class="paper" v-if="art.id">
      <h1 class="serif">{{ art.title }}</h1>
      <div class="meta">
        <span>{{ fmt(art.publishedAt) }}</span>
        <span v-if="art.source">来源:{{ art.source }}</span>
        <span v-if="art.author">作者:{{ art.author }}</span>
        <span><el-icon><View /></el-icon> {{ art.views }}</span>
      </div>
      <div class="rich-content" v-html="art.content"></div>

      <nav class="prevnext">
        <div class="pn-item">
          <span class="pn-label">上一篇</span>
          <router-link v-if="art.prev" :to="`/article/${art.prev.id}`">{{ art.prev.title }}</router-link>
          <span v-else class="pn-none">没有了</span>
        </div>
        <div class="pn-item pn-right">
          <span class="pn-label">下一篇</span>
          <router-link v-if="art.next" :to="`/article/${art.next.id}`">{{ art.next.title }}</router-link>
          <span v-else class="pn-none">没有了</span>
        </div>
      </nav>

      <div class="back">
        <router-link :to="`/list/${art.category}`">&laquo; 返回{{ art.categoryLabel }}列表</router-link>
      </div>
    </article>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle } from '../../api'
import { setMeta } from '../../utils/meta'
import { View } from '@element-plus/icons-vue'

const route = useRoute()
const art = ref({})
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    art.value = await getArticle(route.params.id)
    setMeta({
      title: art.value.title,
      description: art.value.summary || art.value.content,
      image: art.value.cover || undefined,
    })
  } finally { loading.value = false }
}
function fmt(s) { return s ? String(s).slice(0, 16).replace('T', ' ') : '' }

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.detail-page { padding: 24px 16px 40px; }
.crumb { font-size: 13px; color: #8a7f72; margin-bottom: 14px; }
.paper { background: #fff; border-radius: 10px; padding: 40px 48px; box-shadow: 0 2px 12px rgba(0,0,0,.06); border-top: 4px solid var(--shishi-red); }
h1 { font-size: 30px; color: var(--shishi-ink); text-align: center; margin: 0 0 18px; line-height: 1.4; }
.meta { display: flex; justify-content: center; flex-wrap: wrap; gap: 18px; color: #a89e91; font-size: 13px; padding-bottom: 18px; border-bottom: 1px solid #eee5d3; margin-bottom: 26px; }
.meta span { display: inline-flex; align-items: center; gap: 4px; }
.prevnext { display: flex; justify-content: space-between; gap: 20px; margin-top: 30px; padding-top: 18px; border-top: 1px dashed #e5dcc9; }
.pn-item { display: flex; flex-direction: column; gap: 4px; max-width: 48%; }
.pn-item.pn-right { text-align: right; align-items: flex-end; }
.pn-label { font-size: 12px; color: #a89e91; }
.pn-item a { color: #3a332c; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%; }
.pn-item a:hover { color: var(--shishi-red); }
.pn-none { font-size: 14px; color: #c3bab0; }
.back { margin-top: 20px; padding-top: 18px; border-top: 1px dashed #e5dcc9; }
@media (max-width: 860px) {
  .paper { padding: 24px 18px; }
  h1 { font-size: 22px; }
}
</style>
