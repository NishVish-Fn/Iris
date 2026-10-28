import requests

urls = [
    'https://raw.githubusercontent.com/oxpig/CoPriNet/main/pricePrediction/config.py',
    'https://raw.githubusercontent.com/oxpig/CoPriNet/main/data/testData/test_set_PC_scores.csv'
]

for u in urls:
    r = requests.get(u, stream=True)
    print(f'=== {u} ===')
    if r.status_code == 200:
        lines = []
        for line in r.iter_lines():
            lines.append(line.decode('utf-8', errors='ignore'))
            if len(lines) >= 10:
                break
        print('\n'.join(lines))
