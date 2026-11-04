#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ $# -gt 1 ]]; then
  echo "Usage: $0 [image-tag]" >&2
  exit 1
fi

if [[ $# -eq 1 ]]; then
  IMAGE_TAG="$1"
else
  TAG_FILE=".build/image-tag"

  if [[ ! -s "$TAG_FILE" ]]; then
    echo "No saved image tag found. Run ./scripts/build.sh first." >&2
    exit 1
  fi

  IMAGE_TAG="$(<"$TAG_FILE")"
fi

if [[ ! "$IMAGE_TAG" =~ ^[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$ ]]; then
  echo "Invalid image tag: $IMAGE_TAG" >&2
  exit 1
fi

echo "Deploying trs-demo-api:${IMAGE_TAG}"

TRS_IMAGE_TAG="$IMAGE_TAG" ./scripts/ansible.sh \
  'ansible-playbook playbooks/deploy.yml'
