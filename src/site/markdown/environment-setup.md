# Environment Setup Documentation

The delegations environment can be deployed to either vagrant or docker.

The vagrant deployment includes a test harness as well as apache, the delegations app and a postgres database. For full end-to-end testing, a Forgerock Access Manager also needs to be deployed, but this requires purchasing a copy first.

The docker deployment only includes the delegations app and the postgres database. This allows low level API testing only. It also requires Forgerock Access Manager, which is not included.


## Deploying to Vagrant

To deploy the environment to vagrant, please perform the following steps.

1. Install Oracle Virtual Box
2. Install Vagrant
3. Ensure the Datacom Centos Basebox (provided) is accessible from your machine (the Vagrantfile needs to be updated with the location of this)
4. Run 'vagrant up' from the same directory that the Vagrantfile is located in (delegations/platform)


## Deploying to Docker

1. Install Docker
2. Run 'docker-compose up' from the same directory that the Dockerfiel is located in (delegations/cds)
3. Refer to README.md in delegations/cds for more information





## Provisioning and Setup Requirements (for vagrant)

To be able to deploy the delegations application, the ansible scripts need to have access to the built artifact or an artifact repository. Originally Jenkins was used by the development team as an artifact repository.


### Deploying the application manually

The ansible scripts will deploy application binaries from a shared **SOURCES** directory (delegations/platform/SOURCES). The ansible scripts check if a required dependency exists there and if so deploys it.

When running vagrant up, ansible will look in this location to deploy the application binaries. To re-deploy after the vm has already been provisioned, run the following from within the vm:

```
ansible-playbook /vagrant/playbook.yml -i /vagrant/staging/vagrant/inventory/
```

Examples of application binaries that can be deployed in this way include the OpenAM war file, swagger-ui.zip, and test-harness-swarm.war (which is retrieved from the last-successful jenkins build).

#### Deploying the application without ansible

If you only want to deploy the application without running the entire Ansible playbook, you can manually deploy the application as follows:

Copy the built binary `/target/cds-swarm.jar` file to `/opt/cds/cds-swarm.jar` in the VM. Then restart the `cds` service:

`sudo systemctl restart cds`

#### Deploying SQL scripts without ansible

Flyway is used to manage patching the database with new SQL scripts. To update the SQL scripts without running the Ansible playbook, do the following:

Place the migration scripts from `../src/main/resources/db/migration/` in `/opt/flyway-{{ flyway_version }}/sql` then run the flyway migration:

`/opt/flyway-{{ flyway_version }}/flyway -placeholderReplacement=false migrate`   


### Deploy the application from your own artifact repository

For an open-source project, it is best if there is an endpoint through which release or snapshot versions of the application artifacts can be downloaded from. [See here for more info](https://blog.sonatype.com/2009/04/what-is-a-repository/). 

There is a generic download artifact role, `download_artifact`, that will download the application artifacts into the shared **SOURCES** directory.   
Example:   

```YAML
roles:
  - { role: download_artifact, job_name: 'Delegations%20-%20CDS', artifact_name: 'cds-swarm.jar' }
```

which will download cds-swarm.jar artifact from jenkins.   

Currently the role has been commented out so that no repository is being referenced. You will need to uncomment it and put in the correct details for your repository. 

If an artifact repository is setup, you can refactor the `download_artifact` role to fetch from a base url instead. For example: */platform/roles/download_artifact/tasks/main.yml*   

```YAML
- name: Download artifact
  get_url:
    url: http://tools.delegation/releases/stable/{{ artifact_name}}
    dest: SOURCES/{{ artifact_name }}
    force: yes
```

instead of

```YAML
- name: Jenkins binary
  get_url:
    url: "{{ jenkins_url }}/job/{{ job_name }}/lastSuccessfulBuild/artifact/target/{{ artifact_name }}"
    dest: SOURCES/{{ artifact_name }}
    force: yes
  when: (not local_stat.stat.exists) or (not remote_fingerprint.content | search(local_stat.stat.md5))
```

## ForgeRock Access Manager

CDS project requires [ForgeRock Access Manager](https://www.forgerock.com/platform/access-management).   
As Access Manager is proprietary software, it cannot be bundled with the open-source release of the cds codebase.   

#### Installing Access Manager for Vagrant Ansible

The ansible **openam.yml** playbook installs OpenAM on top of tomcat.
The openam playbook first installs the certs and configures an apache virtual host for *am-web*. Then,

1. *platform/roles/tomcat*: tomcat is installed and tomcat-keystore added.
2. *platform/roles/openam*: OpenAM is installed and configured


The example below is from *platform/roles/openam/tasks/main.yml*, which downloads Access Manager dependencies. **A private url for downloading Access Manager binaries is required.**

```YAML
- name: OpenAM sources
  get_url:
    url: # TODO: url for downloading openam dependencies. E.g. https://tools.example/sources/{{ item }}
    dest: SOURCES/{{ item }}
  delegate_to: localhost
  become: no
  with_items:
    - AM-{{ openam_version }}.war
    - SSOAdminTools-{{ openam_version }}.zip
    - SSOConfiguratorTools-{{ openam_version }}.zip
```

#### Installing Access Manager for Docker Compose

Docker is in the cds repository as an alternative to vagrant, not as any kind of deployment target. Currently, the docker-compose file installs postgres, swagger-ui, OpenAM and jaeger.

The openam docker compose service requires an installation of an Access Manager war and an [Amster](https://backstage.forgerock.com/docs/amster/5/user-guide/) zip file.

You will need valid urls to download the two binaries. Once you have them, insert them into the openam **Dockerfile** in the cds repository.

Example: */docker/openam/Dockerfile*   

```Dockerfile
ENV OPENAM_VERSION=14.0.0-SNAPSHOT

RUN wget ###_REPLACE_THIS_WITH_VALID_URL_TO_OPENAM_### -O /tmp/openam.war \
  && rm -fr /usr/local/tomcat/webapps/* \
  && unzip -q /tmp/openam.war -d /usr/local/tomcat/webapps/openam \
  && rm /tmp/openam.war

RUN wget ###_REPLACE_THIS_WITH_VALID_URL_TO_OPENAM_AMSTER_### -O /tmp/amster.zip \
  && unzip -q /tmp/amster.zip -d /tmp/amster \
  && rm /tmp/amster.zip
```

Example of a valid url is: `https://tools.delegation.org.nz/sources/Amster-5.0.0.zip`

Running docker-compose up should then work.

## RealMe

The CDS project requires the use of the RealMe RCMS as described in another section.  
Our stub of the RealMe login service and RCMS, called Fakeme was used and deployed to the test vhost. This will not be part of the open-source project and has been removed. Without RealMe integration end to end test cases cannot be completed.

## Deploying code into vagrant

Deploying cds project to a VM requires copying the built binary `/target/cds-swarm.jar` file to `/opt/cds/cds-swarm.jar` in VM. Then restarting the `cds` service.  
You may want to run the database migrations if you have made changes there. To do so, place the migration scripts from `../src/main/resources/db/migration/` to `/opt/flyway-{{ flyway_version }}/sql` then run the flyway migrations `/opt/flyway-{{ flyway_version }}/flyway -placeholderReplacement=false migrate`.  

All of this is written in an ansible playbook inside the **ansible-deploy** directory. Make changes to that as necessary.
