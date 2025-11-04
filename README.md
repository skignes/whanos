# Whanos

Plateforme CI/CD automatisée qui détecte votre langage, build des images Docker et déploie sur Kubernetes.

## Qu'est-ce que Whanos ?
Whanos automatise le cycle de vie complet d'une application : vous poussez votre code sur Git, et Whanos s'occupe du reste.

**Workflow :** Push Git → Détection du langage → Build Docker → Push Registry → Déploiement Kubernetes

## Langages supportés

- C (via Makefile)
- Java (via Maven)
- JavaScript (via npm)
- Python (via pip)
- Befunge

## Fonctionnalités

**Ce que fait Whanos :**
- Détecte automatiquement le langage de votre projet
- Build des images Docker optimisées (base + standalone)
- Push les images dans un registry Docker privé
- Déploie sur Kubernetes avec configuration personnalisable
- Gère les replicas, ressources CPU/RAM et exposition des ports
- Polling Git automatique pour déploiement continu

**Ce que Whanos ne fait pas :**
- Ne modifie pas votre code source
- Ne gère pas les secrets Kubernetes
- Ne supporte pas plusieurs applications dans un même dépôt
- Ne déploie pas si le fichier whanos.yml est absent (build seulement)

## Infrastructure

**Services déployés :**
- Jenkins sur https://jenkins.skignes.fr (automatisation CI/CD)
- Docker Registry sur http://www.skignes.fr (stockage des images)
- Registry UI sur http://platypus.skignes.fr (interface de gestion)

**Cluster Kubernetes :**
- Minimum 2 nodes requis
- Déploiements via Helm charts
- Services exposés automatiquement si des ports sont configurés

## Configuration

Le fichier whanos.yml (optionnel) permet de configurer :
- Nombre de replicas (instances)
- Ressources CPU et mémoire (requests/limits)
- Ports à exposer

Sans whanos.yml, Whanos build quand même l'image mais ne déploie pas sur Kubernetes.

## Technologies utilisées

Docker • Jenkins • Kubernetes • Helm • Docker Registry
