import requests

urls = [
    'https://drugdesign.unistra.fr/LIT-PCBA/Files/full_data.tgz',
    'https://drugdesign.unistra.fr/LIT-PCBA/Files/ALDH1.tgz',
    'https://drugdesign.unistra.fr/LIT-PCBA/Files/ALDH1.tar.gz',
    'https://drugdesign.unistra.fr/LIT-PCBA/Files/ALDH1/active.smi',
    'https://drugdesign.unistra.fr/LIT-PCBA/Files/ALDH1/inactive.smi',
]

for u in urls:
    try:
        r = requests.head(u, timeout=5, allow_redirects=True)
        print(u, '->', r.status_code, r.headers.get('content-length'), r.headers.get('content-type'))
    except Exception as e:
        print(u, 'error:', e)
