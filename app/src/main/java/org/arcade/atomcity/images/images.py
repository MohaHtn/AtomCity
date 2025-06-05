import requests
from bs4 import BeautifulSoup
import os

# URL de la page contenant la liste complète des chansons
url = 'https://silentblue.remywiki.com/maimai:FiNALE/Complete_Songlist'

# Effectuer une requête GET pour récupérer le contenu de la page
response = requests.get(url)
response.raise_for_status()  # Vérifier que la requête a réussi

# Analyser le contenu HTML de la page avec BeautifulSoup
soup = BeautifulSoup(response.text, 'html.parser')

# Trouver tous les liens d'images dans la page
images = soup.find_all('img')

# Créer un dossier pour enregistrer les pochettes
os.makedirs('artworks', exist_ok=True)

# Télécharger chaque image
for img in images:
    img_url = img['src']
    # Vérifier si l'URL de l'image est complète, sinon la compléter
    if not img_url.startswith('http'):
        img_url = 'https://silentblue.remywiki.com' + img_url
    # Récupérer le nom de fichier de l'image
    img_name = os.path.basename(img_url)
    # Télécharger l'image
    img_response = requests.get(img_url)
    img_response.raise_for_status()
    # Enregistrer l'image dans le dossier 'artworks'
    with open(os.path.join('artworks', img_name), 'wb') as f:
        f.write(img_response.content)
    print(f'Téléchargé : {img_name}')
