---
coding: utf-8

title: Draft 1: PayID Easy Checkout Protocol
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
The PayID Easy Checkout Protocol can be used to facilitate an end-to-end checkout flow for users transacting
with an e-commerce entity.

The protocol is comprised of two parts:
1) PayID Easy Checkout Discovery
2) Checkout flow and auxiliary e-commerce logic.

While PayID Easy Checkout Discovery and the general user flow must be standardized to create a uniform API across 
all participants the manner by which e-commerce entities and digital wallets handle specific parts of the flow, along
with individual entities' business logic around checkout, is out of scope for this RFC. However, this paper will
recommend best practices for building PayID Easy Checkout flows.

## PayID Easy Checkout Discovery
The primary benefit of an e-commerce entity using the PayID Easy Checkout Protocol is its ability to automatically
redirect customers to their digital wallet to complete the checkout process. If merchants and payment
processors were instead forced to create one-off integrations with each wallet, implementing this functionality would
quickly become an untenable undertaking. On the other side of the protocol, individual wallets will likely want to
maintain control over resource locations on their individual domains.

In order to meet the technical needs of both wallets and e-commerce receivers, PayID Easy Checkout extends [PAYID-DISCOVERY][]
by defining a new Link Relation type in the PayID metadata returned by a PayID Discovery query.

E-commerce receivers who wish to perform Easy Checkout MUST query the PayID Discovery server to obtain a PayID Easy Checkout
URL. Digital wallets who wish to enable Easy Checkout for their users MUST host a PayID Discovery server and MUST
respond to PayID Discovery queries with an Easy Checkout URL.

E-commerce receivers SHOULD implement fallback measures to complete checkout if a user's wallet does not support PayID Easy Checkout.

### Assemble PayID Easy Checkout Discovery URL
The process of assembling a PayID Discovery URL is defined in section 4.1.1 of [PAYID-DISCOVERY][].

### Step 2: Query PayID Discovery URL
A Webfinger query MUST be performed against the PayID Easy Checkout Discovery URL,
as described in section 4.2 of Webfinger.

In response, the WebFinger resource returns a JSON Resource Descriptor (JRD)
as the resource representation to convey information about the requested
PayID.

If the Webfinger endpoint returns a non-200 HTTP response status code, or if the resulting JRD does not contain
a link with a PayID Easy Checkout URL Template, then PayID Easy Checkout is considered to have failed. Clients
SHOULD implement fallback measures to complete checkout in this case.

### Parse PayID Easy Checkout Metadata
If a wallet supports PayID Easy Checkout, the PayID server MUST respond with a HTTP status code 200 and a JSON payload
containing a JSON Resource Descriptor (JRD) as defined in section 5.2 of [PAYID-DISCOVERY][]. Along with any other
PayID Metadata, the PayID server's response MUST contain a Link Relation conforming to the Link Relation definition
in the (TODO: link to section) section of this paper.

For example, a PayID server might respond to a PayID Easy Checkout discovery query with the following payload:

     {
        "subject": "payid:alice$wallet.com",
        "links": [
            {
                "rel" : "https://payid.org/ns/payid-easy-checkout/1.0",
                "template": "https://wallet.com/checkout?amount={amount}&receiverPayId={receiverPayId}&currency={currency}&nextUrl={nextUrl}"
            }
        ]
     }
     
The e-commerce receiver must parse this response, and find a link whose "rel" field has a value of 
"https://payid.org/ns/payid-easy-checkout/1.0". Any link with this relation MUST have a corresponding URI template,
as defined in (TODO: link to template syntax) the Template Syntax section of this document.

### Assembling PayID Easy Checkout URL
The PayID Easy Checkout URL is constructed by applying various values, determined by the receiver, to the PayID Easy Checkout 
URI template found in the previous step.

The PayID Checkout URI template MAY not contain the complete set of variables specified in section (TODO link section) 
of this document.  However, PayID Easy Checkout clients MUST replace each variable instance with a value. It is RECOMMENDED
that PayID Easy Checkout clients have values available for every variable defined, in the case that the PayID Easy Checkout
URI Template contains the complete set of specified variables.

The result of replacing all template variables in the PayID Easy Checkout URI Template with values is a PayID Easy Checkout URL.
Once obtained, PayID Easy Checkout Discovery is considered to have completed successfully.

### Template Syntax
TODO: Update this for PayID Easy Checkout URI Template

This specification defines a simple template syntax for PayID URI
transformation.  A template is a string containing brace-enclosed
("{}") variable names marking the parts of the string that are to be
substituted by the corresponding variable values.

This specification defines a one variable -- "acctpart" -- which
corresponds to the 'acctpart' of a PayID URI as defined in [PAYID-URI][].

When substituting the 'acctpart' value into a URI 'path' as defined by
[RFC3986][], values MUST NOT be percent or otherwise encoded because the
'acctpart' value of a PayID URI always conforms to the character set
allowed by paths in [RFC3986][].

However, before substituting template variables into a URI 'query' part,
values MUST be encoded using UTF-8, and any character other than
unreserved (as defined by [RFC3986]) MUST be percent-encoded per [RFC3986].

Protocols MAY define additional variables and syntax rules, but MUST NOT
change the meaning of the 'acctpart' variable. If a client is unable to
successfully process a template (e.g., unknown variable names, unknown or
incompatible syntax), the JRD SHOULD be ignored.

The template syntax ABNF is as follows:

    uri-char     =  ( reserved / unreserved / pct-encoded )
    var-char     =  ALPHA / DIGIT / "." / "_"
    var-name     =  %x61.63.63.74.70.61.72.74 / ( 1*var-char ) ; "acctpart" or
                                                                  other names
    variable     =  "{" var-name "}"
    PAYID-URI-Template =  *( uri-char / variable )

For example:

    Input:    alice$example.org
    Template: https://example.org/{acctpart}
    Output:   https://example.org/alice

# PayID Easy Checkout JRDs
TODO: define JRD Link

## Recommended Checkout Flow 
TODO: Describe recommended checkout flow

### Payment Detection and Correlation
TODO: Describe recommendations for correlating payments to "invoices"

# IANA Considerations
  ## New Link Relation Types
  This document defines the following Link relation types per [RFC7033][].
  See section 3 for examples of each type of Link.
  
  ### PayID Discovery URI Template
  
    * Relation Type ('rel'): `https://payid.org/ns/payid-easy-checkout-uri/1.0`
    * Media Type: `application/jrd+json`
    * Description: PayID Discovery URI Template, version 1.0



# Acknowledgments
