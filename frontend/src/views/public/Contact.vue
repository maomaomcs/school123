<template>
  <div class="container contact-page">
    <div class="crumb"><router-link to="/">首页</router-link> &nbsp;/&nbsp; <span>联系我们</span></div>
    <div class="section-title"><h2>联系我们</h2></div>
    <div class="grid">
      <div class="paper info" v-loading="loading">
        <div class="rich-content" v-html="page.content"></div>
      </div>
      <div class="paper form">
        <h3 class="serif">在线留言</h3>
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-form-item label="称呼" prop="name">
            <el-input v-model="form.name" maxlength="60" placeholder="您的姓名/称呼" />
          </el-form-item>
          <el-form-item label="联系方式" prop="contact">
            <el-input v-model="form.contact" maxlength="120" placeholder="电话或邮箱(选填)" />
          </el-form-item>
          <el-form-item label="留言内容" prop="content">
            <el-input v-model="form.content" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="请输入您的咨询或建议" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submit">提交留言</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPage, submitMessage } from '../../api'

const page = ref({ content: '' })
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ name: '', contact: '', content: '' })
const rules = {
  name: [{ required: true, message: '请填写称呼', trigger: 'blur' }],
  content: [{ required: true, message: '请填写留言内容', trigger: 'blur' }],
}

onMounted(async () => {
  loading.value = true
  try { page.value = await getPage('contact') } catch (e) {} finally { loading.value = false }
})

async function submit() {
  await formRef.value.validate(async (ok) => {
    if (!ok) return
    submitting.value = true
    try {
      const res = await submitMessage({ ...form })
      ElMessage.success(res.message || '提交成功')
      form.name = ''; form.contact = ''; form.content = ''
    } finally { submitting.value = false }
  })
}
</script>

<style scoped>
.contact-page { padding: 24px 16px 40px; }
.crumb { font-size: 13px; color: #8a7f72; margin-bottom: 14px; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.paper { background: #fff; border-radius: 10px; padding: 30px 34px; box-shadow: 0 2px 12px rgba(0,0,0,.06); border-top: 4px solid var(--shishi-red); }
.form h3 { color: var(--shishi-red-deep); margin-top: 0; }
@media (max-width: 860px) { .grid { grid-template-columns: 1fr; } }
</style>
