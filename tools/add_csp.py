"""Add a strict Content Security Policy to the app page.
The game's one inline <script> is allowed by its SHA-256 hash, so any script
smuggled in through text from another player can't run. Run after any edit."""
import base64, hashlib, re

HTML = 'app/src/main/assets/www/index.html'
s = open(HTML, encoding='utf-8').read()
scripts = re.findall(r'<script>(.*?)</script>', s, flags=re.S)
hashes = ' '.join("'sha256-%s'" % base64.b64encode(hashlib.sha256(x.encode('utf-8')).digest()).decode() for x in scripts)
policy = ("default-src 'none'; "
          f"script-src {hashes}; "
          "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
          "font-src 'self' https://fonts.gstatic.com; "
          "img-src 'self' data: blob:; "
          "media-src 'none'; connect-src 'none'; frame-src 'none'; object-src 'none'; "
          "base-uri 'none'; form-action 'none'")
meta = f'<meta http-equiv="Content-Security-Policy" content="{policy}">'
s = re.sub(r'<meta http-equiv="Content-Security-Policy"[^>]*>\n?', '', s)
s = s.replace('<meta charset="utf-8">', '<meta charset="utf-8">\n' + meta, 1)
open(HTML, 'w', encoding='utf-8').write(s)
print('CSP set for', len(scripts), 'script(s)')
