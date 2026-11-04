# Architecture — TRS Episode 01

## Objective

Demonstrate a repeatable, verifiable deployment of a Spring Boot REST API using Ansible and rootless Podman.

## Intended workflow

1. Build and test the Spring Boot application.
2. Build a versioned application container image.
3. Transfer the image to the target runtime.
4. Load the image into rootless Podman.
5. Use Ansible to create or reconcile the application container.
6. Wait for the application health endpoint to report healthy.
7. Run the playbook again and inspect whether any changes are necessary.
8. Remove the application container manually and verify that Ansible restores it.

## Security principles

- Run application containers as an unprivileged user wherever practical.
- Do not expose an unauthenticated Podman API over the network.
- Do not mount the host Podman socket into the automation container without a deliberate security review.
- Use SSH key authentication and verified `known_hosts` entries.
- Never commit private keys, access tokens, passwords, or other credentials.
- Keep environment-specific settings out of reusable role defaults.

## Verification requirements

The implementation is complete only when the following are demonstrated:

- The application builds and tests successfully.
- The deployment playbook runs successfully.
- The application responds over HTTP.
- The health endpoint reports healthy.
- A second run demonstrates idempotency based on actual Ansible results.
- Removing the application container and rerunning the playbook restores service.


