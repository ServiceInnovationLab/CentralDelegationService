# Delegation Project Documentation

The delegations project is composed of the following:

* Java code that exposes an API and interacts with UMA functionality built into Forgerock Access Manager
* Automated build scripts written in Ansible to build a working environment from scratch
* Vagrant and Docker configuration files. The Ansible scripts can be used to create an environment using either of these technologies
* A test harness used for testing the delegations functionality
* An automated test suite based on Selenium.

Other components required to perform end-to-end testing, but not including in the delegations project:

* Forgerock Access Manager (owned by Forgerock)
* RealMe 
