import requests

for sub in ['data/testData', 'data/models']:
    url = f'https://api.github.com/repos/oxpig/CoPriNet/contents/{sub}'
    r = requests.get(url)
    print(f'=== {sub} ===')
    if r.status_code == 200:
        for item in r.json():
            print(item['name'], item['type'], item.get('size', ''))
