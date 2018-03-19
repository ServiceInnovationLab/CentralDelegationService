# Concept

The goal of the delegations project is to enable a user to access a resource owned by another user, or to allow a user to perform an action or access a service on behalf of another user.

## Actors

There are five main actors in enabling this:

### The organisation

The organisation provides services to the users and is the place that owns the protected resource or data that needs delegation in order to allow access.

### RealMe

The organisation authenticates users using RealMe and uses RCMS tokens to communicate with delegations about users.

### Delegations

Delegations contains an API that allows an organisation to perform various tasks, such as registering a resource and registering permissions for users to access that resource.

### Resource Owner (User 1)

A customer of the organisation that 'owns' a resource hosted by the organisation. They are the one who would like to allow someone else access to view or perform actions on their behalf.

### Delegate (User 2)

A customer of the organisation that needs to gain access to a resource of the Resource Owner to view or perform actions on their behalf.


## Basic flows

Creating a delegation involves the following steps:

1. Orgnaisation creates a resource and delegation type
2. Owner logs in to organisation
3. Owner creates a delegation request through organisation (email is sent to delegate)
4. Delegate logs in to organisation
5. Organisation completes rendezvous
6. Delegate requests access to resource
7. Organisation asks Delegations if access is permissioned
8. Delegate sees resouce


Alternative steps:

1. Orgnaisation creates a resource and delegation type
2. Owner logs in to organisation
3. Owner creates a delegation request at organisation
4. Delegate logs in to organisation
5. Organisation completes delegation creation
6. Delegate requests access to resource
7. Organisation asks Delegations if access is permissioned
8. Delegate sees resouce



