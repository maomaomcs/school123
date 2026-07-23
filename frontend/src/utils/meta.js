// 动态设置页面 meta,用于浏览器标题 + 微信/社交分享卡片(og / 微信)
const SITE_NAME = '成都市石室联中132学校'

function upsertMeta(attr, key, content) {
  if (content == null) return
  let el = document.head.querySelector(`meta[${attr}="${key}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, key)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function stripHtml(html) {
  if (!html) return ''
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  return (tmp.textContent || tmp.innerText || '').replace(/\s+/g, ' ').trim()
}

/**
 * @param {Object} opts
 * @param {string} [opts.title]        页面标题(不含站名,函数会自动拼)
 * @param {string} [opts.description]  描述/摘要,支持传富文本自动去标签
 * @param {string} [opts.image]        分享缩略图 URL
 * @param {boolean}[opts.rawTitle]     true 时 title 原样使用不拼站名
 */
export function setMeta({ title, description, image, rawTitle } = {}) {
  const fullTitle = !title ? SITE_NAME : rawTitle ? title : `${title} - ${SITE_NAME}`
  document.title = fullTitle

  const desc = stripHtml(description).slice(0, 110) || `${SITE_NAME}官方网站`
  const url = window.location.href
  const img = image || (window.location.origin + '/img/school-real.jpg')

  upsertMeta('name', 'description', desc)
  // Open Graph(微信、QQ、多数社交平台读取)
  upsertMeta('property', 'og:title', fullTitle)
  upsertMeta('property', 'og:description', desc)
  upsertMeta('property', 'og:type', 'article')
  upsertMeta('property', 'og:url', url)
  upsertMeta('property', 'og:image', img)
  upsertMeta('property', 'og:site_name', SITE_NAME)
}

export { SITE_NAME }
