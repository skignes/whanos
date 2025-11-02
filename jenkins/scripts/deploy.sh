#!/usr/bin/env bash
set -euo pipefail

ROOT="${4:-.}"
NO_BUILD=false
NO_PUSH=false
NO_DEPLOY=false

usage(){
    cat <<EOF
Usage: $0 [--no-build] [--no-push] [--no-deploy] [path]
Options:
    --no-build    Skip image build
    --no-push     Skip pushing the image
    --no-deploy   Skip Kubernetes deployment
    -h, --help    Show this help and exit
EOF
}

ROOT="${ROOT:-.}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-build) NO_BUILD=true; shift ;;
    --no-push) NO_PUSH=true; shift ;;
    --no-deploy) NO_DEPLOY=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) ROOT="$1"; shift ;;
  esac
done

ROOT=$(realpath "$ROOT")

log(){
  printf "%s\n" "$*" >&2
}

if [ ! -d "$ROOT" ]; then
  log "Error: path '$ROOT' not a directory"
  exit 1
fi

detect_language(){
  local dir="$1"
  if [ -f "$dir/Makefile" ]; then echo c; return; fi
  if [ -f "$dir/app/pom.xml" ]; then echo java; return; fi
  if [ -f "$dir/package.json" ]; then echo javascript; return; fi
  if [ -f "$dir/requirements.txt" ]; then echo python; return; fi
  if [ -f "$dir/app/main.bf" ]; then echo befunge; return; fi
  echo unknown
}

LANGUAGE=$(detect_language "$ROOT")
if [ "$LANGUAGE" = "unknown" ]; then
  log "Error: Failed to determine language"
  exit 1
fi

log "Detected language: $LANGUAGE"

REPO_ROOT_DIR="/opt/jenkins/"
APP_NAME=$(basename "$ROOT")
IMAGE="127.0.0.1:5000/${APP_NAME}:latest"

build_image(){
  local dir="$1" image="$2"

  if [ -f "$dir/Dockerfile" ]; then
    log "Info: Building from provided Dockerfile"
    docker build -t "$image" -f "$dir/Dockerfile" "$dir"
    return
  fi

  local d_standalone="$REPO_ROOT_DIR/images/${LANGUAGE}/Dockerfile.standalone"

  if [ -f "$d_standalone" ]; then
    log "Info: Building using Whanos image Dockerfile: $d_standalone"
    docker build -t "$image" -f "$d_standalone" "$dir"
    return
  fi

  log "Error: no Dockerfile found"
  return 1
}

push_image(){
  local image="$1"

  log "Info: Pushing image $image"
  docker push "$image"
}

deploy(){
  local image="$1" repo_root="$2"

  log "Info: Checking for whanos.yml"

  local yml="$repo_root/whanos.yml"

  if [ ! -f "$yml" ]; then
    log "Info: no whanos.yml — skipping deploy"
    return 0
  fi

  if ! command -v helm >/dev/null 2>&1; then
    log "Error: helm command not found"
    return 1
  fi

  local chart_dir="/opt/jenkins/helm"

  log "Info: Rendering Helm template and applying to cluster"
  if ! helm template "$APP_NAME" "$chart_dir" -f "$yml" | kubectl apply -f -; then
    log "Error: helm template | kubectl apply failed"
    return 1
  fi
}

# Build -> Push -> Deploy
if [ "$NO_BUILD" = false ]; then
  log "Building image: $IMAGE"
  build_image "$ROOT" "$IMAGE"
fi

if [ "$NO_PUSH" = false ]; then
  push_image "$IMAGE"
fi

if [ "$NO_DEPLOY" = false ]; then
  deploy "$IMAGE" "$ROOT"
fi

log "Info: Done."
exit 0
