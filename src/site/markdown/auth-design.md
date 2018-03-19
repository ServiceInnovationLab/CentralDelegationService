# Authentication Design

There are a number of ways of authenticating APIs, 
but one of the common mechanisms used is forms of the [RFC 2104 HMAC-SHA1 
specification](https://www.ietf.org/rfc/rfc2104.txt).

Under this specification, clients supply their clientId with each request,
together with a Signature that is signed using their SecretKey.

This is then encoded, and passed in the Authorization header.


## Usages

CloudIdentityIntegrator
AWS
NZ Companies Office
... and many more

## Advantages

Choosing what to put in the Signature means you can protect against reply attacks,
since one of the parameters will be the Timestamp.

You don't have to submit passwords across the wire.


## Some Links
http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html

https://github.com/pd/httpie-api-auth