"""Download the Google Fonts used by the game into the app so it looks right offline.
Run from the repository root (the CI build does this automatically)."""
import os, re, sys, urllib.request

HTML = 'app/src/main/assets/www/index.html'
OUT = 'app/src/main/assets/www/fonts'
UA = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36'}

def get(url):
    return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=30).read()

s = open(HTML, encoding='utf-8').read()
m = re.search(r'<link rel="stylesheet" href="(https://fonts\.googleapis\.com/css2[^"]+)">', s)
if not m:
    print('fonts already bundled'); sys.exit(0)
css = get(m.group(1).replace('&amp;', '&')).decode()
os.makedirs(OUT, exist_ok=True)

def local(match):
    url = match.group(1)
    name = re.sub(r'[^A-Za-z0-9._-]', '_', url.split('/')[-1])
    with open(os.path.join(OUT, name), 'wb') as f:
        f.write(get(url))
    return f'url({name})'

css = re.sub(r'url\((https://fonts\.gstatic\.com/[^)]+)\)', local, css)
open(os.path.join(OUT, 'fonts.css'), 'w').write(css)
s = s.replace(m.group(0), '<link rel="stylesheet" href="fonts/fonts.css">')
s = re.sub(r'<link rel="preconnect"[^>]*>\n?', '', s)
open(HTML, 'w', encoding='utf-8').write(s)
print('bundled', len(os.listdir(OUT)) - 1, 'font files')
