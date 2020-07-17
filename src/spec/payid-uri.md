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

normative:
    RFC2119:
    RFC3629:
    RFC3986:
    RFC5234:
    RFC5890:
    RFC5892:
    RFC8264:
    PAYID-DISCOVERY:
      title: "The PayID Discovery Protocol"
      target: https://tbd.example.com/
      author:
        ins: D. Fuelling
        fullname: David Fuelling
    PAYID-PROTOCOL:
      title: "PayID Protocol"
      target: https://tbd.example.com/
      author:
         - ins: A. Malhotra
           fullname: Aanchal Malhotra
         - ins: D. Schwartz
           fullname: David Schwartz
    VERIFIABLE-PAYID:
       title: "Verifiable PayID Protocol"
       target: https://tbd.example.com/
       author:
         - ins: A. Malhotra
           fullname: Aanchal Malhotra
         - ins: D. Schwartz
           fullname: David Schwartz
    UNICODE:
      title: "The Unicode Standard"
      target: http://www.unicode.org/versions/latest/
      author:
        surname: The Unicode Consortium
        fullname: The Unicode Consortium

informative:
    RFC0020:
    RFC5988:
    RFC6068:
    RFC7033:
    RFC7565:
    RFC7595:

--- note_Feedback
  This specification is a draft proposal, and is part of the
  [PayID Protocol](https://payid.org/) initiative. Feedback related to this
  document should be sent in the form of a Github issue at:
  https://github.com/payid-org/rfcs/issues.

--- abstract
  This specification defines the 'payid' Uniform Resource Identifier (URI)
  scheme as a way to identify a payment account at a service provider.

--- middle

# Introduction
   Various Uniform Resource Identifier (URI) schemes can be used to
   identify a user account at a service provider. However, no standard
   identifier exists to identify a user's _payment_ account at a service
   provider.

   While popular URIs could be re-used as payment account identifiers,
   these identifiers are insufficient because they are typically recognized
   as supporting functionality unique to those schemes. For example, the
   'mailto' scheme [RFC6068][] is broadly deployed for messaging. Re-using
   this identifier for payments would likely cause confusion because one
   desirable quality of a payment account identifier is that it expressly
   does not support messaging, in order to avoid spam and/or other security
   concerns such as phishing attacks.

   Deploying payment protocols on top of identifiers that are commonly
   employed for other use-cases would likely be a mis-use of those
   identifiers, and could also cause confusion for end-users, among other
   problems.

   Instead, the 'payid' scheme defines an identifier that is intended to
   identify accounts for payment use-cases only.

# Terminology
   The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
   "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and
   "OPTIONAL" in this document are to be interpreted as described in
   [RFC2119][].

# Definition
   The syntax of the 'payid' URI scheme is defined in Section 7 of this
   document.

   A 'payid' URI identifies a payment account hosted at a service provider,
   and is designed for payment account identification rather than
   interaction, as discussed in section 1.2.2 of [RFC3986].

   A 'payid' URI is constructed by taking a user's payment account identifier
   at a service provider and using that value as the 'acctpart'. The 'host'
   portion is then set to the DNS domain name of the service provider that
   provides the 'payid'.

   To compare two 'payid' URIs, case normalization and percent-encoding
   normalization (as specified in sections 6.2.2.1 and 6.2.2.2 of
   [RFC3986]) MUST be employed before performing any comparison.

   In addition, a 'payid' is case-insensitive and therefore should be
   normalized to lowercase.  For example, the URI
   "PAYID:aLICE$www.EXAMPLE.com" is equivalent to
   "payid:alice$www.example.com".

   Note that both the 'acctpart' and 'host' components of a 'payid' may
   contain one or more dollar-sign characters. However, because a 'host'
   SHOULD also be a valid DNS domain, that portion of a 'payid' will
   generally not include a dollar-sign. Therefore, applications SHOULD
   always search for the last dollar-sign when attempting to parse a 'payid'
   URI into its two component parts.

# Examples
   As an example, a user with an account name of "apollo" at a wallet
   service "wallet.example.com" can be identified by a URI using the 'payid'
   scheme via the following construction:

     'payid:apollo$wallet.example.com'

   One possible PayID scenario is for an account to be registered with a
   payment service provider using an identifier that is associated with some
   other service provider. For example, a user with the email address
   "alice@example.net" might register with a wallet website whose domain
   name is "wallet.example.com". In order to facilitate payments to/from
   Alice, the wallet service provider might offer Alice a PayID using Alice's
   email address (though using an email address as a PayID is not
   recommended).  In order to use Alice's email address as the 'acctpart' of
   the 'payid' URI, no percent-encoding is necessary because the 'acctpart'
   portion of a PayID allows for at-signs. Thus, the provisioned 'payid' URI
   for Alice would be "payid:alice@example.net$shoppingsite.example".

   Another possible scenario is where a payment service provider (e.g., a
   digital wallet) provides its users with PayIDs that are associated with
   the PayIDs of another service provider. For example, a user with the
   PayID "alice$bank.example.net" might register with a wallet website whose
   domain name is "wallet.example.net". In order to use the bank's PayID
   as the acctpart of the wallet's 'payid' URI, no percent-encoding is
   necessary because the 'acctpart' portion of a PayID allows for
   dollar-signs. Therefore, the resulting 'payid' URI would be
   "payid:alice$bank.example$wallet.example".

   The following example URIs illustrate several variations of PayIDs and
   their common syntax components:

         payid:alice$example.net

         payid:john.doe$example.net

         payid:jane-doe$example.net

# Security Concerns
   The 'payid' URI scheme defined in this document does not directly enable
   interaction with a user's payment account and therefore does not present
   any direct security concerns.

   However, a 'payid' URI indicates existence of a payment account, so
   care should be taken to properly secure any payment account interactions
   allowed by a service provider.

   In addition, service providers and users should consider whether an
   attacker might be able to derive or infer other identifiers correlating
   to the user of any particular PayID. For example, replacing the `$`
   character in a PayID with an `@` sign SHOULD NOT yield a 'mailto' URI,
   when possible. In addition, care should be taken when the 'acctpart' of
   a PayID corresponds to a user's email address (in part or in whole) as this
   might allow an attacker to execute phishing attacks or send spam messages.

   Due to the use of percent-encoding in 'payid' URIs, implementers SHOULD
   disallow percent-encoded characters or sequences that would result in
   "space", "null", "control", or other characters that are otherwise
   forbidden.

# Internationalization Concerns
   As specified in [RFC3986], the 'payid' URI scheme allows any character
   from the Unicode repertoire [Unicode] encoded as UTF-8 [RFC3629] and
   then percent-encoded into valid ASCII [RFC0020]. Before applying any
   percent-encoding, an application MUST ensure the following about the
   string that is used as input to the URI-construction process:

   * The 'acctpart' consists only of Unicode code points that conform to
     the PRECIS IdentifierClass specified in [RFC8264].

   * The 'host' consists only of Unicode code points that conform to the
     rules specified in [RFC5892].

   * Internationalized domain name (IDN) labels are encoded as A-labels
    [RFC5890].

# IANA Considerations
   In accordance with [RFC7595], this section provides the information
   needed to register the 'payid' URI scheme.

   **URI Scheme Name**: payid

   **Status**: permanent

   **URI Scheme Syntax**:  The 'payid' URI syntax is defined here in Augmented
   Backus-Naur Form (ABNF) per [RFC5234], borrowing the 'host' and 'path'
   rules from [RFC3986]:

      payidURI   = "payid" ":" acctpart "$" host
      acctpart   = path

   Note that additional rules limit the characters that can be
   percent-encoded in a 'payid' URI. See "Encoding Considerations" below for
   more details.

   **URI Scheme Semantics**:  The 'payid' URI scheme identifies payment
      accounts hosted at payment service providers.  It is used only for
      identification, not interaction.

   **Encoding Considerations**:  See Section 6 of this document.

   **Applications/Protocols That Use This URI Scheme Name**: The following
    protocols utilize this URI scheme:

      - [PAYID-DISCOVERY][],
      - [PAYID-PROTOCOL][],
      - [VERIFIABLE-PAYID][].

   **Interoperability Considerations**:  n/a.

   **Security Considerations**:  See Section 6 of this document.

   **Contact**:  rfcs@payid.org

   **Author/Change Controller**:  TBD.

   **References**:  None.

# Acknowledgements
  This document was adapted from and heavily influenced by [RFC7565][],
  modifying it (in some cases only slightly) for a payments use-case. The
  author would like to acknowledge the contributions of everyone who worked
  on that and related specifications.

  In addition, the author would like to acknowledge everyone who provided
  feedback and use-cases for this derivative specification.
