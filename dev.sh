#!/usr/bin/env bash
# Atajo para el entorno de desarrollo con Docker.
set -euo pipefail
cd "$(dirname "$0")"

COMPOSE=(docker compose -f docker-compose.dev.yml)

if [[ ! -d ../zarape-react ]]; then
  echo "No encuentro ../zarape-react. Clona los repos lado a lado (zarape-springboot y zarape-react)." >&2
  exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "Docker no está corriendo. Inícialo e intenta de nuevo." >&2
  exit 1
fi

case "${1:-up}" in
  up)      "${COMPOSE[@]}" up --build ;;
  bg)      "${COMPOSE[@]}" up --build -d
           echo "Front: http://localhost:5173   API: http://localhost:8080/api   MySQL: localhost:3307 (root/dev)" ;;
  pwa)     # Front en modo producción (build + preview) para probar el service worker y el modo offline
           "${COMPOSE[@]}" stop web
           echo "PWA: http://localhost:5173  (Ctrl+C para salir; './dev.sh up' vuelve al modo desarrollo)"
           "${COMPOSE[@]}" run --rm --service-ports web \
             sh -c "npm install && npm run build && npm run preview -- --host 0.0.0.0 --port 5173" ;;
  down)    "${COMPOSE[@]}" down ;;
  reset)   "${COMPOSE[@]}" down -v ;;   # borra también la base de datos
  logs)    "${COMPOSE[@]}" logs -f "${@:2}" ;;
  restart) "${COMPOSE[@]}" restart "${2:-api}" ;;
  *)
    cat <<USO
Uso: ./dev.sh [comando]
  up        levanta todo en primer plano (por defecto)
  bg        levanta todo en segundo plano
  pwa       front en modo producción para probar la PWA y el modo offline
  down      detiene los contenedores (conserva la BD)
  reset     detiene y borra volúmenes (BD limpia)
  logs [s]  sigue los logs (servicios: db, api, web)
  restart [s]  reinicia un servicio (por defecto api, para recargar cambios de Java)
USO
    ;;
esac
