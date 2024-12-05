# Supprimer les déploiements 'backend' et 'frontend' s'ils existent
kubectl delete deployments backend --ignore-not-found
kubectl delete deployments frontend --ignore-not-found
kubectl delete deployments kafka-1 --ignore-not-found
kubectl delete deployments kafka-2 --ignore-not-found
kubectl delete deployments zookeeper-1 --ignore-not-found
kubectl delete deployments zookeeper-2 --ignore-not-found
kubectl delete deployments ai --ignore-not-found

# Supprimer les services 'backend' et 'frontend' s'ils existent
kubectl delete services backend --ignore-not-found
kubectl delete services frontend --ignore-not-found
kubectl delete services kafka-1 --ignore-not-found
kubectl delete services kafka-2 --ignore-not-found
kubectl delete services zookeeper-1 --ignore-not-found
kubectl delete services zookeeper-2 --ignore-not-found
kubectl delete services ai --ignore-not-found

sleep 15

kubectl create -f kafka-zookeeper.yaml
sleep 10
kubectl create -f kafka.yaml
sleep 15
kubectl create -f backend-deployment.yaml
kubectl create -f backend-service.yaml
sleep 30
kubectl create -f frontend-deployment.yaml
kubectl create -f frontend-service.yaml
kubectl create -f ia.yaml

minikube service backend
minikube service frontend

kubectl port-forward service/backend 8080:8080