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
    RFC6265:
    RFC7231:
    RFC7413:
    RFC6570:
    RFC8446:
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
initiate a payment from a payer using only the payer's PayID. 

--- middle

# Terminology

This protocol can be referred to as the `PayId Easy Checkout Protocol`. It uses the following terminology:
   
* PayID client: a client that queries a PayID server using the PayID Protocol as defined in [PAYID-PROTOCOL][].
* PayID server: the endpoint that returns payment account(s) information, which conforms to the PayID Protocol.
* Merchant: Individual or entity receiving a payment (e.g., e-commerce merchant, charity).
* Payer: Individual or entity originating a payment to a `merchant`.
* Wallet: A device or application that holds funds (may be a non-custodial wallet).
* PayID Easy Checkout URI Template: The URI Template that is the result of PayID Easy Checkout Discovery 
* PayID Easy Checkout URL: The URL that is the result of the PayID Easy Checkout protocol; can be used to redirect a client to a wallet corresponding to a particular PayID.

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [RFC2119][] and [RFC9174][].

# Introduction

The PayID Easy Checkout Protocol is a minimal protocol that allows an online merchant to
request a payment from a payer using only the payer's PayID. Implementations
of the protocol should require little to no server-side engineering efforts, while creating an improved and uniform
user experience for payers.

The main focus of the Protocol is on PayID Easy Checkout Discovery, which defines how a PayID client can use a PayID
to retrieve a PayID Easy Checkout URI Template which, when expanded, constitutes a PayID Easy Checkout URL 
representing a resource that the payer's digital wallet can use to initiate a payment to the merchant. 

Though section (TODO: link to appendix example usage section) of this specification provides an example usage of a 
PayID Easy Checkout URL using Web Redirects, supplemental RFCs are needed to define the different ways in which a PayID
client can utilize a PayID Easy Checkout URL.

# PayID Easy Checkout Protocol
The PayID Easy Checkout Protocol can be used to facilitate an end-to-end checkout flow between a recipient client, such
as an online merchant UI, and a sending client, such as a wallet.

The protocol is comprised of two parts:

1. PayID Easy Checkout Discovery
2. PayID Easy Checkout URL Assembly

The result of the protocol is a URL, which can be used by sending clients to complete a payment.

## PayID Easy Checkout Discovery
PayID Easy Checkout extends [PAYID-DISCOVERY][] by defining a new link in the PayID metadata JRD
returned by a PayID Discovery query. This link, defined in section (TODO: link to jrd section) 
of this specification, includes the PayID Easy Checkout URI Template representing a resource on the wallet which can
be used to complete a payment.

E-commerce merchants who wish to initiate an Easy Checkout flow MUST query the sender's PayID Discovery server to 
obtain a PayID Easy Checkout URI Template. Digital wallets and PayID server operators who wish to enable PayID Easy
Checkout MUST include a JRD Link conforming to the definition in section (TODO: link to jrd section) of this paper 
in all PayID Easy Checkout Discovery responses.

E-commerce merchants SHOULD implement fallback measures to complete a checkout flow if a user's wallet does not support PayID Easy Checkout.

The following steps describe how a PayID client can query a PayID server to obtain a PayID Easy Checkout URI Template. 

### Step 1: Assemble PayID Easy Checkout Discovery URL
The process of assembling a PayID Discovery URL is defined in section 4.1.1 of [PAYID-DISCOVERY][], and is
the same as for PayID Easy Checkout Discovery.

### Step 2: Query PayID Easy Checkout Discovery URL
Querying the PayID Discovery URL is defined in section 4.1.2 of [PAYID-DISCOVERY][], and is performed
in the same way for the PayID Easy Checkout Discovery URL.

Clients SHOULD implement fallback measures to complete checkout if the PayID Easy Checkout Discovery query fails.

### Step 3: Parse PayID Easy Checkout Metadata
If a wallet supports PayID Easy Checkout, the PayID server MUST respond with a HTTP status code 200 and a JSON payload
containing a JSON Resource Descriptor (JRD) as defined in section (TODO: link to jrd section) of this document. 
The JRD MUST contain a link conforming to the link definition in section (TODO: link to section) of this paper.

For example, a PayID server might respond to a PayID Easy Checkout Discovery query with the following payload:

     {
        "subject": "payid:alice$wallet.com",
        "links": [
            {
                "rel" : "https://payid.org/ns/payid-easy-checkout/1.0",
                "template": "https://wallet.com/checkout?amount={amount}&receiverPayId={receiverPayId}&currency={currency}&nextUrl={nextUrl}"
            }
        ]
     }
     
The receiver PayID Discovery client must parse this response to find the PayID Easy Checkout Link. 
If the JRD returned from the PayID Easy Checkout Discovery query does not contain a 
PayID Easy Checkout Link in its 'links' collection, PayID Easy Checkout is considered to have failed.
Once a PayID Easy Checkout URI Template has been obtained from the PayID Easy Checkout Link by the PayID client, 
PayID Easy Checkout Discovery is considered to be complete. 

### Template Syntax
This specification defines a simple template syntax for PayID Easy Checkout URI Template
transformation.  A template is a string containing brace-enclosed
("{}") variable names marking the parts of the string that are to be
substituted by the corresponding variable values.

This specification defines several variables, which MAY or MAY NOT be present in every PayID Easy Checkout URI Template.
These variables are as follows:
    
    'amount': The amount that should be sent by the sender to the receiver
    'receiverPayID': The PayID URI of the receiver
    'assetCode': The ISO-4217 currency code that the sender should send
    'paymentNetwork': The payment network, as defined in [PAYID-PROTOCOL][], that the sender should send payment over.
    'nextURL': A URL that the sender's wallet can use after completing the payment
    
When substituting values into a URI 'path' or 'query' part as defined by
[RFC3986][], values with characters outside the character set allowed by paths or query parameters in [RFC3986][], 
respectively, MUST be percent or otherwise encoded.

Protocols MAY define additional variables and syntax rules, but MUST NOT
change the meaning of the variables specified in this document. If a client is unable to
successfully process a template (e.g., unknown variable names, unknown or
incompatible syntax), the link SHOULD be ignored.

The template syntax ABNF is as follows:

    uri-char     =  ( reserved / unreserved / pct-encoded )
    var-char     =  ALPHA / DIGIT / "." / "_"
    var-name     =  %x61.63.63.74.70.61.72.74 / ( 1*var-char )
    variable     =  "{" var-name "}"
    PAYID-EASY-CHECKOUT-URI-Template =  *( uri-char / variable )

For example:

    Input:    alice$wallet.com
              amount = 10
              receiverPayID=pay$merchant.com
              currency=XRP
              network=XRPL
              nextUrl=https://merchant.com/thankyou
    Template: https://wallet.com/checkout?amount={amount}&receiverPayId={receiverPayID}&currency={currency}&network={network}&nextUrl={nextURL}
    Output:   https://wallet.com/checkout?amount=10&receiverPayId=payid%2Apay%24merchant.com&currency=XRP&network=XRPL&nextUrl=https://merchant.com/thankyou

TODO: Should we define acceptable URL template variable values for the redirect?

## PayID Easy Checkout URL Assembly
The PayID Easy Checkout URL is constructed by expanding the PayID Easy Checkout URI Template as defined in section 3 of
[RFC6570][] buy applying values corresponding to the variables specified in
section (TODO: link to template section) to the Template.

The PayID Checkout URI template MAY not contain the complete set of variables specified in section (TODO link section) 
of this document.  However, PayID Easy Checkout clients MUST replace each variable present in the URI Template with a value. 
PayID Easy Checkout clients MAY replace URI Template values with an empty string, however it is RECOMMENDED that each
variable be replaced with a non-empty value.

The result of expanding the PayID Easy Checkout URI Template is a PayID Easy Checkout URL.
This URL SHOULD represent a resource that the payer can use to complete a payment. As previously stated, the
ways in which the payer and merchant use that resource is outside the scope of this protocol.
The PayID Easy Checkout URL SHOULD be parsed to retrieve the values set by the recipient client. It is RECOMMENDED
that wallet UIs use these values to pre-populate a payment transaction.

# PayID Easy Checkout JRDs
This section defines the PayID Easy Checkout Link, which conforms to section 4.4 of the
Webfinger RFC.  In order for a PayID server to enable PayID Easy Checkout, a PayID Discovery query to the server
MUST return a JRD containing a PayID Easy Checkout Link.

The Link MUST include the Link Relation Type of section (TODO: link to link type section) in the object's 'rel' field.
The Link MUST also include a PayID Easy Checkout URI Template in the 'template' field of the link.

    * 'rel': `https://payid.org/ns/payid-easy-checkout-uri/1.0`
    * 'template': A PayID Easy Checkout URI Template

The following is an example of a PayID Easy Checkout Link that indicates a PayID Easy Checkout URI Template:

    {
        "rel": "https://payid.org/ns/payid-easy-checkout-uri/1.0",
        "template": https://wallet.com/checkout?amount={amount}&receiverPayId={receiverPayID}&currency={currency}&network={network}&nextUrl={nextURL}
    }

# Security Considerations
Various security considerations should be taken into account for PayID
Easy Checkout.

The security considerations for PayID Easy Checkout Discovery are discussed in 
section 6 of [PAYID-DISCOVERY][].

## PayID Easy Checkout Redirection URI Manipulation
When a payer uses the resource located at the PayID Easy Checkout URL, a hijacker could manipulate
the data encoded in the URL to trick the sender into sending a payment to a different PayID than was originally
requested, or manipulate other points of PayID Easy Checkout data to trick the sender. 

Additionally, if a hijacker gained access to the merchant client, they could replace the PayID Easy Checkout URI Template 
for the purposes of a phishing attack.

Current work on the PayID Protocol and its extensions may prove useful in mitigating these risks. 

## Access Control
As with all web resources, access to the PayID Discovery resource could
require authentication. See section 6 of [RFC7033][] for Access Control
considerations.

Furthermore, it is RECOMMENDED that PayID servers only expose PayID Easy Checkout URI Templates
which resolve to a protected resource.  

# IANA Considerations
  ## New Link Relation Types
  This document defines the following Link relation type per [RFC7033][].
  See section 3 for examples of each type of Link.
  
  ### PayID Discovery URI Template
  
    * Relation Type ('rel'): `https://payid.org/ns/payid-easy-checkout-uri/1.0`
    * Media Type: `application/jrd+json`
    * Description: PayID Discovery URI Template, version 1.0



# Acknowledgments

# Appendix

## Motivation
The PayID Easy Checkout Protocol aims to enable a consistent user experience for payers paying for goods
or services by standardizing the interaction between merchants/non-profits and customer/donor wallets.
Given the ability to assign arbitrary metadata to a PayID as defined in [PayID-Discovery][], there is an opportunity
to standardize the set of interactions between merchant and payer, specifically the process by which a merchant
directs a payer to their digital wallet to complete a payment.
We believe this protocol will enable an improved paying experience by reducing the number
of steps a payer must take to complete a transaction.

PayID Easy Checkout also limits the engineering effort needed to implement the protocol. 
Clients wishing to adopt this pattern should only need to implement UI-level changes in order to make the flow function 
as intended, which may aid in expanding overall adoption, further enhancing the protocol's user experience benefits. 

### Design Goals

#### Minimal effort for the Payer

In order for a payer to checkout using the PayID Easy Checkout protocol, the payer only needs to provide a merchant
with their PayID Easy Checkout enabled PayID.

#### No New Server-Side Software

Apart from a PayID Discovery compliant PayID Server, The PayID Easy Checkout Protocol does not require server-side 
software to be run by either the payer or merchant for a payment. The PayID server is capable of providing details 
of where to send the payer via the PayID Discovery Protocol. Assuming the wallet used by the payer has implemented 
support in their UI for the PayID Easy Checkout Protocol, the payer can be redirected to their wallet 
to complete their transaction.

## Example Usage
This section shows a non-normative example of PayID Easy Checkout between a hypothetical merchant and payer. The merchant
accepts payments using the PayID pay$merchant.com, and the payer controls the PayID alice$wallet.com.

### PayID Easy Checkout Initiation
In this example, the payer might place some items in an online shopping cart on the merchant's web-site, then choose
to checkout.  The merchant would then render a form asking for the payer's PayID, as well as a "Checkout with PayID"
button.  Once the payer inputs their PayID `alice$wallet.com` and clicks the "Checkout with PayID" button, the merchant
begins the PayID Easy Checkout flow.

### PayID Easy Checkout Wallet Discovery
The merchant UI would first assemble the PayID Discovery URL as defined in section 4.1.1 of [PAYID-DISCOVERY][],
yielding the URL `https://wallet.com/.well-known/webfinger?resource=payid%3Aalice%24wallet.com`. 
The merchant UI would then query the assembled URL as defined in section 4.1.2 of [PAYID-DISCOVERY][].

The HTTP request in this example would look like this:
    
    GET /.well-known/webfinger?resource=payid%3Aalice%24wallet.com
    Host: wallet.com
    
If the payer's PayID server has enabled PayID Easy Checkout in their wallet, the server would respond with something like this:
     
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

### Expand Wallet Discovery URL Template
The merchant UI would parse the PayID Discovery response and iterate over the "links" collection to find the link with 
the Relation Type of "https://payid.org/ns/payid-easy-checkout/1.0". The UI can then do a search and replace on
the "template" field value in the link, replacing all occurrences of the predefined query parameter template names with 
the values they want to send to the payer's wallet. One query parameter of note is the "nextUrl" parameter, which
allows the merchant to supply a redirect or callback URL for the sender's wallet to call once the payer has confirmed
the payment. In this example, the merchant would like to display a "Thank You" page, and replaces `{nextUrl}` 
with `https://merchant.com/thankyou`.

### Redirect Payer to Their Wallet
Once the merchant UI populates the required query parameters in the URL template, the merchant UI redirects the payer to 
the Redirect URL so that the payer can confirm the payment.

### Payer Confirms Payment
After the payer clicks the "Pay with PayID" button the merchant's UI, and the merchant performs the previous steps,
the payer will be redirected to the Redirect URL, which is a front end resource of the wallet. The wallet UI can
read the query parameters from the Redirect URL and render a confirmation page or modal with all of the required fields
pre-populated.

Once the payer confirms the payment, the wallet would perform a PayID address lookup on the "receiverPayId" query
parameter to get the payment address of the merchant and submit a transaction to the underlying ledger or payment system.
The merchant can then redirect the user back to the URL specified in the "nextUrl" query parameter, which will display
the "Thank You" page of the merchant.
