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
This specification formalizes how a payment recipient, such as a merchant or a non-profit, can automatically navigate 
a user to the user's wallet using only a PayID for the purposes of completing an online payment such as a checkout or
donation flow.

--- middle

# Terminology

This protocol can be referred to as the `PayId Easy Checkout Protocol`. It uses the following terminology:
* PayID client: a client that queries a PayID server using the PayID Protocol as defined in [PAYID-PROTOCOL][].
* PayID server: the endpoint that returns payment account(s) information, which conforms to the PayID Protocol.
* merchant: individual or entity receiving the payment (ie e-commerce merchant, charity).
* customer: individual or entity originating the payment to the `merchant`.
* wallet: the holder of funds for the `sender` (may or may not be custodied).
* Redirect URL: The URL that is the result of the PayID Easy Checkout protocol, and which can be used to redirect a client to a wallet corresponding to a particular PayID.

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [RFC2119][] and [RFC9174][].

# Introduction

The PayID Easy Checkout Protocol is a minimal protocol designed to provide a set of standard APIs and flows, 
which can be used to send payments between two entities in a way that requires:
* minimal effort for the user initiating the transaction.
* no server-side software specific to PayID or its protocols for servicing the transaction.
* only UI-based solutions.

## Motivation

The PayID Easy Checkout Protocol aims to enable a consistent user experience for customers paying for goods
in an e-commerce by standardizing the interaction between merchants and customer wallets.
Given the ability to assign arbitrary metadata to a PayID as defined in [PayID-Discovery][], there is an opportunity
to standardize the set of interactions between merchant and customer, specifically the process by which a merchant
directs a customer to their digital wallet to complete a payment.
We believe this protocol will enable an improved paying experience by reducing the number
of steps a customer must take to complete a transaction and creating a consistent and familiar checkout pattern
for customers.

The second priority of PayID Easy Checkout is to limit the engineering effort needed to implement the protocol. 
Clients wishing to adopt this pattern should only need to implement UI-level changes in order to make the flow function 
as intended, which may aid in expanding overall adoption, further enhancing the protocol's user experience benefits. 

## Design Goals

### Minimal effort for the customer

In order for a customer to checkout using the PayID Easy Checkout protocol, the customer only needs to provide a merchant
with their PayID Easy Checkout enabled PayID.

### No server-side software not already covered by the PayID Protocol

Because the flow of PayID Easy Checkout is predicated on using the PayID Discovery Protocol and then redirecting the 
customer away from the merchant, all of the flow can be instrumented on the front end and doesn't require server-side resources. 

Apart from a PayID Discovery compliant PayID Server, The PayID Easy Checkout Protocol does not require server-side 
software to be run by either the customer or merchant for a payment. The PayID server is capable of providing details 
of where to send the customer via the PayID Discovery Protocol. Assuming the wallet used by the customer has implemented 
support in their UI for the PayID Easy Checkout Protocol, the customer can be redirected to their wallet 
to complete their transaction.

# Example Usage
This section shows a non-normative example of PayID Easy Checkout between a hypothetical merchant and customer. The merchant
accepts payments using the PayID pay$merchant.com, and the customer controls the PayID alice$wallet.com.

## PayID Easy Checkout Initiation
In this example, the customer might place some items in an online shopping cart on the merchant's UI, then choose
to checkout.  The merchant UI would then render a form asking for the customer's PayID, as well as a "Checkout with PayID"
button.  Once the customer inputs their PayID alice$wallet.com and clicks the "Checkout with PayID" button, the merchant
UI begins the PayID Easy Checkout flow.

## PayID Easy Checkout Wallet Discovery
The merchant UI would first assemble the PayID Discovery URL as defined in section 4.1.1 of [PAYID-DISCOVERY][],
yielding the URL `https://wallet.com/.well-known/webfinger?resource=payid%3Aalice%24wallet.com`. 
The merchant UI would then query the assembled URL as defined in section 4.1.2 of [PAYID-DISCOVERY][].

The HTTP request in this example would look like this:
    
    GET /.well-known/webfinger?resource=payid%3Aalice%24wallet.com
    Host: wallet.com
    
If the customer's PayID server has enabled PayID Easy Checkout in their wallet, the server would respond with something like this:
     
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
The merchant UI would parse the PayID Discovery response and iterate over the "links" collection to find the link with 
the Relation Type of "https://payid.org/ns/payid-easy-checkout/1.0". The UI can then do a search and replace on
the "template" field value in the link, replacing all occurrences of the predefined query parameter template names with 
the values they want to send to the customer's wallet. One query parameter of note is the "nextUrl" parameter, which
allows the merchant to supply a redirect or callback URL for the sender's wallet to call once the customer has confirmed
the payment. In this example, the merchant would like to display a "Thank You" page, and replaces `{nextUrl}` 
with `https://merchant.com/thankyou`.

## Redirect Customer to Their Wallet
Once the merchant UI populates the required query parameters in the URL template, the merchant UI redirects the customer to 
the Redirect URL so that the customer can confirm the payment.

## Customer Confirms Payment
After the customer clicks the "Pay with PayID" button the merchant's UI, and the merchant performs the previous steps,
the customer will be redirected to the Redirect URL, which is a front end resource of the wallet. The wallet UI can
read the query parameters from the Redirect URL and render a confirmation page or modal with all of the required fields
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

### Step 1: Assemble PayID Easy Checkout Discovery URL
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

### Step 3: Parse PayID Easy Checkout Metadata
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

### Step 4: Assembling PayID Easy Checkout URL
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

This specification defines a simple template syntax for PayID Easy Checkout URI
transformation.  A template is a string containing brace-enclosed
("{}") variable names marking the parts of the string that are to be
substituted by the corresponding variable values.

This specification defines several variables, MAY or MAY NOT be present in every PayID Easy Checkout URI Template.
These variables are as follows:
    
    'amount': The amount that should be sent by the sender to the receiver
    'receiverPayID': The PayID URI of the receiver
    'currency': The currency that the sender should send (TODO: define currency enum or use rfc)
    'network': The network that the sender should send payment over (TODO: define network enum or use rfc)
    'nextURL': A URL that the sender's wallet can use after completing the payment
    
When substituting values into a URI 'path' or 'query' part as defined by
[RFC3986][], values with characters outside the character set allows by paths or query parameters in [RFC3986][], 
respectively, MUST be percent or otherwise encoded.

Protocols MAY define additional variables and syntax rules, but MUST NOT
change the meaning of the variables specified in this document. If a client is unable to
successfully process a template (e.g., unknown variable names, unknown or
incompatible syntax), the JRD SHOULD be ignored.

The template syntax ABNF is as follows:

    uri-char     =  ( reserved / unreserved / pct-encoded )
    var-char     =  ALPHA / DIGIT / "." / "_"
    var-name     =  %x61.63.63.74.70.61.72.74 / ( 1*var-char )
    variable     =  "{" var-name "}"
    PAYID-URI-Template =  *( uri-char / variable )

For example:

    Input:    alice$wallet.com
              amount = 10
              receiverPayID=pay$merchant.com
              currency=XRP
              network=XRPL
              nextUrl=https://merchant.com/thankyou
    Template: https://wallet.com/checkout?amount={amount}&receiverPayId={receiverPayID}&currency={currency}&network={network}&nextUrl={nextURL}
    Output:   https://wallet.com/checkout?amount=10&receiverPayId=payid%2Apay%24merchant.com&currency=XRP&network=XRPL&nextUrl=https://merchant.com/thankyou

TODO: Should we define acceptable URL template variables for the redirect?

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
