import { createRouter, createWebHistory } from 'vue-router'
import { setMeta } from '../utils/meta'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/PublicLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('../views/public/Home.vue'), meta: { title: '' } },
      { path: 'list/:category', name: 'list', component: () => import('../views/public/ArticleList.vue') },
      { path: 'search', name: 'search', component: () => import('../views/public/Search.vue'), meta: { title: '站内搜索' } },
      { path: 'article/:id', name: 'article', component: () => import('../views/public/ArticleDetail.vue') },
      { path: 'page/:key', name: 'page', component: () => import('../views/public/SinglePage.vue') },
      { path: 'teachers', name: 'teachers', component: () => import('../views/public/Teachers.vue'), meta: { title: '师资队伍' } },
      { path: 'contact', name: 'contact', component: () => import('../views/public/Contact.vue'), meta: { title: '联系我们' } },
      { path: ':pathMatch(.*)*', name: 'notFound', component: () => import('../views/public/NotFound.vue'), meta: { title: '页面未找到' } },
    ],
  },
  { path: '/admin/login', name: 'adminLogin', component: () => import('../views/admin/Login.vue') },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/articles' },
      { path: 'articles', name: 'adminArticles', component: () => import('../views/admin/Articles.vue') },
      { path: 'articles/new', name: 'adminArticleNew', component: () => import('../views/admin/ArticleEdit.vue') },
      { path: 'articles/:id', name: 'adminArticleEdit', component: () => import('../views/admin/ArticleEdit.vue') },
      { path: 'banners', name: 'adminBanners', component: () => import('../views/admin/Banners.vue') },
      { path: 'teachers', name: 'adminTeachers', component: () => import('../views/admin/Teachers.vue') },
      { path: 'pages', name: 'adminPages', component: () => import('../views/admin/Pages.vue') },
      { path: 'messages', name: 'adminMessages', component: () => import('../views/admin/Messages.vue') },
      { path: 'accounts', name: 'adminAccounts', component: () => import('../views/admin/Accounts.vue') },
      { path: 'password', name: 'adminPassword', component: () => import('../views/admin/ChangePassword.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  if (to.meta.requiresAdmin && !localStorage.getItem('admin_token')) {
    return { name: 'adminLogin', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  // 后台页统一标题;前台静态页按 meta.title 设置;
  // 动态页(文章详情/栏目/单页)由各自组件加载数据后自行 setMeta。
  if (to.path.startsWith('/admin')) {
    setMeta({ title: '管理后台', rawTitle: false })
  } else if (to.meta && to.meta.title !== undefined) {
    setMeta({ title: to.meta.title })
  }
})

export default router
