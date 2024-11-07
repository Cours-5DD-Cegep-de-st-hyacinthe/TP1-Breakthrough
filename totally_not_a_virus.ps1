# =========================================================================================
# Script PowerShell pour gérer l'environnement Kubernetes avec Docker Desktop et minikube
# Description: 
#   Automatiser le déploiement des applications backend et frontend dans l'environnement Kubernetes local avec Docker Desktop et minikube.
# - Vérifie si Docker Desktop est en cours d'exécution ; demande à l'utilisateur de le démarrer si nécessaire.
# - Vérifie si minikube est en cours d'exécution ; le démarre automatiquement si nécessaire, avec gestion des erreurs potentielles.
# - Supprime les déploiements et services Kubernetes existants pour les applications backend et frontend afin d'assurer un déploiement propre.
# - Crée de nouveaux déploiements et services à partir des fichiers YAML fournis (`backend-deployment.yaml`, `frontend-deployment.yaml`, `backend-service.yaml`, `frontend-service.yaml`).
# - Configure l'environnement Docker pour utiliser le démon Docker de minikube, ce qui permet de construire les images Docker directement dans le contexte de minikube.
# - Construit les images Docker pour les applications backend et frontend en utilisant les Dockerfiles situés dans les répertoires respectifs.
# - Démarre le port-forwarding pour le service backend, rendant l'API accessible localement sur le port 8080.
# - Ouvre les services backend et frontend dans le navigateur par défaut via les commandes `minikube service`, facilitant ainsi l'accès aux applications déployées.
# 
# Ce script automatise et simplifie le processus de déploiement et de mise à jour des applications dans le cluster Kubernetes local, améliorant l'efficacité du développement et des tests.
#
# Ce fichier a été annoté avec le LLM o1-Preview.
# =========================================================================================

# --- Message d'avertissement -------------------------------------------------------------

Write-Host "___________     __         .__  .__                         __                   .__                     "
Write-Host "\__    ___/____/  |______  |  | |  | ___.__.   ____   _____/  |_  _____    ___  _|__|______ __ __  ______"
Write-Host "  |    | /  _ \   __\__  \ |  | |  |<   |  |  /    \ /  _ \   __\ \__  \   \  \/ /  \_  __ \  |  \/  ___/"
Write-Host "  |    |(  <_> )  |  / __ \|  |_|  |_\___  | |   |  (  <_> )  |    / __ \_  \   /|  ||  | \/  |  /\___ \ "
Write-Host "  |____| \____/|__| (____  /____/____/ ____| |___|  /\____/|__|   (____  /   \_/ |__||__|  |____//____  >"
Write-Host "                         \/          \/           \/                   \/                             \/ "

# --- Message d'avertissement -------------------------------------------------------------

# Write-Host "Veuillez vous assurer d'avoir fait mvn clean package dans le backend si vous avez modifie ce projet."
# Pause

Set-Location ./breakthrough_backend
try {
    Write-Host "Tentative de build du backend..."
    mvn clean package
    Write-Host "Build du backend reussi"
}
catch {
    Write-Host "La entative de build du backend a echoue." -ForegroundColor Red
    Write-Host "Veuillez vous assurer d'avoir fait mvn clean package dans le backend manuellement si vous avez modifie ce projet."
    Pause
}
finally {
    Set-Location ..
}

# --- Vérification et démarrage de Docker Desktop -----------------------------------------

# Vérifier si Docker Desktop est en cours d'exécution
$dockerProcess = Get-Process -Name "Docker Desktop" -ErrorAction SilentlyContinue

# Si Docker Desktop n'est pas en cours d'exécution, demander à l'utilisateur de le démarrer
while (-not $dockerProcess) {
    Write-Host "Veuillez demarrer Docker Desktop et attendre que tout soit initialise, puis appuyer sur Entree."
    Pause
    # Réinitialiser $dockerProcess après la pause pour re-vérifier
    $dockerProcess = Get-Process -Name "Docker Desktop" -ErrorAction SilentlyContinue
}

# --- Vérification et démarrage de minikube -----------------------------------------------

# Vérifier le statut de minikube
$minikubeStatus = & minikube status --format='{{.Host}}' 2>$null;

if ($minikubeStatus -ne 'Running') {
    # minikube n'est pas en cours d'exécution, le démarrer
    Write-Host "Minikube n'est pas en cours d'execution."
    Write-Host "Demarrage de minikube..."
    try {
        # Démarrer minikube
        minikube start
        Start-Sleep -Seconds 5
    } catch {
        # Gérer les erreurs lors du démarrage de minikube
        Write-Host "Erreur lors du demarrage de minikube : $($_.Exception.Message)"
        Pause
        Exit
    }
} else {
    Write-Host "Minikube est deja en cours d'execution."
}

# --- Suppression des déploiements et services existants ----------------------------------

Write-Host "Suppression des deploiements et services existants..."

# Supprimer les déploiements 'backend' et 'frontend' s'ils existent
kubectl delete deployments backend --ignore-not-found
kubectl delete deployments frontend --ignore-not-found
kubectl delete deployments kafka-1 --ignore-not-found
kubectl delete deployments kafka-2 --ignore-not-found
kubectl delete deployments zookeeper-1 --ignore-not-found
kubectl delete deployments zookeeper-2 --ignore-not-found

# Supprimer les services 'backend' et 'frontend' s'ils existent
kubectl delete services backend --ignore-not-found
kubectl delete services frontend --ignore-not-found
kubectl delete services kafka-1 --ignore-not-found
kubectl delete services kafka-2 --ignore-not-found
kubectl delete services zookeeper-1 --ignore-not-found
kubectl delete services zookeeper-2 --ignore-not-found

# --- Construction des images Docker avec minikube ----------------------------------------

Write-Host "Configuration de l'environnement Docker pour minikube..."

# Configurer l'environnement Docker pour utiliser le Docker de minikube
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# Construire l'image Docker pour le backend
Write-Host "Construction de l'image Docker pour le backend..."
docker build -t breakthrough_backend-image:latest .\breakthrough_backend\

# Construire l'image Docker pour le frontend
Write-Host "Construction de l'image Docker pour le frontend..."
docker build -t breakthrough_frontend-image:latest .\breakthrough_frontend\

# Construire l'image Docker pour l'ia
Write-Host "Construction de l'image Docker pour l'ia..."
docker build -t breakthrough_ia-image:latest .\scalakafka

# --- Création des nouveaux déploiements et services --------------------------------------

Write-Host "Creation des deploiements et services a partir des fichiers YAML..."

# Appliquer les configurations des déploiements
kubectl create -f backend-deployment.yaml
kubectl create -f frontend-deployment.yaml
kubectl create -f kafka.yaml
kubectl create -f ia.yaml

# Appliquer les configurations des services
kubectl create -f backend-service.yaml
kubectl create -f frontend-service.yaml

# --- Lancement des services et du port-forwarding ----------------------------------------

# Attendre quelques secondes pour s'assurer que le port-forwarding est établi
# Start-Sleep -Seconds 2

# Ouvrir le service frontend dans le navigateur via minikube
Write-Host "Ouverture du service frontend via minikube..."
Start-Process powershell -ArgumentList "minikube service frontend"

# Ouvrir le service backend dans le navigateur via minikube
Write-Host "Ouverture du service backend via minikube..."
Start-Process powershell -ArgumentList "minikube service backend"

# Démarrer le port-forwarding du service 'backend' pour accéder au port 8080
Write-Host "Demarrage du port-forwarding pour le service backend..."
do{  $result = kubectl port-forward service/backend 8080:8080;} while(!($result -like "error:*"));Write-Host $result; pause;

# =========================================================================================
# Fin du script
# =========================================================================================