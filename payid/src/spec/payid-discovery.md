---
coding: utf-8

title: PayID Discovery
docname: draft-fuelling-payid-discovery-01
category: std

pi: [toc, sortrefs, symrefs, comments]
smart_quotes: off
 
area: security
author:
       
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
      
normative:
    RFC2119:
    RFC3986:
    RFC4627:
    RFC6570:
    RFC7033:
    PAYID-URI:
      title: "The 'payid' URI Scheme"
      target: https://tbd.example.com/
      author:
        ins: D. Fuelling
        fullname: David Fuelling

informative:
    
--- note_Feedback

This specification is a part of the [PayID Protocol](https://payid.org/) work.
 Feedback related to this specification should be sent to <payid@ripple.com>.

--- abstract
This specification defines the PayID Discovery protocol, which can be used
to discover information about a 'payid' URI using standard HTTP methods.

The primary use-case of this protocol is to define how to transform a 
PayID URI into a URL that can be used with other protocols.   
       
--- middle

# Introduction
   PayID Discovery is used to transform a PayID URI [PAYID-URI][] into a URL 
   that can be used by higher-order protocols using HTTP.
      
   This document specifies two modes of PayID discovery: one using
   Webfinger [RFC7033][] to resolve a corresponding URL from a PayID URI
   in an automated fashion; and one using a manual, fallback mechanism
   to assemble a URL from a PayID URI by-hand.
   
   In 'automated' mode, a PayID can be presented to a service endpoint that
   supports PayID Discovery. The resource returns a Webfinger-compliant 
   JavaScript Object Notation (JSON) [RFC4627][] object that can be used
   to assemble PayID URL as defined by the procedure in section 4.1 of
   this document.
   
   Conversely, in "manual" mode, a PayID can be decomposed into a URL, without
   any intermediate server interaction, simply by transposing portions of the
   PayID into a URL format defined in section 4.2 of this document.
   
   It should be noted that "manual" mode does not allow divergence between the
   string characters in a PayID URI and any corresponding URL. Conversely, 
   "automatic" mode does allow such divergence. For example, in "manual" mode,
   the PayID 'alice$example.com' MUST always map to the URL 
   'https://example.com/alice', whereas in "automatic" mode that same PayID
   URI can map to any arbitrary URL structure determined buy the service
   provider, such as 'https://example.com/users/alice'.
      
   Information returned via PayID Discovery might be for direct human
   consumption (e.g., looking up someone's Bitcoin address), or it might be
   used by systems to help carry out some operation (e.g., facilitating,
   with additional security mechanisms, protocols to support compliance or 
   other legal requirements necessary to facilitate a payment).
      
   The information returned via this protocol is intended to be static
   in nature, and, as such, PayID Discovery is not intended to be
   used to return dynamic information like a payment account balance or 
   the current status of an account.
   
   PayID Discovery is designed to be used across many applications. Use of 
   PayID Discovery is illustrated in the examples in Section 3 and
   described more formally in Section 4.
   
# Terminology
   The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
   "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and
   "OPTIONAL" in this document are to be interpreted as described in
   [RFC2119][].

# Example Usage 
   This section shows sample uses of PayID Discovery in several
    hypothetical scenarios.

## Automated PayID Discovery by a Wallet 
   Suppose Alice wishes to send a friend some XRP from a web-based wallet
   provider that Alice has an account on. Alice would log-in to the wallet
   provider and enter Bob's PayID (say, `bob$receiver.example.com`) into the
   wallet UI to start the payment. 
   
   The Wallet application would first perform a WebFinger query looking for 
   the PayID Discovery service provider, like this:

     GET /.well-known/webfinger?
           resource=payid%3Abob%24receiver.example.com
           HTTP/1.1
     Host: receiver.example.com

   The server might respond like this:

     HTTP/1.1 200 OK
     Access-Control-Allow-Origin: *
     Content-Type: application/jrd+json

     {
       "subject" : "payid:bob$receiver.exmaple.com",
       "links" :
       [
         {  
           "rel": "http://payid.org/rel/discovery/1.0 ",
           "type": "application/payid-uri-template",
           "href": "https://receiver.exmaple.com/users/{acctpart}"
         }
       ]
     }

   Alice's wallet then uses URL template found in the `href` property to 
   assemble a PayId-specific URL, `https://receiver.exmaple.com/users/bob`.
   
   Per RFC-7033, Webfinger requests can be filtered by using a "rel" 
   parameter in the Webfinger request. Because support for the "rel" parameter
   is not required nor guaranteed, the client must not assume the "links" 
   array will contain only the link relations related to PayID Discovery.

## Manual PayID Discovery by a Wallet
  Suppose Alice wishes to send a friend some XRP from a web-based wallet
  provider that Alice has an account on. However, in this example, let's
   assume that the PayID Alice is wanting to pay doesn't support "automated"
   PayID discovery (i.e., the receiver's server doesn't support Webfinger).
   
   Alice would log-in to her wallet provider and enter Bob's PayID (say
   `bob$receiver.example.com`) to make a payment.
     
   The Wallet application would first attempt a WebFinger query as in the 
   example above, like this:
  
       GET /.well-known/webfinger?
             resource=payid%3Abob%24receiver.example.com&
             HTTP/1.1
       Host: receiver.example.com
  
   However, in this case the `receiver.exapmle.com` server doesn't support 
   "automated" PayID Discovery, so the server responds like this:
  
       HTTP/1.1 404 NOT FOUND
   
   Because Alice's Wallet can utilize "manual" PayID Discovery, the wallet
   software merely transforms `bob$receiever.example.com` into the URL 
   `https://receiver.example.com/bob`. Alice's wallet then uses that URL to 
   continue making a PayID payment.
   
   It should be noted that "manual" mode does not allow the PayID URI to
   diverge from the underlying URL returned via PayID Discovery. Because of
   this, "automated" PayID Discovery is generally preferred.

# PayID Discovery Protocol
  The PayID Discovery protocol is used to request information about an entity
  identified by a query target that is a PayID URI.

  The result of PayID Discovery is a URL that can be used for any other
  protocol purposes that are outside the scope of this document.

  To this end, PayID Discovery defines two modes of operation, "manual" and
  "automated" (defined below). Clients SHOULD attempt "automated mode" first.
  Failing that, "manual mode" SHOULD be used.

## Automated Mode
  This mode utilizes the Webfinger specification in a narrowly defined
  profile.

  A WebFinger resource is always given a query target, which is another
  URI that identifies the entity whose information is sought. For PayID
  Discovery, this supplied URI MUST always be a valid PayID URI, and must be
  supplied to the WebFinger service according to the rules defined in
  section 4.1 of Webfinger [RFC7033][].

  In this profile, the WebFinger resource returns a JSON Resource Descriptor
  (JRD) as the resource representation to convey information about the
  requested PayID. The returned JRD MUST have a URI template that can be used
  to map the source PayID to a de-referencable URL.

### Automated Mode URL Assembly
If a PayID Discovery server returns a JRD that conforms to section 4.1.2
below, then the following ruleset SHOULD be used to transform the
corresponding PayID URI into a URL:

1. Decompose the PayID URI into its component parts, per [PAYID-URI][], 
capturing the `acctpart` and `host` values. 
                                                                
1. Repalce the `acctpart` template variable in the URI template returned from 
#1 above and with the `acctpart` portion of the PayID URI.

The resulting URL can be used according to the rules defined in any other
protocol.

### PayID Disovery JRDs
  Any JRD returned via Webfinger MUST be compatible with section 4.4 of
  RFC-7033. As such, this protocol defines a single compatible 'link' as 
  follows:
            
  * 'rel': `http://payid.org/rel/discovery/1.0`
  * 'type': `application/payid-uri-template`
  * 'href': A URI template as defined by [RFC6570][] that specifies an
            `{acctpart}` template value specifying where in the service
            provider's URL-space the account identifier should be located.
  
## Manual Mode
  If "Automated" mode is not supported or otherwise fails to yield a PayID
  URL, then a URL can be assembled manually.
  
### Manual Mode URL Assembly
  If "Automated" mode is not supported or otherwise fails to yield a PayID
  URL, then the following predefined ruleset SHOULD instead be used to
  transform a PayID URI into a URL.

  1. Decompose the PayID URI into its component parts, per [PAYID-URI][], 
     capturing the `acctpart` and `host` values. 

  1. Using the `acctpart` and the `host`, assemble a URL according to the
     following URL template:`https://{host}/{acctpart}` 

The resulting URL can be used according to the rules defined in any other
protocol.

# Cross-Origin Resource Sharing (CORS)
PayID Discovery resources might not be accessible from a web browser due to
"Same-Origin" policies. See section 5 of RFC-7033 for CORS considerations
that apply to both "manual" and "automated" PayID Discovery modes.  

# Access Control
As with all web resources, access to the PayID Discovery resource could
require authentication. See section 6 of RFC-7033 for Access Control
considerations that could apply to both "manual" and "automated" PayID
Discovery modes.  

# Hosted PayID Discovery Services
As with most services provided on the Internet, it is possible for a domain
owner to utilize "hosted" WebFinger services. Consult section 7 of 
RFC-7033 for considerations that could apply to both "manual" and
"automated" PayID Discovery when hosted by a third-party.  
      
# Security Considerations
Various security considerations should be taken into account for PayID
Discovery. 

Among other resource, consult section 9 of RFC-7033 and section 7 of
[RFC3986][] for important security considerations involved in PayID
Discovery.

# IANA Considerations

## New Link Relation Types 
This document defines the following Link relation type per RFC-7033:
     
  * Relation Type ('rel'): `http://payid.org/rel/discovery/1.0` 
  * Media Type: `application/payid-uri-template` 
  * Description: PayID Discovery URI Template, version 1.0

See section 3 for examples of this type of Link.
    
# Acknowledgments
This document was heavily influenced by, and builds upon, the Webfinger
protocol as defined in RFC-7033 (adapted for a payments use-case). The
author would like to acknowledge the contributions of everyone who worked
on that and related specifications. 

In addition, the author would like to acknowledge everyone who provided
feedback and use-cases for this derivative specification.
