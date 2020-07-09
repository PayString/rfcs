---
coding: utf-8

title: PayID Easy Checkout Protocol
docname: payid-easy-checkout-protocol
category: std

pi: [toc, sortrefs, symrefs, comments]
smart_quotes: off

area: security
author:

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

normative:
    RFC2119:
    RFC2818:
    RFC8446:
    RFC7231:
    RFC7413:
    RFC6265:
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
This specification defines the PayID Easy Checkout Protocol, which can be used to allow two parties to transact money in a formalized way for some means of doing business with one another.

The primary use-case of this protocol is to define how two parties use a PayID in the context of knowing how to send a user to their wallet to send funds to a recipient (for example, a merchant, charity, etc).   

--- middle

# Terminology

This protocol can be referred to as the `PayId Easy Checkout Protocol`. It uses the following terminology:
* PayID client: the endpoint that initiates PayID protocol/sending side of the transaction.
* PayID server: the endpoint that returns payment account(s) information/receiving side of the transaction (custodial or non-custodial wallets, exchanges, etc).
* receiver: individual or entity receiving the transaction.
* sender: individual or entity originating the transaction/owner of the PayID[PayID-URI][].
* wallet: the host of the funds of the `sender`; may or may not be custodied.

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [RFC2119][] and [RFC9174][].

# Introduction

The PayID Easy Checkout Protocol is a minimal protocol designed to provide a set of standard APIs and flows, 
which can be used to transact money between two entities in a way that requires:
* minimal effort for the user initiating the transaction.
* no server-side software specific to PayID or its protocols for servicing the transaction.
* only UI-based solutions.

## Motivation

The PayID Easy Checkout Protocol aims to correct the current absence of a consistent and broadly adopted pattern for 
paying for goods and services via a digital wallet on an e-commerce website.
Given the ability to assign arbitrary metadata to a PayID as defined in [PayID-Discovery][], there is an opportunity
to standardize the set of interactions between receiver and sender, specifically the process by which a receiver
directs a sender to their digital wallet to complete a payment.
We believe this protocol will enable a largely improved user experience in e-commerce transactions by reducing the number
of steps a user must take to complete a transaction, creating a consistent and familiar checkout pattern, and lowering
the barrier to entry use for cryptocurrency novices.

The second priority of PayID Easy Checkout is to limit the engineering effort needed to implement the protocol. 
Clients wishing to adopt this pattern should only need to implement UI-level changes in order to make the flow function 
as intended, which may aid in expanding overall adoption, further enhancing the protocol's user experience benefits. 

## Design Goals

### Minimal effort for the user initiating the transaction

The PayID Easy Checkout protocol requires a small number of points of data from the user:
* Their PayID
* Identifying themselves with the wallet they end up redirected to in order to approve transfer of funds to the receiver.

### No server-side software specific to PayID or its protocols for servicing the transaction

Since the flow of PayID Easy Checkout is predicated on using the PayID Discovery Protocol and then redirecting the 
sender away from the site of the receiver, all of the flow can be instrumented in the browser and doesn't require server-side resources. 

Apart from a PayID Discovery compliant PayID Server, The PayID Easy Checkout Protocol does not require server-side 
software to be run by either the sender or receiver for a transaction. The PayID server is capable of providing details 
of where to send the user via the PayID Discovery Protocol. Assuming the wallet used by the sender has implemented 
support in their UI for the PayID Easy Checkout Protocol, the sender can be redirected to their wallet within the 
browser to complete their transaction.

# Example Usage
This section shows the canonical PayID Easy Checkout flow between a hypothetical merchant and customer. The merchant
accepts payments at the PayID pay$merchant.com, and the customer has the PayID alice$wallet.com.

## PayID Easy Checkout Initiation
In this example, the customer might place some items in their online cart on the merchant's website, then choose
to checkout.  The merchant would then render a form asking for the customer's PayID, as well as a "Checkout with PayID"
button.  Once the user inputs their PayID alice$wallet.com and clicked the "Checkout with PayID" button, the merchant
site begins the PayID Easy Checkout flow.

## PayID Easy Checkout Wallet Discovery
The merchant site would resolve the customer's PayID to a host as defined in [PAYID-URI][], in this case resolving
alice$wallet.com to https://wallet.com. The merchant site would then perform PayID Discovery as defined in
[PAYID-DISCOVERY][] to receive a PayID Easy Checkout JRD like this:
    
    GET /.well-known/webfinger?resource=payid%3Aalice%24wallet.com
    Host: wallet.com
    
If the server has enabled PayID Easy Checkout in their wallet, they would respond with something like this:
     
     HTTP/1.1 200 OK
     Access-Control-Allow-Origin: *
     Content-Type: application/jrd+json

     {
       "subject" : "payid:alice$wallet.com",
       "links" :
       [
         {  
           "rel": "https://payid.org/ns/payid-easy-checkout/1.0",
           "template": "https://wallet.com/checkout?amount={amount}&receiverPayId={receiverPayId}&currency={currency}&nextUrl={nextUrl}"
         }
       ]
     }

## Expand Wallet Discovery URL Template
The merchant would parse the PayID Discovery response and iterate over the "links" collection to find the link with 
the Relation Type of "https://payid.org/ns/payid-easy-checkout/1.0". The site can then do a search and replace on
the "template" field value in the link, replacing all occurrences of the predefined query parameter template names with 
the values they want to send to the customer's wallet. One query parameter of note is the "nextUrl" parameter, which
allows the merchant to supply a redirect or callback URL for the sender's wallet to call once the customer has confirmed
the payment.

## Redirect Customer to Their Wallet
Once the merchant populates the required query parameters in the URL template, they would redirect the customer to 
the resulting URL. In this example, the merchant would like to display a "Thank You" page, and replaces `{nextUrl}` with 
`https://wallet.com/checkout?amount=10&receiverPayId=payid%2Apay%24merchant.com&currency=XRP&nextUrl=https://merchant.com/thankyou`.

## Customer Confirms Payment
After the customer clicks the "Pay with PayID" button the merchant's site, and the merchant performs the previous steps,
the customer will be redirected to their wallet at the URL from the previous step.  The wallet front end can
read the query parameters from the redirect URL and render a confirmation page or modal with all of the required fields
pre-populated.

Once the customer confirms the payment, the wallet would perform a PayID address lookup on the "receiverPayId" query
parameter to get the payment address of the merchant and submit a transaction to the underlying ledger or payment system.
The merchant can then redirect the user back to the URL specified in the "nextUrl" query parameter, which will display
the "Thank You" page of the merchant.

# PayID Easy Checkout Protocol
TODO: define protocol.
## Template Syntax
TODO: define url template params.

# PayID Easy Checkout JRDs
TODO: define JRD Link

# Common Response Status Codes (TODO)
  A PayID server MAY respond to a request using any valid HTTP response code appropriate for the request. The PayID server SHOULD be as specific as possible in its choice of an HTTP specific status code.

## Success Responses
  The following response codes represent successful requests.

### Response Code 200 OK
 A request that does not create a resource returns 200 OK if it is completed successfully and the value of the resource is not null. null. In this case, the response body MUST contain the value of the resource specified in the request URL.

## Client Error Responses
  Error codes in the 4xx range indicate a client error, such as a malformed request.
  In the case that a response body is defined for the error code, the body of the error is as defined for the appropriate format.

# Content Negotiation

# Security Considerations

## Network Attacks
  PayID Easy Checkout protocol's security model assumes the following network attackers:

  * Off-path attacker: An off-path attacker can be anywhere on the network. She can inject and spoof packets but can not observe, or tamper with the legitimate traffic between the PayID Easy Checkout client and the server.

  * On-path attacker: An on-path attacker can eavesdrop, inject, spoof and replay packets, but can not drop, delay or tamper with the legitimate traffic.

  * In-path or Man-in-the-middle (MiTM) attacker: An MiTM is the most powerful network attacker. An MiTM has full access to the communication path between the PayID Easy Checkout client and the server. She can observe, modify, delay and drop network packets.

  Additionally we assume that the attacker has enough resources to mount an attack but can not break the security guarantees provided by the cryptographic primitives of the underlying secure transport.

  The basic PayID Easy Checkout protocol runs over HTTPS and thus relies on the security of the underlying transport. Implementations utilizing TLS 1.3 benefit from the TLS security profile defined in [RFC8446][] against all the above network attackers.

### Denial-of-Service (DoS) attacks
  As such, cryptography can not defend against DoS attacks because any attacker can stop/interrupt the PayID Easy Checkout protocol by:
  * Dropping network packets,
  * Exhaustion of resources either at the network level or at PayID Easy Checkout client and/or server.

  The PayID Easy Checkout servers are recommended to follow general best network configuration practices to defend against such attacks [RFC4732][].

## Information Integrity
  The HTTPS connection provides transport security for the interaction between PayID Easy Checkout client and server but does not provide the response integrity of the data provided by PayID Easy Checkout server. A PayID Easy Checkout client has no way of knowing if data provided in the payment account information resource has been manipulated at the PayID Easy Checkout server, either due to malicious behavior on the part of the PayID server administrator or as a result of being compromised by an attacker. As with any information service available on the Internet, PayID Easy Checkout clients should be wary of the information received from untrusted sources.  

# Privacy Considerations
  The PayID Easy Checkout client and server should be aware that placing information on the Internet means that any one can access that information. While PayID Easy Checkout protocol is an extremely useful tool to discovering payment account(s) information corresponding to a human-rememberable PayID URI, PayID owners should also understand the associated privacy risks. The easy access to payment account information via PayID protocol was a design goal of the protocol, not a limitation.  

## Access Control (TODO)

## On the Wire
  PayID Easy Checkout protocol over HTTPS encrypts the traffic and requires mutual authentication of the PayID client and the PayID server. This mitigates both passive surveillance [RFC7258][] and the active attacks that attempt to divert PayID Easy Checkout protocol queries to rogue servers.

  Additionally, the use of the HTTPS default port 443 and the ability
  to mix PayID protocol traffic with other HTTPS traffic on the same connection can deter unprivileged on-path devices from interfering with PayID Easy Checkout operations and make PayID Easy Checkout traffic analysis more difficult.


# IANA Considerations
  ## New Link Relation Types
  This document defines the following Link relation types per [RFC7033][].
  See section 3 for examples of each type of Link.
  
  ### PayID Discovery URI Template
  
    * Relation Type ('rel'): `https://payid.org/ns/payid-easy-checkout-uri/1.0`
    * Media Type: `application/jrd+json`
    * Description: PayID Discovery URI Template, version 1.0



# Acknowledgments
