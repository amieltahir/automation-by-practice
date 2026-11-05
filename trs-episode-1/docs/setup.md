# Audience setup

This guide prepares a control machine to build and deploy the TRS Episode 01 demo API to a Linux target machine. The control machine needs Git and rootless Podman. The target needs SSH access for `trsadmin` and rootless Podman.

## 1. Clone the episode

The episode lives in a subdirectory of the repository:

```bash
git clone https://github.com/amieltahir/automation-by-practice.git
cd automation-by-practice/trs-episode-1
```

## 2. Prepare the control machine

Install Podman on the control machine and confirm it is running rootless:

```bash
podman info
```

Build the tools image used by the episode scripts:

```bash
podman build -t trs-episode-01-dev:local -f .devcontainer/Containerfile .
```

The image includes Java 21, Ansible, and the Podman CLI. Install the Ansible collection required by the deployment role into the episode's ignored local collections directory:

```bash
podman run --rm --user 0:0 \
  -v "$PWD:/workspace:Z" \
  -w /workspace/ansible \
  --entrypoint ansible-galaxy trs-episode-01-dev:local \
  collection install -r requirements.yml -p collections
```

## 3. Prepare the target and SSH access

The target must be a reachable Linux machine with SSH enabled, a `trsadmin` account, and rootless Podman available to that account. Ensure the target permits inbound TCP port `8082` from machines that need to access the demo.

Create a dedicated SSH key on the control machine if you do not already have one for this episode:

```bash
ssh-keygen -t ed25519 -f ~/.ssh/trs_episode01
```

Authorize `~/.ssh/trs_episode01.pub` for the target's `trsadmin` account using your normal provisioning process. Obtain the target's SSH host-key fingerprint from a trusted source, then record its host key:

```bash
ssh-keyscan -H TARGET_HOST > ~/.ssh/known_hosts_trs
ssh-keygen -lf ~/.ssh/known_hosts_trs
```

Compare the displayed fingerprint with the trusted fingerprint before connecting. Do not disable SSH host-key checking.

Set the target hostname or IP address in the current shell:

```bash
export TRS_TARGET_HOST=TARGET_HOST
```

Check that SSH works and presents the verified host key:

```bash
ssh -i ~/.ssh/trs_episode01 \
  -o UserKnownHostsFile="$HOME/.ssh/known_hosts_trs" \
  trsadmin@"$TRS_TARGET_HOST"
```

Exit the remote shell before continuing. Keep the private key private; never commit it to the repository.

## 4. Build and deploy

From the `trs-episode-1` directory, build and test the application, then create its container image:

```bash
./scripts/build.sh
```

The script saves the image tag in `.build/image-tag`. Deploy that image to the target:

```bash
./scripts/deploy.sh
```

The deployment script transfers the image, starts or reconciles the container, and waits for its health endpoint. To use a specific tag instead, pass the same tag to both commands:

```bash
./scripts/build.sh audience-demo
./scripts/deploy.sh audience-demo
```

## 5. Verify the deployment

From a machine that can reach the target on port `8082`, check the health endpoint:

```bash
curl http://TARGET_HOST:8082/actuator/health
```

A healthy response includes `"status":"UP"`. Run the deployment a second time to demonstrate Ansible idempotency. To demonstrate recovery, remove the container on the target and deploy again:

```bash
ssh -i ~/.ssh/trs_episode01 \
  -o UserKnownHostsFile="$HOME/.ssh/known_hosts_trs" \
  trsadmin@"$TRS_TARGET_HOST" podman rm -f trs-demo-api
./scripts/deploy.sh
```

The health check should pass again after the redeployment.

## Troubleshooting

- **SSH connection or host-key error:** Confirm `TRS_TARGET_HOST`, the authorized public key, and the verified entry in `~/.ssh/known_hosts_trs`.
- **Ansible cannot find `containers.podman`:** Re-run the collection installation command in step 2.
- **Image tag is missing:** Run `./scripts/build.sh` before `./scripts/deploy.sh`, or pass the same explicit tag to both.
- **Health check or `curl` fails:** Confirm rootless Podman is available to `trsadmin`, the deployment completed, and target firewalls allow TCP port `8082`.
