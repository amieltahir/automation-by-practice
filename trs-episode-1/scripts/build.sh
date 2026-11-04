#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

VERSION="${1:-$(git rev-parse --short HEAD)}"

if [[ ! "$VERSION" =~ ^[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$ ]]; then
  echo "Invalid image tag: $VERSION" >&2
  exit 1
fi

podman run --rm --user 0:0 -v "$PWD:/workspace:Z" -v trs-m2:/root/.m2 \
  -w /workspace/app/trs-demo-api --entrypoint bash trs-episode-01-dev:local \
  -lc './mvnw -B clean test package'

podman build -t "trs-demo-api:${VERSION}" app/trs-demo-api

mkdir -p .build
printf '%s\n' "$VERSION" > .build/image-tag

echo "Built trs-demo-api:${VERSION}"
echo "Image tag saved to .build/image-tag"
