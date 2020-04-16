---
coding: utf-8

title: The 'payid' URI Scheme
docname: draft-fuelling-payid-uri-01
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
    RFC3629:
    RFC3986:
    RFC5234:
    RFC5890:
    RFC5892:
    RFC7564:
    PAYID-DISCOVERY: 
      title: "The PayID Discovery Protocol"
      target: https://tbd.example.com/
      author:
        ins: D. Fuelling
        fullname: David Fuelling
    UNICODE:
      title: "The Unicode Standard"
      target: http://www.unicode.org/versions/latest/
      author:
        surname: The Unicode Consortium
        fullname: The Unicode Consortium
 
informative:
    RFC0020: 
    RFC4395:
    RFC5988:
    RFC6068:
    RFC7033:

--- note_Feedback

This specification is a part of the [PayID Protocol](https://payid.org/) work.
 Feedback related to this specification should be sent to <payid@ripple.com>.

--- abstract
 This specification defines the 'payid' Uniform Resource Identifier (URI) 
 scheme as a way to identify a payment account at a service provider, 
 irrespective of the particular protocols that can be used to interact with
 the account.
   
--- middle

# Introduction
   Various Uniform Resource Identifier (URI) schemes enable interaction
   with, or identify resources associated with, a user account at a
   service provider. However, no standard identifier exists to identify a
   user's payment account at a service provider.
   
   While popular URIs could be re-used as payment account identifiers, 
   these identifiers are insufficient because they are typically recognized
   as supporting functionality unique to those schemes. For example, the
   'mailto' scheme [RFC6068][] (which enables interaction with a user's email
    account
   ) is broadly deployed for messaging. Re-using this identifier for
   payments would likely cause confusion because one desirable quality of
   a payment account identifier is that it expressly does not support
   messaging, in order to avoid spam and/or other security concerns such as 
   phishing attacks.
   
   Deploying payment protocols on top of identifiers that are commonly
   employed for other use-cases would likely be a mis-use of those
   identifers, and could also cause confusing for end-users, among other
   problems. 
   
   Instead, the `payid` scheme uses a new type of identifier that is
   inteneded to identify accounts for payment use-cases only.

# Terminology
   The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
   "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and
   "OPTIONAL" in this document are to be interpreted as described in
   [RFC2119][].
   
# Definition
   The syntax of the 'payid' URI scheme is defined under Section 7 of
   this document.  Although 'payid' URIs take the form "user$host", the
   scheme is designed for the purpose of identification instead of
   interaction (regarding this distinction, see Section 1.2.2 of
   [RFC3986][]). The "Internet resource" identified by a 'payid' URI is a
   user's payment account hosted at a service provider, where the service
   provider is typically associated with a DNS domain name.  Thus, a
   particular 'payid' URI is formed by setting the "user" portion to the
   user's payment account name at the service provider and by setting the "host"
   portion to the DNS domain name of the service provider.

   Consider the case of a user with an account name of "apollo" on a
   wallet service "wallet.example.com".  It is taken as
   convention that the string "apollo$wallet.example.com" designates
   that payment account.  This is expressed as a URI using the 'payid' 
   scheme as 'payid:apollo$wallet.example.com'.

   A common scenario is for a user to register with a payment service provider
   using an identifier (such as an email address) that is associated
   with some other service provider. For example, a user with the email
   address "alice@example.net" might register with a wallet
   website whose domain name is "wallet.example.com". In order to facilitate
   payments to/from alice, the wallet service provider might offer alice a
   PayID using alice's email address. In order to use her email address as the
   localpart of the 'payid' URI, the at-sign character (U+0040) needs to be
   percent-encoded as described in [RFC3986]. Thus, the resulting 'payid' 
   URI would be "payid:alice%40example.net$shoppingsite.example".
   
   Another scenario is a payment service provider (e.g., a digital wallet) 
   that allows a user to use a PayID that is associated with some other
   payment service provider. For example, a user with the PayID 
   "alice$bank.example.net" might
   register with a wallet website whose domain name is "wallet.example.net". 
   In order to use her bank's PayID as the localpart of the wallet's 'payid' 
   URI, the dollar-sign character (U+0024) needs to be percent-encoded as
   described in [RFC3986].  Thus, the resulting 'payid' URI would be
   "payid:alice%24bank.example$wallet.example".

   It is not assumed that an entity will necessarily be able to interact
   with a user's PayID using any particular application protocol, 
   such as a wallet or banking application. To enable interactions
   (payments or otherwise), an entity would need to use the appropriate URI
   scheme for such a protocol. While it might be true that the 'payid' URI
   minus the scheme name (e.g., "user$example.com" derived from 
   "payid:user$example.com") could be paid via some application protocol, 
   that fact would be purely contingent and dependent upon the deployment
   practices of the payment account provider.
   
   Because a 'payid' URI enables abstract identification only and not
   interaction, this specification provides no method for dereferencing
   a 'payid' URI on its own, e.g., as the value of the 'href' attribute
   of an HTML anchor element.  For example, there is no behavior
   specified in this document for a 'payid' URI used as follows:

   ```
    <a href='payid:bob$example.com'>find out more</a>
   ```

   Any protocol that uses 'payid' URIs is responsible for specifying how
   a 'payid' URI is employed in the context of that protocol (in
   particular, how it is dereferenced or resolved; see [RFC3986]).  As a
   concrete example, an "Account Information" application of the
   WebFinger protocol [RFC7033] might take a 'payid' URI, resolve the
   host portion to find a WebFinger server, and then pass the 'payid' URI
   as a parameter in a WebFinger HTTP request for metadata (i.e., web
   links [RFC5988]) about the resource.  For example:

   ```
    GET /.well-known/webfinger?resource=payid%3Abob%24example.com HTTP/1.1
   ```

   In the above example, the service retrieves the metadata associated with
   the payment account identified by that URI and then provides that
   metadata to the requesting entity in an HTTP response.

   If an application needs to compare two 'payid' URIs (e.g., for
   purposes of authentication and authorization), it MUST do so using
   case normalization and percent-encoding normalization as specified in
   Sections 6.2.2.1 and 6.2.2.2 of [RFC3986]. In addition, the `acctpart` 
   is case-insensitive and therefore should be normalized to lowercase. 
   For example, the URI `PAYID:aLICE$www.EXAMPLE.com` is equivalent to
   `payid:alice$example.com`.
      
# Examples
   The following example URIs illustrate several variations of PayIDs
   and their common syntax components:
   
         payid:alice$example.net
         
         payid:John.Doe$example.net
         
         payid:jane-doe$example.net 

# Security Concerns
   Because the 'payid' URI scheme does not directly enable interaction
   with a user's payment account at a service provider, direct security concerns
   are minimized.

   However, a 'payid' URI does provide proof of existence of the payment
   account; this implies that harvesting published 'payid' URIs could
   prove useful for certain attackers -- for example, if
   an attacker can use a 'payid' URI to leverage more information about the
   account (e.g., via WebFinger) or if they can interact with protocol-
   specific URIs (such as 'mailto' URIs) whose user@host portion is the
   same as that of the 'payid' URI (e.g., replacing the `$` character with an
   `@` sign).

   In addition, protocols that make use of 'payid' URIs are responsible
   for defining security considerations related to such usage, e.g., the
   risks involved in dereferencing a 'payid' URI, the authentication and
   authorization methods that could be used to control access to
   personal data associated with a user's payment account at a service, and
   methods for ensuring the confidentiality of such information.

   The use of percent-encoding allows a wider range of characters in
   payment account names but introduces some additional risks.  Implementers
   are advised to disallow percent-encoded characters or sequences that
   would (1) result in space, null, control, or other characters that
   are otherwise forbidden, (2) allow unauthorized access to private
   data, or (3) lead to other security vulnerabilities.
   
# Internationalization Concerns
   As specified in [RFC3986], the 'payid' URI scheme allows any character
   from the Unicode repertoire [Unicode] encoded as UTF-8 [RFC3629] and
   then percent-encoded into valid ASCII [RFC0020].  Before applying any
   percent-encoding, an application MUST ensure the following about the
   string that is used as input to the URI-construction process:

   * The accountpart consists only of Unicode code points that conform to
     the PRECIS IdentifierClass specified in [RFC7564].

   * The host consists only of Unicode code points that conform to the
     rules specified in [RFC5892].

   * Internationalized domain name (IDN) labels are encoded as A-labels
    [RFC5890]. 
 
# IANA Considerations

In accordance with the guidelines and registration procedures for new
URI schemes [RFC4395], this section provides the information needed
to register the 'payid' URI scheme.

   **URI Scheme Name**: payid

   **Status**: permanent

   **URI Scheme Syntax**:  The 'payid' URI syntax is defined here in Augmented
   Backus-Naur Form (ABNF) [RFC5234], borrowing the 'host', 'pct-encoded',
   'sub-delims', and 'unreserved' rules from [RFC3986]:

      payidURI      = "payid" ":" accountpart "$" host
      accountpart   = unreserved / sub-delims  
                     *( unreserved / pct-encoded / sub-delims )

   Note that additional rules regarding the strings that are used as input
   to construction of 'payid' URIs further limit the characters that can be
   percent-encoded; see the Encoding Considerations as well as Section 6
   of this document.

   **URI Scheme Semantics**:  The 'payid' URI scheme identifies payment accounts
      hosted at payment service providers.  It is used only for
      identification, not interaction.  A protocol that employs the 'payid'
      URI scheme is responsible for specifying how a 'payid' URI is
      dereferenced in the context of that protocol.  There is no media type
      associated with the 'payid' URI scheme.

   **Encoding Considerations**:  See Section 6 of this document.

   **Applications/Protocols That Use This URI Scheme Name**: At the time of
     this writing, only the [payid-uri][] protocol uses the 'payid' URI
     scheme.  However, use is not restricted to this protocol, and the
     scheme might be considered for use in other protocols.

   **Interoperability Considerations**:  There are no known interoperability
      concerns related to use of the 'payid' URI scheme.

   **Security Considerations**:  See Section 5 of this document.

   **Contact**:  fuelling@ripple.com

   **Author/Change Controller**:  This scheme is registered under the IETF
      tree.  As such, the IETF maintains change control.

   **References**:  None.


# Acknowledgements
The author would like to acknowledge the contributions of everyone who
 provided feedback and use cases for this specification.
