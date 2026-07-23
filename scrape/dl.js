const https = require('https');
const fs = require('fs');
const path = require('path');

const urls = process.argv.slice(2);
const dir = path.join(__dirname, 'imgs');
fs.mkdirSync(dir, { recursive: true });

function get(url, file, redirects) {
  redirects = redirects || 0;
  return new Promise((resolve) => {
    const req = https.get(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36',
        'Referer': 'https://baike.baidu.com/',
      },
    }, (res) => {
      if ([301, 302, 303, 307, 308].includes(res.statusCode) && res.headers.location && redirects < 5) {
        res.resume();
        return resolve(get(res.headers.location, file, redirects + 1));
      }
      if (res.statusCode !== 200) { res.resume(); return resolve({ file, status: res.statusCode }); }
      const ct = res.headers['content-type'] || '';
      const ext = ct.includes('png') ? '.png' : ct.includes('webp') ? '.webp' : ct.includes('gif') ? '.gif' : '.jpg';
      const full = path.join(dir, file + ext);
      const ws = fs.createWriteStream(full);
      res.pipe(ws);
      ws.on('finish', () => { ws.close(); const sz = fs.statSync(full).size; resolve({ file: full, status: 200, bytes: sz, ct }); });
    });
    req.on('error', (e) => resolve({ file, error: e.message }));
    req.setTimeout(20000, () => { req.destroy(); resolve({ file, error: 'timeout' }); });
  });
}

(async () => {
  let i = 1;
  for (const u of urls) {
    const r = await get(u, 'img' + i);
    console.log(JSON.stringify(r));
    i++;
  }
})();
