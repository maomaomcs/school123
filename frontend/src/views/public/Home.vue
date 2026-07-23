<template>
  <div class="home" v-loading="loading">
    <!-- 轮播 -->
    <el-carousel v-if="data.banners && data.banners.length" height="440px" :interval="5000" arrow="hover" class="banner">
      <el-carousel-item v-for="b in data.banners" :key="b.id">
        <a v-if="b.link" class="banner-item" :href="b.link" target="_blank" rel="noopener">
          <img :src="b.imageUrl" :alt="b.title" />
          <div class="banner-caption serif" v-if="b.title">
            <div class="bc-title">{{ b.title }}</div>
            <div class="bc-sub">{{ b.subtitle }}</div>
          </div>
        </a>
        <div v-else class="banner-item">
          <img :src="b.imageUrl" :alt="b.title" />
          <div class="banner-caption serif" v-if="b.title">
            <div class="bc-title">{{ b.title }}</div>
            <div class="bc-sub">{{ b.subtitle }}</div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="container">
      <!-- 快捷入口 -->
      <div class="quick-links">
        <router-link v-for="q in quickLinks" :key="q.to" :to="q.to" class="ql card-hover">
          <el-icon class="ql-icon"><component :is="q.icon" /></el-icon>
          <span class="serif">{{ q.label }}</span>
        </router-link>
      </div>

      <!-- 新闻 + 通知 -->
      <div class="news-row">
        <section class="news-block">
          <div class="section-title">
            <h2>校园新闻</h2>
            <router-link class="more" to="/list/xwzx">更多 +</router-link>
          </div>
          <div class="feature" v-if="firstNews">
            <router-link :to="`/article/${firstNews.id}`" class="feature-img card-hover">
              <img :src="firstNews.cover || '/img/news-default.svg'" :alt="firstNews.title" />
            </router-link>
            <div class="feature-body">
              <router-link :to="`/article/${firstNews.id}`" class="feature-title serif">{{ firstNews.title }}</router-link>
              <p class="feature-sum">{{ firstNews.summary }}</p>
              <span class="feature-date">{{ fmt(firstNews.publishedAt) }}</span>
            </div>
          </div>
          <ul class="news-list">
            <li v-for="n in restNews" :key="n.id">
              <router-link :to="`/article/${n.id}`" class="nl-title">{{ n.title }}</router-link>
              <span class="nl-date">{{ fmtShort(n.publishedAt) }}</span>
            </li>
          </ul>
        </section>

        <section class="notice-block">
          <div class="section-title">
            <h2>通知公告</h2>
            <router-link class="more" to="/list/tzgg">更多 +</router-link>
          </div>
          <ul class="notice-list">
            <li v-for="n in notices" :key="n.id">
              <div class="date-badge serif">
                <span class="d">{{ day(n.publishedAt) }}</span>
                <span class="m">{{ month(n.publishedAt) }}</span>
              </div>
              <router-link :to="`/article/${n.id}`" class="notice-title">{{ n.title }}</router-link>
            </li>
          </ul>
        </section>
      </div>

      <!-- 教学 + 德育 -->
      <div class="news-row">
        <section class="news-block">
          <div class="section-title">
            <h2>教育教学</h2>
            <router-link class="more" to="/list/jyjx">更多 +</router-link>
          </div>
          <ul class="news-list">
            <li v-for="n in sections.jyjx" :key="n.id">
              <router-link :to="`/article/${n.id}`" class="nl-title">{{ n.title }}</router-link>
              <span class="nl-date">{{ fmtShort(n.publishedAt) }}</span>
            </li>
          </ul>
        </section>
        <section class="news-block">
          <div class="section-title">
            <h2>德育天地</h2>
            <router-link class="more" to="/list/dycd">更多 +</router-link>
          </div>
          <ul class="news-list">
            <li v-for="n in sections.dycd" :key="n.id">
              <router-link :to="`/article/${n.id}`" class="nl-title">{{ n.title }}</router-link>
              <span class="nl-date">{{ fmtShort(n.publishedAt) }}</span>
            </li>
          </ul>
        </section>
      </div>

      <!-- 校园风采 -->
      <section class="gallery-sec">
        <div class="section-title">
          <h2>校园风采</h2>
          <router-link class="more" to="/list/xyfc">更多 +</router-link>
        </div>
        <div class="gallery">
          <router-link v-for="g in data.gallery" :key="g.id" :to="`/article/${g.id}`" class="g-item card-hover">
            <img :src="g.cover || '/img/news-default.svg'" :alt="g.title" loading="lazy" />
            <div class="g-title">{{ g.title }}</div>
          </router-link>
        </div>
      </section>

      <!-- 师资预览 -->
      <section class="teacher-sec" v-if="data.teachers && data.teachers.length">
        <div class="section-title">
          <h2>名师风采</h2>
          <router-link class="more" to="/teachers">全部师资 +</router-link>
        </div>
        <div class="teacher-grid">
          <div v-for="t in data.teachers" :key="t.id" class="t-card card-hover">
            <img :src="t.photo || '/img/avatar.svg'" :alt="t.name" loading="lazy" />
            <div class="t-name serif">{{ t.name }}</div>
            <div class="t-title">{{ t.title }}</div>
            <div class="t-subject">{{ t.subject }}</div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getHome } from '../../api'
import { Reading, Bell, School, Trophy, Postcard, Message } from '@element-plus/icons-vue'

const data = ref({ banners: [], sections: {}, teachers: [], gallery: [] })
const loading = ref(false)
const sections = computed(() => data.value.sections || {})
const newsList = computed(() => sections.value.xwzx || [])
const firstNews = computed(() => newsList.value[0])
const restNews = computed(() => newsList.value.slice(1, 6))
const notices = computed(() => (sections.value.tzgg || []).slice(0, 6))

const quickLinks = [
  { to: '/page/intro', label: '学校简介', icon: School },
  { to: '/list/tzgg', label: '通知公告', icon: Bell },
  { to: '/list/jyjx', label: '教育教学', icon: Reading },
  { to: '/list/zsks', label: '招生招考', icon: Postcard },
  { to: '/teachers', label: '师资队伍', icon: Trophy },
  { to: '/contact', label: '联系我们', icon: Message },
]

function fmt(s) { return s ? String(s).slice(0, 10) : '' }
function fmtShort(s) { return s ? String(s).slice(5, 10) : '' }
function day(s) { return s ? String(s).slice(8, 10) : '' }
function month(s) { return s ? String(s).slice(5, 7) + '月' : '' }

onMounted(async () => {
  loading.value = true
  try { data.value = await getHome() } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
.banner { margin-bottom: 24px; }
.banner-item { display: block; width: 100%; height: 100%; position: relative; }
.banner-item img { width: 100%; height: 100%; object-fit: cover; }
.banner-caption {
  position: absolute; left: 8%; bottom: 20%; color: #fff;
  text-shadow: 0 2px 12px rgba(0,0,0,.4);
}
.bc-title { font-size: 40px; font-weight: 700; letter-spacing: 4px; }
.bc-sub { font-size: 20px; margin-top: 8px; color: #f0d9a8; }

.quick-links {
  display: grid; grid-template-columns: repeat(6, 1fr); gap: 14px; margin-bottom: 28px;
}
.ql {
  background: #fff; border-radius: 10px; padding: 20px 8px; text-align: center;
  color: var(--shishi-red-deep); box-shadow: 0 2px 10px rgba(0,0,0,.05);
  display: flex; flex-direction: column; align-items: center; gap: 10px;
}
.ql-icon { font-size: 30px; color: var(--shishi-red); }
.ql span { font-size: 15px; }

.news-row { display: grid; grid-template-columns: 1.4fr 1fr; gap: 28px; margin-bottom: 30px; }

.feature { display: flex; gap: 16px; margin-bottom: 14px; }
.feature-img { flex: 0 0 220px; height: 140px; border-radius: 8px; overflow: hidden; }
.feature-img img { width: 100%; height: 100%; object-fit: cover; }
.feature-body { flex: 1; }
.feature-title { font-size: 18px; font-weight: 700; color: var(--shishi-ink); display: block; }
.feature-title:hover { color: var(--shishi-red); }
.feature-sum { color: #7a7167; font-size: 14px; line-height: 1.6; margin: 8px 0; }
.feature-date { color: #a89e91; font-size: 13px; }

.news-list { list-style: none; margin: 0; padding: 0; }
.news-list li {
  display: flex; justify-content: space-between; align-items: center; gap: 12px;
  padding: 11px 0 11px 16px; border-bottom: 1px dashed #e5dcc9; position: relative;
}
.news-list li::before {
  content: ""; position: absolute; left: 0; top: 50%; transform: translateY(-50%);
  width: 6px; height: 6px; border-radius: 50%; background: var(--shishi-gold);
}
.nl-title { color: #3a332c; font-size: 15px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.nl-title:hover { color: var(--shishi-red); }
.nl-date { color: #a89e91; font-size: 13px; flex-shrink: 0; }

.notice-list { list-style: none; margin: 0; padding: 0; }
.notice-list li { display: flex; align-items: center; gap: 14px; padding: 12px 0; border-bottom: 1px dashed #e5dcc9; }
.date-badge {
  flex: 0 0 54px; text-align: center; background: var(--shishi-paper);
  border: 1px solid #e5dcc9; border-radius: 6px; padding: 4px 0;
}
.date-badge .d { display: block; font-size: 22px; font-weight: 700; color: var(--shishi-red); line-height: 1; }
.date-badge .m { display: block; font-size: 12px; color: #a89e91; }
.notice-title { color: #3a332c; font-size: 15px; line-height: 1.5; }
.notice-title:hover { color: var(--shishi-red); }

.gallery-sec, .teacher-sec { margin-bottom: 36px; }
.gallery { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
.g-item { background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,.05); }
.g-item img { width: 100%; height: 180px; object-fit: cover; display: block; }
.g-title { padding: 12px 14px; font-size: 15px; color: #3a332c; }

.teacher-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; }
.t-card { background: #fff; border-radius: 10px; padding: 16px 8px; text-align: center; box-shadow: 0 2px 10px rgba(0,0,0,.05); }
.t-card img { width: 84px; height: 84px; border-radius: 50%; object-fit: cover; border: 2px solid var(--shishi-gold); }
.t-name { font-size: 17px; font-weight: 700; margin-top: 8px; color: var(--shishi-red-deep); }
.t-title { font-size: 12px; color: #8a7f72; margin-top: 2px; }
.t-subject { font-size: 13px; color: var(--shishi-red); margin-top: 4px; }

@media (max-width: 860px) {
  .banner :deep(.el-carousel__container) { height: 200px !important; }
  .bc-title { font-size: 22px; }
  .banner-caption { left: 6%; bottom: 12%; }
  .quick-links { grid-template-columns: repeat(3, 1fr); }
  .news-row { grid-template-columns: 1fr; }
  .feature { flex-direction: column; }
  .feature-img { flex: none; width: 100%; height: 180px; }
  .gallery { grid-template-columns: 1fr 1fr; }
  .g-item img { height: 130px; }
  .teacher-grid { grid-template-columns: repeat(3, 1fr); }
  .bc-sub { font-size: 15px; }
}
</style>
