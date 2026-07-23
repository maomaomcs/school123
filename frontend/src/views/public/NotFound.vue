<template>
  <div class="container notfound">
    <div class="nf-seal serif">石</div>
    <div class="nf-code serif">404</div>
    <div class="nf-title serif">页面走失了</div>
    <p class="nf-desc">您访问的页面不存在或已被移除,可能是链接失效或地址输入有误。</p>
    <div class="nf-actions">
      <router-link to="/"><el-button type="primary" round>返回首页</el-button></router-link>
      <router-link to="/list/xwzx"><el-button round>看看校园新闻</el-button></router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.notfound {
  min-height: 52vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 60px 16px;
}
.nf-seal {
  width: 64px; height: 64px; border-radius: 10px;
  background: var(--shishi-red); border: 2px solid var(--shishi-gold);
  color: var(--shishi-paper); font-size: 38px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 18px;
}
.nf-code { font-size: 72px; font-weight: 700; color: var(--shishi-red); line-height: 1; letter-spacing: 4px; }
.nf-title { font-size: 24px; color: var(--shishi-ink); margin: 10px 0 6px; }
.nf-desc { color: #7a7167; font-size: 15px; line-height: 1.7; max-width: 460px; margin: 0 0 24px; }
.nf-actions { display: flex; gap: 14px; flex-wrap: wrap; justify-content: center; }
</style>
