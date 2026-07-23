const { chromium } = require('d:\\security agent\\balance-checker\\node_modules\\playwright');
const fs = require('fs');
const path = require('path');

const BASE = 'http://localhost:8083';
const OUT = path.join(__dirname, 'shots');
fs.mkdirSync(OUT, { recursive: true });

const pages = [
  ['home', '/'],
  ['list', '/list/xwzx'],
  ['detail', '/article/1'],
  ['teachers', '/teachers'],
  ['contact', '/contact'],
  ['about', '/page/intro'],
  ['adminlogin', '/admin/login'],
];

(async () => {
  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-gpu', '--single-process', '--disable-extensions', '--js-flags=--max-old-space-size=128'],
  });
  const results = [];
  for (const [device, vp] of [['mobile', { width: 390, height: 844 }], ['desktop', { width: 1280, height: 800 }]]) {
    const ctx = await browser.newContext({ viewport: vp, deviceScaleFactor: 1, isMobile: device === 'mobile', locale: 'zh-CN' });
    const page = await ctx.newPage();
    for (const [name, url] of pages) {
      try {
        await page.goto(BASE + url, { waitUntil: 'networkidle', timeout: 30000 });
        await page.waitForTimeout(800);
        const overflow = await page.evaluate(() => ({
          scrollW: document.documentElement.scrollWidth,
          clientW: document.documentElement.clientWidth,
          bodyScrollW: document.body.scrollWidth,
        }));
        const hasOverflow = overflow.scrollW > overflow.clientW + 1;
        await page.screenshot({ path: path.join(OUT, `${device}-${name}.png`), fullPage: device === 'mobile' });
        results.push({ device, name, ...overflow, hasOverflow });
      } catch (e) {
        results.push({ device, name, error: e.message });
      }
    }
    await ctx.close();
  }
  await browser.close();
  fs.writeFileSync(path.join(OUT, 'report.json'), JSON.stringify(results, null, 2), 'utf8');
  console.log('=== 横向溢出检测(hasOverflow=true 表示有横向滚动/超宽) ===');
  results.forEach((r) => console.log(`${r.device.padEnd(7)} ${String(r.name).padEnd(11)} scrollW=${r.scrollW} clientW=${r.clientW} overflow=${r.hasOverflow} ${r.error || ''}`));
})().catch((e) => { console.error('ERR', e.message); process.exit(1); });
