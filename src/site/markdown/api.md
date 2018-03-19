# Api Documentation

CDS offers a REST API for the purpose of creating and managing Delegations, and the various Domain Entities associated with them




## CRNs
CRNs, or Canonical Resource Names are an agency agnostic scheme for representing resources and other items which are to be shared.  They are external identifiers used by the CDS api.

They look like:
```
crn:dia:test:123456789012:templates/passport-1
crn:dia:test::delegation_types/read-only
```
and follow a fixed segment scheme.
```
{scheme}:{domain}:{service}:{account}:{resourcetype}/{path}
```

This means the front-half is meaningful, but the back half, from the account onwards, only has meaning to the client application.

### Rules

1. schema is always `crn`
2. domain is required, typically it is the privacy domain.  Sometimes might be the entire agency.
3. service and account may be omitted.  Resources that are global to a particular service would not have to belong to an account, although it might make sense to preserve a system or global account.
4. account must always be 12 digits.  Its an opaque identifier, and does not require any meaning outside its domain/service combination.  It is not a universal identifier.
5. even if segments are skipped, the colons `:` MUST remain.
6. CDS will validate CRNs and reject if not valid.
7. System generated CRNs, currently Delegations and Rendezvous, use a UUID as the path, so they look like `crn:dia:test::delegation/43046b2b-839a-4aba-a50e-c70212d88fc4`.  This ensures they are not discoverable, but keeps the meaning of which system and privacy domain they are connected to.

## Data Domain

The CDS Data Domain is made up of the following models

__DelegationRequest__

Creating new Delegations is usually done by first creating a DelegationRequest.  That request will specify the Resource you are wanting to act on, and the DelegationType.  Both the Resource and DelegationTypes are referred to externally by their CRN.  A DelegationRequest will trigger the creation of a Rendezvous and the associated Delegation.

__Rendezvous__

A Rendezvous manages the process of getting Consent from both parties to create a Delegation over a Resource.  Typically this is done via email.  

__Delegation__

An instance of a particular DelegationType (like a certain read-only permission), against a particular Resource.  Each Delegation links to 2 Users: the Owner (via the Resource), and the Delegate.  A Delegation may link to some Policies.  

__Resource__

A Resource is a logical item somewhere that is registered against CDS.  It is referred to by its CRN.  A Resource always has an Owner.  Any Client application can check whether a particular User has Access

__DelegationType__

A DelegationType represents a class of Delegations, such as "Read-Only Access to Medical Records".  Every Delegation must belong to a DelegationType that belongs to that Client.  The DelegationType can specify the email template that will be used for Rendezvous

__Template__

A Template provides the content for an outgoing communication, typically a request for a Delegation or Rendezvous.  It contains a Thymeleaf html section which is allows for formatting of rich emails.  Each Client must have a link to a default Template, but DelegationTypes can supercede that with a more custom crafted message.  

__Policy__

Policies are a collection of statements that expand on the language contained in a Delegation.  While a Delegation might be named 'medical-record-view-only', it may further contain statements such as

    Effect: Allow
    Action: patientdata/read
    Resource: crn:moh:patientdata:*:health/bloodtests/*

    Effect: Allow
    Action: patientdata/list
    Resource: crn:moh:patientdata:*:health/bloodtests/

    Effect: Deny
    Action: patientdata/update
    Resource: crn:moh:patientdata:*:health/bloodtests/*

    Effect: LimitHours
    Action: begin/0900 end/1700


Such policies are not enforced by CDS, but the detail of them is made available to the Client application, which can then enforce them via their internal logic.  This Policy construct can aid in consistent and effective Permissioning and Limits across various Client application.

Policies can also be defined against an individual Delegation, referred to as a Local Policy, or against the parent DelegationType, referred to as a Global Policy

__Client__

A Client Application is a system that communicates with CDS.  Each Client must be registered, and will have its own secure credentials.  A Client must specify both its `domain` eg. DIA, and its `service`, eg. Passports.
CDS scopes all data apart from Users and Clients themselves on the basis of this domain/service boundary combination, so a "dia/passports" client will not see the same data as a "dia/births" client when asking for a list of Resources.

__User__

A User is an actual Human person who authorizes sharing their right to act on a Resource, the core of Delegations.  Client applications communicate with CDS about a User via the use of RealMe RCMS tokens, so that their underlying identity is not compromised.

__Uma__

Once a Delegation is `complete` within the CDS, that is, both parties have consented, then CDS takes care of leveraging UMA via ForgeRock Access Management (AM).  The Resources are registered and protected.

## API Examples

Below is the full sequence of calls supported and expected to be used.  You should be able to copy them and one-by-one call the API.

### A note on calls
All calls need to include HttpBasic authentication.  This will transition to an HMAC SHA256 scheme in future, but is being kept intentionally simple for now.

It is required that all calls should set an Accept value of "application/json" when calling the API.

It is also recommended that all calls should set a ContentType value of "application/json" when sending data.  


```json
curl -H "Authorization: Basic YWNjZXNzMTIzOnNlY3JldDQ1Ng==" https://cds.dia.govt.nz/api/resources
```

```json
curl -H "Authorization: Basic YWNjZXNzMTIzOnNlY3JldDQ1Ng==" -H "Content-Type: application/json" -X POST https://cds.dia.govt.nz/api/resources -d '
{
  "crn":"crn:dia:test:123456789012:templates/passport-1"
}'

```

All examples below are assuming that `Authorization`, `Accept` and `Content-Type` headers are being supplied, but for the sake of brevity they will be omitted.

### Create a Client (Admin only)

Note: When creating a client, only the domain and service are supplied.  The CRN will be constructed from that, and the access and secret keys will be created.  For now, the access and secret keys can be used as the username and password respectively for HTTP basic authentication.

__Request__

```json
curl -X POST https://cds.dia.govt.nz/api/clients -d '{
  "privacy_domain":"dia",
  "service":"passports"
}'

```

__Response__

```json
{   
    "crn":"crn:dia:passports",
    "privacy_domain":"dia",
    "service":"passports",
    "access_key":"somekey",
    "secret_key":"somesecret",
    "admin":false
}

```

### Create a Delegation Type

__Request__

```json
curl -X POST https://cds.dia.govt.nz/api/delegation_types -d
'{
  "crn":"crn:dia:test::delegation_types/limited-update",
  "name":"limited-update"
}'

```

__Response__

```shell
HTTP/1.1 201 CREATED
Location: https://cds.dia.govt.nz/api/delegation_types/crn:dia:test::delegation_types/limited-update

```

### Create a Template

__Request__

```json
curl -X POST https://cds.dia.govt.nz/api/templates -d
'{
    "crn":"crn:dia:test::templates/general-invite",
    "name":"general-invite",
    "version": 1
}'

```

__Response__

```shell
HTTP/1.1 201 CREATED
Location: https://cds.dia.govt.nz/api/templates/crn:dia:test::templates/general-invite/1

```

### Update Delegation Type to use a different Template or version of

Note: This operation allows for a partial update via the PUT verb to change the template name and version.

__Request__

```json
curl -X PUT https://cds.dia.govt.nz/api/delegation_types/crn:dia:test::delegation_types/limited-update -d
'{
    "template_name":"general-invite",
    "template_version":"2"
}'

```

__Response__

```shell
HTTP/1.1 200 OK

```

### Create a Resource

__Request__

```json
curl -X POST https://cds.dia.govt.nz/api/resources -d
'{
  "crn":"crn:dia:test:123456789012:templates/passport-1"
}'

```

__Response__

```shell
HTTP/1.1 201 CREATED
Location: https://cds.dia.govt.nz/api/resources/crn:dia:test:123456789012:templates/passport-1

```

### Create a DelegationRequest (fully consented)

__Request__

```json
curl -X POST https://cds.dia.govt.nz/api/delegation_requests -d
'{
    "resource_crn":"crn:dia:test:123456789012:templates/passport-1",
    "delegation_type_crn":"crn:dia:test::delegation_types/limited-update",
    "owner_rcms":"{rcms_token}",
    "delegate_rcms":"{rcms_token}",
    "owner_consent": true,
    "delegate_consent": true
 }'

```

__Response__

```json
HTTP/1.1 200 OK
{   
    "delegation_ready":true,
    "access_resource_uri":"https://cds.dia.govt.nz/api/resources/crn:dia:test:123456789012:templates/passport-1/access/{rcms_token}"
}
```

### Create a DelegationRequest (consents outstanding)

__Request__

```json
curl -X POST https://cds.dia.govt.nz/api/delegation_requests -d
'{
    "resource_crn":"crn:dia:test:123456789012:templates/passport-1",
    "delegation_type_crn":"crn:dia:test::delegation_types/limited-update",
    "owner_rcms":"{rcms_token}",
    "delegate_rcms":"{rcms_token}"
 }'

```

__Response__ that needs Owner or Delegate to Accept

```json
HTTP/1.1 202 ACCEPTED
{   
    "delegation_ready":false,
    "complete_owner_rendezvous_uri":"https://cds.dia.govt.nz/api/rendezvous/d86b1ddc-abd5-4b34-9804-33653fe4ad7f/accept/{rcms_token}",
    "complete_owner_rendezvous_code": "d86b1ddc-abd5-4b34-9804-33653fe4ad7f"
}
```

### Give Consent to a Rendezvous

__Request__

```shell
curl https://cds.dia.govt.nz/api/rendezvous/d86b1ddc-abd5-4b34-9804-33653fe4ad7f/accept/{rcms_token}

```

__Response__

```json
HTTP/1.1 201 CREATED
{   
    "delegation_ready":true,
    "access_resource_uri":"https://cds.dia.govt.nz/api/resources/crn:dia:test:123456789012:templates/passport-1/access/{rcms_token}"
}
```

### Check access to a Resource


__Request__

```shell
curl https://cds.dia.govt.nz/api/resources/crn:dia:test:123456789012:templates/passport-1/access/{rcms_token}

```

__Response__ (Authorized)

```json
HTTP/1.1 200 OK

```

__Response__ (Denied)

```json
HTTP/1.1 403 NOT ALLOWED

```
