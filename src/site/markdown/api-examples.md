# API Examples

These use httpie, an application for command-line API testing, similar to cURL.
https://httpie.org/

You can install it with `sudo apt install httpie`

## Required headers
Endpoints are temporarily secured requiring Basic Auth.
`clientId` and `secretKey` should be provided in Authorization header.
Colons (`:`) in clientId must be url-encoded (`%3A`).

Later on this will be migrated to an HMAC-SHA1 signed signature, same as CloudIdentityIntegrator

```text
10:49 $ http --auth dia%3A123:secret:456 http://localhost:8080/api/resources
```

## Creating a resource
```text
10:49 $ http --auth dia%3A123:secret:456 POST http://localhost:8080/api/resources crn=crn:dia:user1
HTTP/1.1 200 OK
Connection: keep-alive
Content-Length: 0
Date: Wed, 28 Jun 2017 22:49:31 GMT
```

## Querying a resource by CRN
```text
10:49 $ http -a dia%3A123:secret:456 http://localhost:8080/api/resources/crn:dia:user1
HTTP/1.1 200 OK
Connection: keep-alive
Content-Length: 81
Content-Type: application/json
Date: Wed, 28 Jun 2017 22:49:38 GMT

{
    "createdAt": 1498690171589,
    "crn": "crn:dia:user1",
    "id": 1,
    "updatedAt": 1498690171589
}
```

## Listing resources
```text
10:49 $ http -a dia%3A123:secret:456 http://localhost:8080/api/resources/
HTTP/1.1 200 OK
Connection: keep-alive
Content-Length: 83
Content-Type: application/json
Date: Wed, 28 Jun 2017 22:49:48 GMT

[
    {
        "createdAt": 1498690171589,
        "crn": "crn:dia:user1",
        "id": 1,
        "updatedAt": 1498690171589
    }
]
```
