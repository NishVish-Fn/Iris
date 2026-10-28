import requests

url = 'https://raw.githubusercontent.com/oxpig/CoPriNet/main/pricePrediction/predict.py'
r = requests.get(url, timeout=5)
print('predict.py status:', r.status_code)
print(r.text[:2000])
