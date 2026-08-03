import { chromium } from '/opt/node22/lib/node_modules/playwright/index.mjs';
const b = await chromium.launch({ executablePath:'/opt/pw-browsers/chromium-1194/chrome-linux/chrome',
  args:['--no-sandbox','--force-color-profile=srgb'] });
for (const [file, w, h] of [['icon',512,512], ['feature',1024,500]]) {
  const p = await b.newPage({ viewport:{width:w,height:h}, deviceScaleFactor:1 });
  await p.goto(`file:///home/user/spark-gym/store/${file}.html`);
  await p.waitForTimeout(350);
  await p.screenshot({ path:`/home/user/spark-gym/store/${file}.png`, omitBackground:false });
  console.log(file, w+'x'+h);
  await p.close();
}
await b.close();
