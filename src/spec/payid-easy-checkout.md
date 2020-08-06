---
coding: utf-8

title: Draft 1 - PayID Easy Checkout Protocol
docname: payid-easy-checkout-protocol
category: std

pi: [toc, sortrefs, symrefs, comments]
smart_quotes: off

area: security
author:
  -
    ins: N. Kramer
    name: Noah Kramer
    org: Ripple
    street: 315 Montgomery Street
    city: San Francisco
    region: CA
    code: 94104
    country: US
    phone: -----------------
    email: nkramer@ripple.com
    uri: https://www.ripple.com
  -
    ins: D. Fuelling
    name: David Fuelling
    org: Ripple
    street: 315 Montgomery Street
    city: San Francisco
    region: CA
    code: 94104
    country: US
    phone: -----------------
    email: fuelling@ripple.com
    uri: https://www.ripple.com
  -
    ins: I. Simpson
    name: Ian Simpson
    org: Ripple
    street: 315 Montgomery Street
    city: San Francisco
    region: CA
    code: 94104
    country: US
    phone: -----------------
    email: isimpson@ripple.com
    uri: https://www.ripple.com

normative:
    RFC2119:
    RFC2818:
    RFC3986:
    RFC6265:
    RFC7033:
    RFC7231:
    RFC7413:
    RFC6570:
    RFC8446:
    PAYID-PROTOCOL:
      title: "PayID Protocol"
      author:
         ins: A. Malhotra
         fullname: Aanchal Malhotra
         ins: D. Schwartz
         fullname: David Schwartz
    PAYID-URI:
      title: "The 'payid' URI Scheme"
      target: https://tbd.example.com/
      author:
        ins: D. Fuelling
        fullname: David Fuelling
    PAYID-DISCOVERY:
      title: "PayID Discovery"
      author:
        ins: D. Fuelling
        fullname: David Fuelling

informative:
    RFC5988:

--- note_Feedback

This specification is a draft proposal, and is part of the [PayID Protocol](https://payid.org/) initiative. Feedback related to this document should be sent in the form of a Github issue at: https://github.com/payid-org/rfcs/issues.
 
--- abstract
This specification formalizes how a payment recipient, such as a merchant or a non-profit, can automatically 
initiate a payment from a sender using only the sender's PayID. 

--- middle

# Terminology

This protocol can be referred to as the `PayID Easy Checkout Protocol`. It uses the following terminology:
   
* PayID Easy Checkout Client: A client that assembles a PayID Easy Checkout URL using information obtained from PayID Discovery Server via [PAYID-DISCOVERY][].
* PayID Discovery Server: An endpoint that returns a PayID Discovery JRD conforming to [PAYID-DISCOVERY][].
* Recipient: An individual or entity receiving a payment (e.g., e-commerce merchant, charity).
* Sender: An individual or entity originating a payment to a `recipient`.
* Wallet: A device or application that holds funds (may be a non-custodial wallet).
* PayID Easy Checkout URL: A URL that is the result of this protocol; can be used to redirect a client to a wallet corresponding to a particular PayID as defined in [PAYID-URI][].

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [RFC2119][] and [RFC9174][].

# Introduction

The PayID Easy Checkout Protocol allows a recipient (e.g., an online merchant or a charity) to
request a payment from a sender using only the sender's [PayID][PAYID-URI]. Implementations
of this protocol require little to no server-side engineering effort, while creating a seamless and uniform
user experience for senders.

The main focus of the protocol is on PayID Easy Checkout Discovery, which defines how a PayID Easy Checkout Client can 
retrieve a PayID Easy Checkout URL and use it to initiate 
a payment to the merchant. 

Though the [appendix](#appendix) of this specification provides an example usage of
this protocol using Web Redirects, supplemental RFCs are needed to define any different ways in which a PayID
client can utilize a PayID Easy Checkout URL.

# PayID Easy Checkout Protocol
The PayID Easy Checkout Protocol can be used to initiate an end-to-end checkout flow between a payment recipient, such
as an online merchant, and a sender.

The protocol is comprised of two parts:

1. PayID Easy Checkout Discovery
2. PayID Easy Checkout URL Assembly

## PayID Easy Checkout Discovery
PayID Easy Checkout Discovery extends [PAYID-DISCOVERY][] by defining a new link relation in the PayID metadata JRD
returned by a PayID Discovery query. This link relation, defined in the [JRD](#payid-easy-checkout-jrds) section
of this specification, includes the URL on the sender's wallet that can
be used to initiate a payment.

Recipients who wish to initiate an Easy Checkout flow MUST first query the sender's PayID Discovery Server to 
obtain a PayID Easy Checkout URL. Therefore, PayID Discovery Servers that wish to enable PayID Easy
Checkout MUST include an Easy Checkout JRD Link 
in all PayID Easy Checkout Discovery responses.

Recipients SHOULD implement fallback measures to complete a checkout flow if a sender's wallet does not support PayID Easy Checkout.

The following steps describe how a PayID Easy Checkout Client can query a PayID Discovery Server to obtain a PayID Easy Checkout URL.

### Step 1: Assemble PayID Easy Checkout Discovery URL
The process of assembling a PayID Discovery URL is defined in section 4.1.1 of [PAYID-DISCOVERY][].

### Step 2: Query PayID Easy Checkout Discovery URL
The process of querying the PayID Discovery URL is defined in section 4.1.2 of [PAYID-DISCOVERY][].

Clients SHOULD implement fallback measures to complete checkout if the PayID Easy Checkout Discovery query fails.

### Step 3: Parse PayID Easy Checkout Metadata
If PayID Easy Checkout is supported, a PayID Discovery server MUST respond to discovery requests with an HTTP status code of `200` and a JSON payload
containing a JRD with an Easy Checkout link relation.

For example, a PayID Discovery Server might respond to a PayID Discovery query with the following payload:

     {
        "subject": "payid:alice$wallet.com",
        "links": [
            {
                "rel" : "https://payid.org/ns/payid-easy-checkout-uri/1.0",
                "href": "https://wallet.com/checkout"
            }
        ]
     }
     
A PayID Easy Checkout client MUST parse this response to find the PayID Easy Checkout Link. 
If the JRD returned from the PayID Discovery query does not contain a 
PayID Easy Checkout Link in its 'links' collection, PayID Easy Checkout is considered to have failed.

However, if a PayID Easy Checkout URL can been obtained from the PayID Easy Checkout Link, 
PayID Easy Checkout Discovery is considered to be complete. 

## PayID Easy Checkout URL Assembly
A PayID Easy Checkout URL represents the resource on a wallet that can
be used by a sender to complete a payment. However, before directing a sender to their wallet, the recipient
MUST append all of the query parameters defined in the [following section](#payid-easy-checkout-url-query-parameters).

Once a PayID Easy Checkout URL is assembled, PayID Easy Checkout is considered to be complete.

### PayID Easy Checkout URL Query Parameters
This specification defines several query parameter names and corresponding datatypes which MUST be added to the
PayID Easy Checkout URL before redirecting a sender to their wallet. The PayID Easy Checkout URL SHOULD be parsed 
by the wallet in order to retrieve any values set by the recipient. It is RECOMMENDED that wallets use these 
values to pre-populate a payment transaction.
    
| Name           | Type             | Description                                                          |
|----------------|------------------|----------------------------------------------------------------------|
| amount         | integer          | The amount that should be sent by the sender to the recipient.        |
| receiverPayId  | string           | The [PAYID-URI][] of the receiver.                           |
| assetCode      | string           | The currency code that denominates the amount as defined in [PAYID-PROTOCOL][].|
| assetScale     | short            | Defines how many units make up one regular unit of the assetCode.     |
| paymentNetwork | string           | The payment network, as defined in [PAYID-PROTOCOL][], that the sender should use to send a payment. |
| nextUrl        | HTTP Url string  | A URL that the sender's wallet can navigate a sender to after the sender completes a payment.  |
|----------------|------------------|----------------------------------------------------------------------|
    
When adding values into a URI 'query' part as defined by
[RFC3986][], values with characters outside the character set allowed by query parameters in [RFC3986][]
MUST be percent or otherwise encoded.

Protocols MAY define additional query parameter names and syntax rules, but MUST NOT
change the meaning of the variables specified in this document.

For example:

    Input:    alice$wallet.com
              amount=10
              receiverPayId=pay$merchant.com
              assetCode=XRP
              assetScale=6
              network=XRPL
              nextUrl=https://merchant.com/thankyou
    PayID Easy Checkout URL: https://wallet.com/checkout
    Output:   https://wallet.com/checkout?amount=100000&receiverPayId=payid%2Apay%24merchant.com&assetCode=XRP&assetScale=6&paymentNetwork=XRPL&nextUrl=https%3A%2F%2Fmerchant.com%2Fthankyou

# PayID Easy Checkout JRDs
This section defines the PayID Easy Checkout Link Relation, which conforms to section 4.4 of
[Webfinger][RFC7033].

The Link MUST include the Link Relation Type defined in [PayID Easy Checkout URL](#iana-considerations) in the object's 'rel' field.
The Link MUST also include a PayID Easy Checkout URL in the 'href' field of the link.

    * 'rel': `https://payid.org/ns/payid-easy-checkout-uri/1.0`
    * 'href': {A PayID Easy Checkout URL}

The following is an example of a PayID Easy Checkout Link:

    {
        "rel": "https://payid.org/ns/payid-easy-checkout-uri/1.0",
        "href": "https://wallet.com/checkout"
    }

# Security Considerations
Various security considerations should be taken into account for PayID
Easy Checkout.

The security considerations for PayID Easy Checkout Discovery are discussed in 
section 6 of [PAYID-DISCOVERY][].

## PayID Easy Checkout Redirection URI Manipulation
When a sender uses the resource located at the PayID Easy Checkout URL, an attacker could manipulate
the data encoded in the URL to trick the sender into sending a payment to a different PayID than was originally
requested, or manipulate other parts of PayID Easy Checkout data to trick the sender. 

Additionally, if an attacker gains access to the merchant application, an attacker could replace the PayID Easy Checkout URL
to execute a phishing or other attack.


## Access Control
As with all web resources, access to the PayID Discovery resource could
require authentication. See section 6 of [RFC7033][] for Access Control
considerations.

Furthermore, it is RECOMMENDED that PayID Discovery Servers only expose PayID Easy Checkout URLs
which resolve to a protected resource (e.g., by logging into a wallet) before allowing access.

# IANA Considerations
  ## New Link Relation Types
  This document defines the following Link relation type per [RFC7033][].
  See section 3 for examples of each type of Link.
  
  ### PayID Easy Checkout URL
  
    * Relation Type ('rel'): `https://payid.org/ns/payid-easy-checkout-uri/1.0`
    * Media Type: `application/jrd+json`
    * Description: PayID Easy Checkout URL, version 1.0

# Acknowledgments

# Appendix

## Motivation
The PayID Easy Checkout Protocol aims to enable a consistent user experience for senders paying for goods
or services by standardizing the interaction between merchants/non-profits and customer/donor wallets.
Given the ability to assign arbitrary metadata to a PayID as defined in [PayID-Discovery][], there is an opportunity
to standardize the set of interactions between merchant and sender, specifically the process by which a merchant
directs a sender to their digital wallet to complete a payment.
The intention of this protocol is to enable an improved paying experience by reducing the number
of steps a sender must take to complete a transaction.

PayID Easy Checkout also limits the engineering effort needed to implement the protocol. 
Clients wishing to adopt this pattern should only need to implement UI-level changes in order to make the flow function 
as intended, which may aid in expanding overall adoption, further enhancing the protocol's user experience benefits. 

### Design Goals

#### Minimal effort for the Sender

In order for a sender to checkout using the PayID Easy Checkout protocol, the sender only needs to provide a merchant
with their PayID Easy Checkout enabled PayID.

#### No New Server-Side Software

Apart from a PayID Discovery compliant PayID Discovery Server, The PayID Easy Checkout Protocol does not require server-side 
software to be run by either the sender or merchant for a payment. The PayID Discovery Server is capable of providing details 
of where to send the sender via the PayID Discovery Protocol. Assuming the wallet used by the sender has implemented 
support in their UI for the PayID Easy Checkout Protocol, the sender can be redirected to their wallet 
to complete their transaction.

## Example Usage
This section shows a non-normative example of PayID Easy Checkout between a hypothetical merchant (recipient) and sender. The merchant
accepts payments using the PayID pay$merchant.example.com, and the sender controls the PayID alice$wallet.example.com.

### PayID Easy Checkout Initiation
In this example, the sender might place some items in an online shopping cart on the merchant's web-site, then choose
to checkout.  The merchant would then render a form asking for the sender's PayID, as well as a "Checkout with PayID"
button.  Once the sender inputs their PayID `alice$wallet.example.com` and clicks the "Checkout with PayID" button, the merchant
begins the PayID Easy Checkout flow.

### PayID Easy Checkout Wallet Discovery
The merchant UI would first assemble the PayID Easy Checkout URL as defined in [PayID Easy Checkout Discovery](#payid-easy-checkout-discovery),
yielding the URL `https://wallet.example.com/.well-known/webfinger?resource=payid%3Aalice%24wallet.example.com`. 
The merchant UI would then [query the assembled URL](#step-2-query-payid-easy-checkout-discovery-url).

The HTTP request in this example would look like this:
    
    GET /.well-known/webfinger?resource=payid%3Aalice%24wallet.example.com
    Host: wallet.example.com
    
If the sender's PayID Discovery Server has enabled PayID Easy Checkout in their wallet, the server would respond with something like this:
     
     HTTP/1.1 200 OK
     Access-Control-Allow-Origin: *
     Content-Type: application/jrd+json

     {
       "subject" : "payid:alice$wallet.example.com",
       "links" :
       [
         {  
           "rel": "https://payid.org/ns/payid-easy-checkout-uri/1.0",
           "template": "https://wallet.example.com/checkout"
         }
       ]
     }

### Assemble PayID Easy Checkout URL with Query Parameters
The merchant UI would parse the PayID Discovery response and iterate over the 'links' collection to find the link with 
the Relation Type of "https://payid.org/ns/payid-easy-checkout-uri/1.0". The merchant UI would then add all of the query
parameters defined in [PayID Easy Checkout URL Query Parameters](#payid-easy-checkout-url-query-parameters) to the URL included in the JRD Link. 
One query parameter of note is the "nextUrl" parameter, which allows the merchant to supply a redirect or callback URL 
for the sender's wallet to call once the sender has confirmed the payment. In this example, the merchant would like 
to display a "Thank You" page, and replaces `{nextUrl}` with `https://merchant.com/thankyou`.

#### Correlating a Payment to an Invoice
Merchants and non-profits will often need to correlate discrete layer-1 payments to an invoice or transaction entity
in the merchants' native systems. The merchant in this example may have an invoice tracking system, on which an invoice
gets created for the goods that the sender is buying, for example an invoice with a unique identifier of `1045464`. A common practice for correlating
layer-1 payments to a specific transaction or invoice is to accept payments on a different layer-1 address for each invoice
so that the merchant can listen for payments into that address and correlate the payment to the invoice.  However, because
the PayID Easy Checkout URL only provides the receiver's PayID, there is currently no way to associate the address that
is given to the sender to the invoice.

In order to accomplish this, a merchant could provide a unique PayID associated with an invoice, for example a PayID 
containing the invoice identifier, for each PayID Easy Checkout transaction. In this example, the merchant would first associate a payment address with the
invoice ID, and would then redirect the sender to their wallet with the `receiverPayId` query parameter set to `pay-1045464$merchant.com`.
When the merchant PayID Server receives a query for the address associated with that PayID, they could return the previously
stored payment address. When the merchant receives a payment to that address, they can then associate the layer 1 payment
with the invoice.

### Redirect Sender to Their Wallet
Once the merchant UI populates the required query parameters in the URL template, the merchant UI redirects the sender to 
the Redirect URL so that the sender can confirm the payment.

### Sender Confirms Payment
After the sender clicks the "Pay with PayID" button the merchant's UI, and the merchant performs the previous steps,
the sender will be redirected to the Redirect URL, which is a front end resource of the wallet. The wallet UI can
read the query parameters from the Redirect URL and render a confirmation page or modal with all of the required fields
pre-populated.

Once the sender confirms the payment, the wallet would perform a PayID address lookup on the "receiverPayId" query
parameter to get the payment address of the merchant and submit a transaction to the underlying ledger or payment system.
The merchant can then redirect the user back to the URL specified in the "nextUrl" query parameter, which will display
the "Thank You" page of the merchant.
