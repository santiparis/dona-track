#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DONACIONES_LOG="/tmp/dona-track-donaciones.log"
LOGISTICA_LOG="/tmp/dona-track-logistica.log"

echo "Iniciando donaciones-service en http://localhost:8081"
(
  cd "$ROOT_DIR" &&
  mvn -q -f donaciones-service/pom.xml -DskipTests compile exec:java \
    -Dexec.mainClass=donaciones.Main \
    -Dexec.cleanupDaemonThreads=false
) >"$DONACIONES_LOG" 2>&1 &
DONACIONES_PID=$!

echo "Iniciando logistica-service en http://localhost:7070"
(
  cd "$ROOT_DIR" &&
  mvn -q -f logistica-service/pom.xml -DskipTests compile exec:java \
    -Dexec.mainClass=logistica.Main \
    -Dexec.cleanupDaemonThreads=false
) >"$LOGISTICA_LOG" 2>&1 &
LOGISTICA_PID=$!

cleanup() {
  kill "$DONACIONES_PID" "$LOGISTICA_PID" 2>/dev/null || true
}

trap cleanup EXIT INT TERM

echo "Servicios iniciados."
echo "Logs:"
echo "  donaciones-service -> $DONACIONES_LOG"
echo "  logistica-service   -> $LOGISTICA_LOG"
echo "Presiona Ctrl+C para detenerlos."

wait "$DONACIONES_PID" "$LOGISTICA_PID"
