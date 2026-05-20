const fs = require('fs');
const pngToIco = require('png-to-ico').default;

(async () => {
  try {
    const buf = await pngToIco('app_icon.png');
    fs.writeFileSync('app_icon.ico', buf);
    console.log('Icon created successfully!');
  } catch (err) {
    console.error(err);
    process.exit(1);
  }
})();
