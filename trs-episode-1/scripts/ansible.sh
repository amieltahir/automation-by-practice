#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${TRS_TARGET_HOST:?set TRS_TARGET_HOST}"
exec podman run --rm --user 0:0 \
  -v "$PWD:/workspace:Z" \
  -v "$HOME/.ssh/trs_episode01:/root/.ssh/trs_episode01:ro" \
  -v "$HOME/.ssh/known_hosts_trs:/root/.ssh/known_hosts:ro" \
  -e TRS_TARGET_HOST -e TRS_IMAGE_TAG \
  -w /workspace/ansible \
  --entrypoint bash trs-episode-01-dev:local -lc "$*"
