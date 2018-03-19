The delegation system makes use of RealMe's RCMS when separated components
need to talk about the same user.

In particular, the Delegation Service's `createDelegation` operation needs to know
about the principal (entity doing the delegating) and the agent (entity being
    delegated to).

The following assumes that the reader is familiar with RCMS messaging and tokens.
Describing the RCMS is beyond the scope of this document.

When a user is creating a delegation, the user is online but the agent is not.
RCMS provides different types of tokens to handle these situations.

We expect that Service Providers will obtain an Opaque Token for agents at the
time that they register. This token would have a `use` value of
`urn:nzl:govt:ict:stds:authn:deployment:igovt:gls:iCMS:1_0:SAMLV2.0:Delayed`
and a suitably long lifetime (up to a year is allowed).

We expect that Service Providers will obtain an Opaque Token for the principal
at the time that a delegation is created. This token would have a `use` value of
`urn:nzl:govt:ict:stds:authn:deployment:igovt:gls:iCMS:1_0:SAMLV2.0:Authenticated`
and a suitably short lifetime.

We have no expectations as to which `consent` value should be used.
The current implementation uses `urn:oasis:names:tc:SAML:2.0:consent:current-implicit`.
