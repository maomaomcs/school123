<template>
  <div v-loading="loading">
    <div class="section-title">
      <h2>数据统计</h2>
      <el-radio-group v-model="days" size="small" @change="load">
        <el-radio-button :value="7">近7天</el-radio-button>
        <el-radio-button :value="30">近30天</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 概览卡片 -->
    <div class="cards">
      <div class="card">
        <div class="c-label">今日访客(UV)</div>
        <div class="c-num">{{ data.today?.uv ?? 0 }}</div>
        <div class="c-sub">昨日 {{ data.yesterday?.uv ?? 0 }}</div>
      </div>
      <div class="card">
        <div class="c-label">今日访问量(PV)</div>
        <div class="c-num">{{ data.today?.pv ?? 0 }}</div>
        <div class="c-sub">昨日 {{ data.yesterday?.pv ?? 0 }}</div>
      </div>
      <div class="card">
        <div class="c-label">近{{ data.range?.days ?? days }}天访客</div>
        <div class="c-num">{{ data.range?.uv ?? 0 }}</div>
        <div class="c-sub">去重 UV</div>
      </div>
      <div class="card">
        <div class="c-label">近{{ data.range?.days ?? days }}天访问量</div>
        <div class="c-num">{{ data.range?.pv ?? 0 }}</div>
        <div class="c-sub">总 PV</div>
      </div>
    </div>

    <!-- 趋势 -->
    <div class="panel">
      <div class="p-title">访问趋势</div>
      <div class="trend" v-if="trend.length">
        <div class="t-col" v-for="t in trend" :key="t.day">
          <div class="t-bars">
            <div class="bar pv" :style="{ height: barH(t.pv) + 'px' }" :title="`PV ${t.pv}`"></div>
            <div class="bar uv" :style="{ height: barH(t.uv) + 'px' }" :title="`UV ${t.uv}`"></div>
          </div>
          <div class="t-day">{{ t.day.slice(5) }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无数据" :image-size="80" />
      <div class="legend"><span class="dot pv"></span>访问量 PV <span class="dot uv"></span>访客 UV</div>
    </div>

    <div class="two-col">
      <!-- 来源 -->
      <div class="panel">
        <div class="p-title">访问来源 Top</div>
        <ul class="rank" v-if="data.referers?.length">
          <li v-for="(r, i) in data.referers" :key="i">
            <span class="r-name">{{ r.name }}</span>
            <div class="r-bar"><div :style="{ width: pct(r.count, maxRef) + '%' }"></div></div>
            <span class="r-num">{{ r.count }}</span>
          </li>
        </ul>
        <el-empty v-else description="暂无数据" :image-size="70" />
      </div>

      <!-- 热门页面 -->
      <div class="panel">
        <div class="p-title">热门页面 Top</div>
        <ul class="rank" v-if="data.pages?.length">
          <li v-for="(p, i) in data.pages" :key="i">
            <span class="r-name" :title="p.name">{{ pageLabel(p.name) }}</span>
            <div class="r-bar"><div :style="{ width: pct(p.count, maxPage) + '%' }"></div></div>
            <span class="r-num">{{ p.count }}</span>
          </li>
        </ul>
        <el-empty v-else description="暂无数据" :image-size="70" />
      </div>
    </div>

    <!-- 访问地区 -->
    <div class="panel">
      <div class="p-title">访问地区 Top(按 IP 归属地)</div>
      <ul class="rank" v-if="data.regions?.length">
        <li v-for="(r, i) in data.regions" :key="i">
          <span class="r-name" :title="r.name">{{ r.name }}</span>
          <div class="r-bar"><div :style="{ width: pct(r.count, maxRegion) + '%' }"></div></div>
          <span class="r-num">{{ r.count }}</span>
        </li>
      </ul>
      <el-empty v-else description="暂无数据" :image-size="70" />
    </div>

    <!-- 设备 -->
    <div class="panel">
      <div class="p-title">访问设备</div>
      <div class="devices" v-if="data.devices?.length">
        <div class="dev" v-for="d in data.devices" :key="d.name">
          <span class="dev-name">{{ d.name === 'mobile' ? '手机' : (d.name === 'desktop' ? '电脑' : d.name) }}</span>
          <span class="dev-num">{{ d.count }}</span>
          <span class="dev-pct">{{ pct(d.count, devTotal) }}%</span>
        </div>
      </div>
      <el-empty v-else description="暂无数据" :image-size="70" />
    </div>

    <p class="tip">说明:UV 按「访客当天去重」估算(IP+浏览器指纹,IP 已脱敏不留明文);统计仅覆盖前台页面,后台操作不计入。</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminAnalytics } from '../../api'

const loading = ref(false)
const days = ref(7)
const data = ref({})

const trend = computed(() => data.value.trend || [])
const maxTrend = computed(() => Math.max(1, ...trend.value.map(t => Math.max(t.pv, t.uv))))
const maxRef = computed(() => Math.max(1, ...(data.value.referers || []).map(r => r.count)))
const maxPage = computed(() => Math.max(1, ...(data.value.pages || []).map(p => p.count)))
const maxRegion = computed(() => Math.max(1, ...(data.value.regions || []).map(r => r.count)))
const devTotal = computed(() => (data.value.devices || []).reduce((s, d) => s + d.count, 0) || 1)

function barH(v) { return Math.round((v / maxTrend.value) * 120) }
function pct(v, max) { return Math.round((v / max) * 100) }
function pageLabel(path) {
  const map = { '/': '首页', '/teachers': '师资队伍', '/contact': '联系我们', '/search': '站内搜索' }
  return map[path] || path
}

async function load() {
  loading.value = true
  try { data.value = await adminAnalytics(days.value) } finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped>
.cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 18px; }
.card { background: #fff; border-radius: 10px; padding: 18px 20px; box-shadow: 0 2px 10px rgba(0,0,0,.05); border-top: 3px solid var(--shishi-red); }
.c-label { font-size: 13px; color: #8a7f72; }
.c-num { font-size: 34px; font-weight: 700; color: var(--shishi-red-deep); line-height: 1.2; margin: 4px 0; }
.c-sub { font-size: 12px; color: #a89e91; }

.panel { background: #fff; border-radius: 10px; padding: 18px 20px; box-shadow: 0 2px 10px rgba(0,0,0,.05); margin-bottom: 18px; }
.p-title { font-size: 15px; font-weight: 700; color: var(--shishi-ink); margin-bottom: 16px; }

.trend { display: flex; align-items: flex-end; gap: 8px; height: 150px; overflow-x: auto; padding-bottom: 4px; }
.t-col { display: flex; flex-direction: column; align-items: center; flex: 1; min-width: 26px; }
.t-bars { display: flex; align-items: flex-end; gap: 3px; height: 130px; }
.bar { width: 10px; border-radius: 3px 3px 0 0; transition: height .3s; }
.bar.pv { background: var(--shishi-red); }
.bar.uv { background: var(--shishi-gold); }
.t-day { font-size: 11px; color: #a89e91; margin-top: 6px; white-space: nowrap; }
.legend { font-size: 12px; color: #8a7f72; margin-top: 12px; }
.legend .dot { display: inline-block; width: 10px; height: 10px; border-radius: 2px; margin: 0 4px 0 12px; vertical-align: middle; }
.legend .dot.pv { background: var(--shishi-red); }
.legend .dot.uv { background: var(--shishi-gold); }

.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
.rank { list-style: none; margin: 0; padding: 0; }
.rank li { display: flex; align-items: center; gap: 10px; padding: 7px 0; }
.r-name { flex: 0 0 34%; font-size: 13px; color: #3a332c; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.r-bar { flex: 1; background: #f0e9db; border-radius: 4px; height: 14px; overflow: hidden; }
.r-bar > div { height: 100%; background: linear-gradient(90deg, var(--shishi-gold), var(--shishi-red)); border-radius: 4px; }
.r-num { flex: 0 0 44px; text-align: right; font-size: 13px; color: #7a7167; }

.devices { display: flex; gap: 28px; }
.dev { display: flex; flex-direction: column; }
.dev-name { font-size: 13px; color: #8a7f72; }
.dev-num { font-size: 26px; font-weight: 700; color: var(--shishi-red-deep); }
.dev-pct { font-size: 12px; color: #a89e91; }

.tip { font-size: 12px; color: #a89e91; margin-top: 8px; }

@media (max-width: 768px) {
  .cards { grid-template-columns: 1fr 1fr; }
  .two-col { grid-template-columns: 1fr; }
}
</style>
