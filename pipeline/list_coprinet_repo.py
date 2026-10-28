import requests
import json

url = 'https://api.github.com/repos/oxpig/CoPriNet/contents'
r = requests.get(url)
print('Status:', r.status_code)
if r.status_code == 200:
    for item in r.json():
        print(item['name'], item['type'], item.get('size', ''))
