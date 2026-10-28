import requests

urls = [
    'https://raw.githubusercontent.com/oxpig/CoPriNet/main/README.md',
    'https://raw.githubusercontent.com/oxpig/CoPriNet/master/README.md',
    'https://raw.githubusercontent.com/oxpig/CoPriNet/main/LICENSE',
    'https://raw.githubusercontent.com/oxpig/CoPriNet/master/LICENSE',
    'https://api.github.com/repos/oxpig/CoPriNet/contents'
]

for u in urls:
    try:
        r = requests.get(u, timeout=5)
        print(u, r.status_code, len(r.text))
        if 'README' in u and r.status_code == 200:
            print(r.text[:1000])
        elif 'LICENSE' in u and r.status_code == 200:
            print(r.text[:500])
    except Exception as e:
        print(u, 'error:', e)
