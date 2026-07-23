const { chromium } = require('d:\\security agent\\balance-checker\\node_modules\\playwright');

const QUERY = process.argv[2] || '成都石室联合中学132学校';
const ENGINE = process.argv[3] || 'bing';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const ctx = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36',
    locale: 'zh-CN',
  });
  const page = await ctx.newPage();

  const url = ENGINE === 'baidu'
    ? 'https://www.baidu.com/s?wd=' + encodeURIComponent(QUERY)
    : 'https://cn.bing.com/search?q=' + encodeURIComponent(QUERY);
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 40000 });
  await page.waitForTimeout(2500);

  let results = [];
  if (ENGINE === 'baidu') {
    results = await page.$$eval('.result, .c-container', (nodes) =>
      nodes.slice(0, 12).map((n) => {
        const a = n.querySelector('a');
        return { title: (n.querySelector('h3') || {}).innerText || '', url: a ? a.href : '', snippet: (n.innerText || '').slice(0, 160) };
      }));
  } else {
    results = await page.$$eval('#b_results > li.b_algo', (nodes) =>
      nodes.slice(0, 12).map((n) => {
        const a = n.querySelector('h2 a');
        return { title: a ? a.innerText : '', url: a ? a.href : '', snippet: (n.querySelector('.b_caption') || n).innerText.slice(0, 200) };
      }));
  }
  console.log(JSON.stringify(results, null, 2));
  await browser.close();
})().catch((e) => { console.error('ERR', e.message); process.exit(1); });
