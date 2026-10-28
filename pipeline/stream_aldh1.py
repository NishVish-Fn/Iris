import urllib.request
import tarfile
import os

url = 'https://drugdesign.unistra.fr/LIT-PCBA/Files/full_data.tgz'
out_dir = r'C:\Users\creat\.gemini\antigravity\scratch\molfunnel\data\raw'
os.makedirs(out_dir, exist_ok=True)

print('Opening remote stream from', url)
with urllib.request.urlopen(url) as response:
    with tarfile.open(fileobj=response, mode='r|gz') as tar:
        for member in tar:
            if 'aldh1' in member.name.lower():
                print('Found ALDH1 member:', member.name, member.size)
                # extract to data/raw
                tar.extract(member, path=out_dir)
print('Done streaming ALDH1 files!')
