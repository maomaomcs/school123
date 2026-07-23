const { chromium } = require('d:\\security agent\\balance-checker\\node_modules\\playwright');
const fs = require('fs');
const path = require('path');

const URL = process.argv[2] || 'https://baike.baidu.com/item/%E6%88%90%E9%83%BD%E5%B8%82%E7%9F%B3%E5%AE%A4%E8%81%94%E4%B8%AD132%E5%AD%A6%E6%A0%A1/66740986';
const OUT = path.join(__dirname, 'baike-out.json');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const ctx = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36',
    locale: 'zh-CN',
    ignoreHTTPSErrors: true,
  });
  const page = await ctx.newPage();
  const out = { url: URL };
  try {
    const resp = await page.goto(URL, { waitUntil: 'domcontentloaded', timeout: 45000 });
    out.status = resp ? resp.status() : null;
    await page.waitForTimeout(3500);
    out.title = await page.title();
    // 概述段落
    out.summary = await page.evaluate(() => {
      const nodes = document.querySelectorAll('[class*="lemmaSummary"] , [class*="para-title"], .para, [class*="J-summary"]');
      let txt = [];
      document.querySelectorAll('div[class*="lemmaSummary"] .para, div[class*="lemmaSummary"] span').forEach(p => { const t=p.innerText.trim(); if(t) txt.push(t); });
      if (!txt.length) {
        document.querySelectorAll('.para').forEach(p => { const t=p.innerText.trim(); if(t && t.length>10) txt.push(t); });
      }
      return txt.slice(0, 12).join('\n');
    });
    // 信息框
    out.infobox = await page.evaluate(() => {
      const pairs = [];
      document.querySelectorAll('[class*="basicInfo"] dt, [class*="basicInfoItem"]').forEach(() => {});
      const dts = document.querySelectorAll('dt[class*="basicInfoItem"], .basic-info dt');
      const dds = document.querySelectorAll('dd[class*="basicInfoItem"], .basic-info dd');
      for (let i = 0; i < Math.min(dts.length, dds.length); i++) {
        pairs.push([dts[i].innerText.trim().replace(/\s+/g,''), dds[i].innerText.trim()]);
      }
      return pairs;
    });
    // 正文全文(截断)
    out.bodyText = (await page.evaluate(() => document.body ? document.body.innerText : '')).slice(0, 4000);
    // 图片(大图)
    out.images = await page.$$eval('img', (is) => is
      .map((i) => ({ src: i.src || i.getAttribute('data-src') || '', w: i.naturalWidth, h: i.naturalHeight }))
      .filter((x) => x.src && x.w >= 200));
  } catch (e) {
    out.error = e.message;
  }
  fs.writeFileSync(OUT, JSON.stringify(out, null, 2), 'utf8');
  console.log('DONE status=%s title=%s imgs=%s err=%s', out.status, out.title, (out.images||[]).length, out.error);
  await browser.close();
})().catch((e) => { fs.writeFileSync(OUT, JSON.stringify({ fatal: e.message }, null, 2)); console.error('ERR', e.message); });
