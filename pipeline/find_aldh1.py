import requests
import re

url = 'https://drugdesign.unistra.fr/LIT-PCBA/'
try:
    r = requests.get(url, timeout=10)
    print('Home status:', r.status_code)
    links = re.findall(r'href=[\'"](.*?)[\'"]', r.text)
    for l in links:
        if any(k in l.lower() for k in ['aldh', 'download', 'dataset', 'zenodo', '.tar', '.zip', '.smi', 'target']):
            print('Candidate link:', l)
except Exception as e:
    print('Error:', e)
