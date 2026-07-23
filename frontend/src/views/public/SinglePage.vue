<template>
  <div class="container single-page" v-loading="loading">
    <div class="crumb">
      <router-link to="/">首页</router-link> &nbsp;/&nbsp; <span>{{ page.title }}</span>
    </div>
    <div class="section-title"><h2>{{ page.title }}</h2></div>
    <div class="paper">
      <div class="rich-content" v-html="page.content"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPage } from '../../api'
import { setMeta } from '../../utils/meta'

const route = useRoute()
const page = ref({})
const loading = ref(false)

async function load() {
  loading.value = true
  try { page.value = await getPage(route.params.key) }
  catch (e) { page.value = { title: '页面', content: '<p>内容尚未发布</p>' } }
  finally { loading.value = false; setMeta({ title: page.value.title, description: page.value.content }) }
}
watch(() => route.params.key, load)
onMounted(load)
</script>

<style scoped>
.single-page { padding: 24px 16px 40px; }
.crumb { font-size: 13px; color: #8a7f72; margin-bottom: 14px; }
.paper { background: #fff; border-radius: 10px; padding: 36px 44px; box-shadow: 0 2px 12px rgba(0,0,0,.06); border-top: 4px solid var(--shishi-red); }
@media (max-width: 860px) { .paper { padding: 22px 18px; } }
</style>
