<template>
  <div class="container teachers-page" v-loading="loading">
    <div class="crumb"><router-link to="/">首页</router-link> &nbsp;/&nbsp; <span>师资队伍</span></div>
    <div class="section-title"><h2>师资队伍</h2></div>
    <div class="grid" v-if="list.length">
      <div v-for="t in list" :key="t.id" class="tc card-hover">
        <img :src="t.photo || '/img/avatar.svg'" :alt="t.name" loading="lazy" />
        <div class="info">
          <div class="name serif">{{ t.name }}</div>
          <div class="tt">{{ t.title }}</div>
          <div class="sub">任教:{{ t.subject }}</div>
          <p class="intro">{{ t.intro }}</p>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无师资信息" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTeachers } from '../../api'

const list = ref([])
const loading = ref(false)
onMounted(async () => {
  loading.value = true
  try { list.value = await getTeachers() } finally { loading.value = false }
})
</script>

<style scoped>
.teachers-page { padding: 24px 16px 40px; }
.crumb { font-size: 13px; color: #8a7f72; margin-bottom: 14px; }
.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.tc { display: flex; gap: 16px; background: #fff; border-radius: 10px; padding: 18px; box-shadow: 0 2px 10px rgba(0,0,0,.05); }
.tc img { width: 96px; height: 96px; border-radius: 8px; object-fit: cover; border: 2px solid var(--shishi-gold); flex-shrink: 0; }
.name { font-size: 20px; font-weight: 700; color: var(--shishi-red-deep); }
.tt { font-size: 13px; color: #8a7f72; margin-top: 2px; }
.sub { font-size: 14px; color: var(--shishi-red); margin: 4px 0; }
.intro { font-size: 13px; color: #7a7167; line-height: 1.6; margin: 0; }
@media (max-width: 860px) { .grid { grid-template-columns: 1fr; } }
</style>
