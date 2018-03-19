# Decisions

### 1. Don't expose UMA - wrap it completely in our own api

| PRO | CON |
|--
| agencies don't need to buy a product | agencies can't use off-the-shelf clients |
| simpler abstraction for agencies | we still have to deal with UMA complexity under the hood |
| agencies not locked in to UMA (currently 2.0) | api will be missing some parts of UMA |
| we're not constrained by the UMA spec | we don't have the (security, pricvacy) safeguards built into UMA |

### 2. Don't expose UMA - but expose the tokens (PAT, AAT, etc)

We dismissed this option completely - there's no value in the tokens without UMA.

### 3. Late delegation, i.e. do rendezvous first, then pure UMA

| PRO | CON |
|--
| we can do whatever we need for the rendezvous | have to notify agency app somehow when rendezvous is complete |
| can follow UMA exactly | |
| get the UMA bits that we want | forced to deal with the UMA bits that we don't want |
| rendezvous and delegation cleanly separated | |
| product "safety" | |

#### aside - basic patterns for notifying agency app
1. browser redirect to agency app (agency must implement endpoint)
1. api callback to agency app (agency must implement endpoint)
1. agency app polls CDS (agency app must implement polling)

### 4. Status quo (mixture of UMA and bespoke api)

| PRO | CON |
|--
| best known - it's already implemented | there are non-UMA calls (we replace some UMA interactions with our own) |
| create call gives an immediate response to agency | misusing the spec - product or spec upgrades can break us |
| | weirdness around PAT before rendezvous is finished |
| | conflates rendezvous and delegation<br/><ul><li>adds additional state to delegation</li><li>can't have immutable delegation</li></ul> |

### 5a. Bespoke api

| PRO | CON |
|--
| rendezvous and delegation cleanly separated | |
| complete re-write (we get to do it right this time) | complete re-write (we need to design and implement from scratch) |
| flexibility to drop OpenAM | need to vet our design (protocol, architecture)
| perf NFRs easier to hit | |
| agency doesn't have to worry about UMA integration | |

### 5b. minimal architecture
This option is like 5a above, but we also drop 3rd party products (i.e. OpenAM).
It has the same PRO/CONS as 5a, with these additional points:

| PRO | CON |
|--
| lower TCO | have to re-implement<br/><ul><li>user store</li><li>RealMe integration</li><li>authn/authz</li></ul> |
| simpler to change | |
