---
coding: utf-8

title: PayID Aliases
docname: draft-romero-payid-aliases-01
category: std

pi: [toc, sortrefs, symrefs, comments]
smart_quotes: off

area: security
author:
      
  -
    ins: J. Romero
    name: Javi Romero
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

--- note_Feedback
  This specification is a draft proposal and is part of the 
  [PayID Protocol](https://payid.org/) initiative. Feedback related to this 
  document should be sent in the form of a Github issue at: 
  https://github.com/payid-org/rfcs/issues.

--- abstract
  This specification defines the 'payid' aliases.
   
--- middle

# Introduction

   Although the PayID protocol supports returning multiple addresses in the Payment Account Information Response for a given Network and Environment,
   it might be useful to users to be able to create groups within a PayID that can separate accounts by its purpose or use.
   
   Gmail addresses offer a feature called aliasing that allows the same address to receive emails on some variations of the original address.
   For example, if a user owns `user@gmail.com` emails sent to the following addresses will all resolve to the primary address:

    user+newsletter@gmail.com
    user+family@gmail.com
    user+ads@gmail.com
    ...

   This kind of feature would be useful in the PayID context, allowing the user to create aliases and group accounts depending on its use or purpose.
 
# Terminology
   The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
   "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and
   "OPTIONAL" in this document are to be interpreted as described in
   [RFC2119][].
 
# Definition

   Create the concept of PayID aliases that will allow the user to create PayID aliases linked to the primary one.
   For example, by owning `alice$example.com` could allow the user to generate multiple aliases as needed:

    alice+main$example.com
    alice+savings$example.com
    alice+crypto$example.com
    ...

   So, in this example, "alice+main$example.com === alice+savings$example.com === alice+crypto$example.com".

   This approach implies that you will get back different payment details for alice+main than you would for alice+savings, for the same Network, Environment tuple.
   There will be a "wall" between aliases, so the only way to retrieve an address within "alice+savings$example.com" will be to request the "savings" alias.
   Requests made to the main group ("alice$example.com") will not return addresses linked to other aliases.

   Registrations of PayID variations after the plus sign should be blocked and return an error.

   The syntax convention could allow further features like the generation of single use / one time PayID's or assignment of different policies to different accounts under the same PayID.

# Examples                                                              
   A user with an account name of "alice" at a wallet
   service "example.com" will have a primary group of addresses under "alice@example.com".
   A request to the PayID server might look like this:

    GET /alice HTTP/1.1
    Host: example.com
    Accept: application/xrpl-mainnet+json
    PayID-version: 1.0

  And the response:

    HTTP/1.1 200 OK
    Content-Type: application/json
    Content-Length: 403
    PayID-version: 1.0
    Cache-Control: max-age=0
    Server: Apache/1.3.11
    {
      "payId" : "alice$example.com",
      "addresses" :
      [
        {  
          "paymentNetwork" : "xrpl",
          "environment" : "mainnet",
          "addressDetailsType" : "CryptoAddressDetails",
          "addressDetails" : {
              "address" : "XTVQWr6BhgBLW2jbFyqqufgq8T9eN7KresB684ZSHKQ3oDth"
            }
          }
        ],
        "memo" : "Additional optional Information"
    }

   A request to retreive addreses linked to an alias might look like this:

    GET /alice+savings HTTP/1.1
    Host: example.com
    Accept: application/xrpl-mainnet+json
    PayID-version: 1.0

   Returning a similar response:

    HTTP/1.1 200 OK
      Content-Type: application/json
      Content-Length: 403
      PayID-version: 1.0
      Cache-Control: max-age=0
      Server: Apache/1.3.11
      {
        "payId" : "alice$example.com",
        "alias" : "savings",
        "addresses" :
        [
          {  
            "paymentNetwork" : "xrpl",
            "environment" : "mainnet",
            "addressDetailsType" : "CryptoAddressDetails",
            "addressDetails" : {
                "address" : "XTVQWr6BhgBLW2jbFyqqufgq8T9eN7KresB684ZSHKQ3oDade"
              }
            }
          ],
          "memo" : "Additional optional Information"
      }

   Let's say that the PayID server already holds an account for "bob" ("bob$example.com").
   A request to create any variation with "bob" + "+" (plus sign) + "alias", for example "bob+crypto$example.com", should be blocked and return an error:

    HTTP/1.1 409 OK
    Content-Type: application/json
    Content-Length: 403
    PayID-version: 1.0
    Cache-Control: max-age=0
    Server: Apache/1.3.11
    {
      "statusCode": 409,
      "error": "Conflict",
      "message": "There already exists a user with the provided PayID."
    }
