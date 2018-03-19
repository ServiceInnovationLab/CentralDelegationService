# Build and install

The Central Delegation Service consists of one core service, and several supporting components.

The solution currently consists of the following components:

## CDS
Source code: [https://git.tools.delegation.org.nz/delegation/cds](https://git.tools.delegation.org.nz/delegation/cds)

This is the core Central Delegation Service.
It provides the delegations API.

Given a running vagrant machine, `mvn package -P deploy` will build the app and use ansible to deploy it into vagrant.

Pushing to Gitlab will trigger a Jenkins job (see `Jenkinsfile`) that builds, tests, runs Flyway migrations, and deploys into the `pocdev` environment.
CDS is installed as a systemd unit.

## Platform
Source code: [https://git.tools.delegation.org.nz/delegation/platform](https://git.tools.delegation.org.nz/delegation/platform)

Ansible playbooks and a vagrant definition.

Vagrant is intended for running on a developer's workstation.
There is also an ansible inventory for deploying to the `pocdev` environment, which consists of a number of EC2 instances on AWS.

The playbooks provided constitute a complete install of all necessary components, with one gap.
There is no play for doing an initial install of [Let's Encrypt](https://letsencrypt.org/) certificates, nor handling their renewal.
`pocdev` uses Let's Encrypt, and we had intended to use them for later environments also.

`pocdev` certs were acquired as a manual step when setting up the servers.

Pushing to Gitlab will trigger a Jenkins job (see `Jenkinsfile`) that runs ansible against the `pocdev` environment.

## Fakeme
Source code: [https://git.tools.delegation.org.nz/delegation/fakeme](https://git.tools.delegation.org.nz/delegation/fakeme)

Our stub of the RealMe login service and RCMS.

Given a running vagrant machine, `mvn package -P deploy` will build the app and use ansible to deploy it into vagrant.

Pushing to Gitlab will trigger a Jenkins job (see `Jenkinsfile`) that builds, tests, and deploys into the `pocdev` environment.
FakeMe is installed as a systemd unit.

#### Adding a SAML SP

FakeMe looks for integrated SP metadata in `/opt/fakeme/sp-metadata.xml`.
New metadata can be appended to that file - add a new `EntityDescriptor` element inside the root `EntitiesDescriptor` element.
You will need to restart FakeMe (`systemctl restart fakeme`) to pick up the new metadata.

However, since the ansible play will overwrite the metadata file, it's better to update `platform/roles/fakeme/templates/sp-metadata.xml.j2` and re-deploy FakeMe.

Neither of these options is particularly good. This is a good candidate for some useful extension work.

Also, FakeMe is dumb about user data.
It uses an in-memory database, so users will be lost on restarting the app.
Seed users can be created by adding them to `src/main/resources/META-INF/sql/seed.sql`.
Note that a user has only one FLT - FakeMe always returns the same FLT for a user, instead of a different one for each SP.

FakeMe's IdP metadata can be obtained from the `api/idp` endpoint on a running FakeMe, e.g. [https://test.pocdev.delegation.org.nz/fakeme/api/idp](https://test.pocdev.delegation.org.nz/fakeme/api/idp)

## SAML Broker
Source code: [https://git.tools.delegation.org.nz/delegation/saml-broker](https://git.tools.delegation.org.nz/delegation/saml-broker)

The SAML broker component.

This is a holdover from earlier proof-of-concept work.
It provides a single SAML integration with RealMe/FakeMe that was originally brokered out to the different test harnesses and demo apps.
At this point there is only a single test harness, but we continued to use the broker to save development time.

A more correct solution would be for each application to be separately integrated with RealMe/FakeMe, and to handle SAML interactions internally.

Given a running vagrant machine, `mvn package -P deploy` will build the app and use ansible to deploy it into vagrant.

Pushing to Gitlab will trigger a Jenkins job (see `Jenkinsfile`) that builds, tests, and deploys into the `pocdev` environment.
The SAML Broker is installed as a systemd unit.

## Test harness
Source code: [https://git.tools.delegation.org.nz/delegation/test-harness](https://git.tools.delegation.org.nz/delegation/test-harness)

Provides a web-page that eases sending requests to the CDS api.

Given a running vagrant machine, `mvn package -P deploy` will build the app and use ansible to deploy it into vagrant.

Pushing to Gitlab will trigger a Jenkins job (see `Jenkinsfile`) that builds, tests, and deploys into the `pocdev` environment.
The Test Harness is installed as a systemd unit.

# Open Issues
I've identified a couple of issues that are unresolved: see [https://git.tools.delegation.org.nz/delegation/cds/issues](https://git.tools.delegation.org.nz/delegation/cds/issues)
