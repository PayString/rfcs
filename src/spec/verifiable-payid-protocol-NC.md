---
coding: utf-8

title: Verifiable PayID Protocol Extension
docname: draft-aanchal-verifiable-payid-protocol-ext-01
category: std

pi: [toc, sortrefs, symrefs, comments]
smart_quotes: off

area: security
author:

  -
    ins: A. Malhotra
    name: Aanchal Malhotra
    org: Ripple
    street: 315 Montgomery Street
    city: San Francisco
    region: CA
    code: 94104
    country: US
    phone: -----------------
    email: amalhotra@ripple.com
    uri: https://www.ripple.com

  -
    ins: D. Schwartz
    name: David Schwartz
    org: Ripple
    street: 315 Montgomery Street
    city: San Francisco
    region: CA
    code: 94104
    country: US
    phone: -----------------
    email: david@ripple.com
    uri: https://www.ripple.com

normative:
    RFC2119:
    RFC2818:
    RFC8446:
    RFC7258:
    RFC5280:
    RFC6979:
    RFC8422:
    RFC7515:
    RFC7797:
    RFC7519:
    RFC4949:
    RFC7517:
    DID:
      title: "Digital Identity Alliance"
      target: "https://www.didalliance.org/"
    GiD:
      title: "Global identity"
      target: "https://www.global.id/"
    HUUID:
      title: "Human Universally Unique Identifier"
      target: "https://github.com/codetsunami/HumanUUID"
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
    PAYID-PROTOCOL:
      title: "PayID Protocol"
      author:
         ins: A. Malhotra
         fullname: Aanchal Malhotra
         ins: D. Schwartz
         fullname: David Schwartz
    VER-PAYID-PROTOCOL:
      title: "Verifiable PayID Protocol"
      author:
         ins: A. Malhotra
         fullname: Aanchal Malhotra
         ins: D. Schwartz
         fullname: David Schwartz

informative:
    RFC4732:

--- note_Feedback

  This specification is a draft proposal, and is part of the
  [PayID Protocol](https://payid.org/) initiative. Feedback related to this
  document should be sent in the form of a Github issue at:
  https://github.com/payid-org/rfcs/issues.

--- abstract

This specification defines one of the extensions of the verifiable PayID protocol [VER-PAYID-PROTOCOL][] that aims to enable trust minimized PayID service that has applications in non-custodial settings. More specifically this extension of verifiable PayID protocol eliminates the trust requirement between the PayID owner and their PayID service provider by allowing the non-custodial PayID service providers (wallets and exchanges) to send payment account(s) address information associated with a PayID [PAYID-URI][] that is digitally signed with the PayID private key of the PayID owner along with PayID owner's `identity` information and other meta-data needed to verify the signature.

--- middle

# Terminology
   This protocol can be referred to as the `Verifiable PayID Protocol Extension for Non-Custodial Services`. It uses the following terminology.

   * Endpoint: either the client or the server of the connection.
   * Sender: individual or entity originating the transaction. 
   * PayID client: the endpoint that initiates PayID protocol/sending side of the transaction.
   * PayID server: the endpoint that returns payment account(s) information/receiving side of the transaction (non-custodial wallets, exchanges, etc).
   * PayID owner: individual or entity receiving the transaction/owner of the PayID[PayID-URI][]. 
   * Digital Signture: As defined in [RFC4949][].

   The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [RFC2119][] and [RFC9174][].

# Motivation

   We anticipate that the most common use-case for retrieving the payment account(s) address information is to make transactions. Providers of such services can be categorized as custodial and non-custodial each of which operate under different security models. Non-custodial wallets and exchanges do not store their customers’ on-ledger private keys on their servers. The customers hold their private keys locally on their device and hence are in full control of their funds. As such, there is no trust requirement between the non-custodial wallets and exchanges and their customers. Additionally, since the customers hold the private keys so the wallets are not liable for any consequences coming from the lost, compromised or hacked private keys of the customers. Nor do they need their customers to trust their servers in case wallet's servers go malicious or are compromised.

   Basic PayID protocol [PAYID-PROTOCOL][] specifies a protocol to interact with a PayID server and retrieve a payment account(s) information resource along with other meta-data corresponding to the queried PayID. One of the assumptions in the security model of the basic PayID protocol that is less desirable is the trust requirement between the PayID owner and their PayID server to provide correct payment account(s) address information resource. As such, PayID server has full control over the contents of the response message, and may go rogue or be compromised. The PayID owner has no way of knowing if the PayID server behaves maliciously. This implicit trust assumption between the PayID owner and PayID server is not ideal in the non-custodial settings.
   
   The motivation for this extension of verifiable PayID protocol is to enable non-custodial service providers to provide non-custodial PayID service while preserving their existing security model. More specifically, this extension allows PayID owners to digitally sign the payment account(s) information resource locally on their application/device with their PayID private key (which never leaves their device). This signed payment account(s) information resource can then be securely transferred to the non-custodial PayID service providers' servers who can then send this to the PayID client in response to a PayID query along with PayID owner's `identity` information. The PayID client and/or the sender of the transaction can use this information to verify if the payment account(s) address information is signed by the PayID owner and decide if they want to proceed with the transaction.  

# Verifiable PayID Protocol Extension Specification
  The Verifiable PayID protocol is designed along the same design principles as [PAYID-PROTOCOL][].

## PaymentInformation Resource as JSON Web Signatures
  We describe the payment accounts(s) information resource per [PAYID-PROTOCOL][] that is digitally signed as `PaymentInformation` resource represented as a signed JSON Web Tokens (JWT) [RFC7519][] known as JSON Web Signature (JWS) [RFC7515][]. JWT is a compact, URL-safe means of representing claims to be transferred between two parties. JWTs encode claims as a JSON object that is used as the payload of a JWS structure enabling the claims to be digitally signed. Below we define the three components of JWS.

### JOSE Protected Header
  For JWS, the members of the JSON object represented by the JOSE Header describe the cryptographic operations applied to the JWS Protected header and the JWS payload and optionally additional properties of the JWS.

  For a complete list of members of this object, refer to [RFC7519][] and [RFC7515][]. Following is an example of a JWT object representing JOSE header parameters for JWS JSON Serialization syntax.

    {
           "name": "identityKey",
           "alg" : "ES256K", 
           "typ" : "JOSE+JSON",
           "b64" : "false",
           "crit": ["b64"],
           "jwk" :  {
                "kty": "EC",
                "use": "sig",
                "crv": "secp256k1", 
                "x"  : "0", 
                "y". : "0",
                    },
    }

#### name
The `name` Header Parameter identifies the type of signature. It is a new OPTIONAL header parameter that is not defined in the IANA JSON Web Signature and Encryption Header Parameters Registry.

#### alg
The "alg" (algorithm) Header Parameter identifies the cryptographic algorithm used to secure the JWS. This is a required field as described in [RFC7515][]. We RECOMMEND using "ES256K" which is Elliptic Curve Digital Signature Algorithm (ECDSA) using secp256k1 curve-type and SHA-256 hash-type as defined in IANA JSON Web Signature and Encryption Header Parameters Registry.

#### typ
The "typ" (type) Header Parameter is used by JWS applications to declare the media type of the complete JWS. This is an optional field as described in [RFC7515][].

#### b64
The "b64" (base64url-encode) Header Parameter is an extension to JWS specification that determines how payload is represented in the JWS and the JWS signing input. When the "b64" value is "false", the payload is represented simply as the JWS Payload value with no encoding; otherwise, it is represented as ASCII(BASE64URL(JWS Payload)). This is an optional field as described in [RFC7797][].

#### crit
The "crit" (critical) Header Parameter indicates that extensions to JWS specification are being used that MUST be understood and processed. This is a required field to be used with "b64" parameter as described in [RFC7797][].

#### jwk 
The "jwk" (JSON Web Key) Header Parameter represents the public key that is used to digitally sign the JOSE header and JWS payload. This parameter is represented as a JSON Web Key as specified in [RFC7517][]. In the header above, members of "jwk" represent the properties of the public key, including its value that corresponds to the algorithm "ES256K".

  * kty: The "kty" (key type) parameter identifies the cryptographic algorithm family used with the key, such as "EC" for Elliptic Curve.

  * use: The "use" (public key use) parameter identifies the intended use of the public key such as "sig" for signature.

  * crv : The "crv" (Curve) parameter represents the elliptic curve-type and the hash-type such as "secp256k1" represents curve-type `secp256k1` and the hash-type `SHA-256`. 

  * x : Since we assume the "alg" parameter as "ES256K" which is one from the ECDSA family, so "x" parameter represents the X-coordinate of the corresponding public key.

  * y : Since we assume the "alg" parameter as "ES256K" which is one from the ECDSA family, so "y" parameter represents the Y-coordinate of the corresponding public key.

Note: "jwk" is one way way of embedding public key in the JOSE header. For more details on other possible options for "alg" and representing public keys refer to [RFC7515][]. 

### JWS Payload
 The JWS payload is the message that needs to be signed.

      {
              "exp"  : "",
              "payId": "bob$wallet.com",
              "payIdAddress": {
                "expTime": "",
                "paymentNetwork": "XRPL",
                "environment": "TESTNET",
                "addressDetailsType": "CryptoAddressDetails",
                "addressDetails": {
                  "address": "T7CKYKhRujaxEs9fSxQwJApHsQVPKUgD7EtLWCGTAFBwTha"
                }
              }
      }

#### exp
The `exp` field is an optional field as described in [RFC7797][]. If used, it SHOULD be set to the expiration time of the cryptographic key used to generate the digital signature.

#### payId
The `payId` field is a required field. The value of `payId` field is the PayID URI in the client request that identifies the payment account information that the JSON object describes.

#### PayIDAddress
The `PayIDAddress` is a required field. The value of `PayIDAddress` field is a JSON object with the following keys: 

  * "expTime": This is an optionl field. The value of `expTime` string is set to the maximum time upto which the payment address in the `address` field is valid.

  * "paymentNetwork": The value of the `paymentNetwork` is the value of payment-network string as specified in the client request's `Accept` header.

  * "environment": The value of `environment` string is the value of environment as specified in the client request's `Accept` header.

  * "addressDetailsType": The value of `addressDetailsType` is one of the following strings:

       * CryptoAddressDetails

       * FiatAddressDetails

  * "addressDetails": The value of `addressDetails` is the address information necessary to send payment on a specific `paymentNetwork` and `environment`.

  The `address` field MUST be present in the JWS payload.

### JWS signature
The JWS signature is the digital signature which is calculated over the JOSE header and the JWS payload.

     "signature": "base64Signature"

#### signature
The value of `signature` field is computed as described in [RFC7515][].

## End-to-End Verifiable PayID Protocol Flow for Non-Custodial Wallets and Exchanges
A pre-requisite for this extension of Verifiable PayID protocol requires the PayID owner to transfer signed `PaymentInformation` to the PayID server. In this document we specify one such way of doing this. 

Following are the pre-steps at PayID owner's (non-custodial wallet's customer) device locally:

### Generating PayID Key-pair
We RECOMMEND using elliptic curve (EC) key type with Elliptic Curve Digital Signature Algorithm (ECDSA) with secp256k1 curve for signature. 

### Generating JWS Token 
For each `payment-network` and `environment` that the PayID owner has a payment address for, generate the JOSE header, JWS Payload and JWS Signature as described above. A complete `PaymentInformation` response might look like:

{

     "payId": "bob$wallet.com",
     "addresses": [],
     "verifiedAddresses": [
        {
            "signatures": [
              {
                "protected": {
                  "name": "identityKey",
                  "alg": "ES256K", 
                  "typ": "JOSE+JSON",
                  "b64": "false",
                  "crit": ["b64"],
                  "jwk": {
                      "kty": "EC",
                      "use": "sig",
                      "crv": "secp256k1", 
                      "x": "b8w36l6eCf7GyD5fvXp0Xj7ugdFuvYYcnmb1VRjBl5g=", 
                      "y": "Tp8RPAf4dWkd+K/BApSW/Ey5UJs53NOPJRqDNZzItPc=",
                  },
                },
                "signature": "base64Signature",
              }
              ]
            "payload": {
                "exp" :
                "payId": "bob$wallet.com",
                "payIdAddress": {
                  "expTime":
                  "paymentNetwork": "XRPL",
                  "environment": "TESTNET",
                  "addressDetailsType": "CryptoAddressDetails",
                  "addressDetails": {
                    "address": "T7CKYKhRujaxEs9fSxQwJApHsQVPKUgD7EtLWCGTAFBwTha"
                   }
                 }
              }
            }
          ]
        }

* addresses: The `addresses` array is an OPTIONAL field. The implementations MAY choose to populate this field with payment address(es) information as per [PAYID-PROTOCOL][]. The implementations SHOULD refer to Security Considerations sections for the possible security trade-offs while using this field.

* VerifiedAddresses: The `VerifiedAddresses` is a required field.

### Posting signed response to non-custodial PayID service Provider's server
Implementations SHOULD use a secure communication channel to transfer these resources to the PayID server.

## Basic Operations
  Following are the basic operations performed by the verifiable PayID client and PayID server to retrieve `PaymentInformation` resource corresponding to PayID.

### PayID Client Requesting the PaymentInformation Resource
  When requesting the `PaymentInformation` resource, a verifiable PayID client MAY use the same HTTP `GET` method as in [PAYID-PROTOCOL][] to the PayID URL without any query parameters and body.

  The PayID client MUST query the PayID server using HTTPS only. [RFC2818][] defines how HTTPS verifies the PayID server's identity. If the HTTPS connection cannot be established for any reason, then the PayID client MUST accept that the PayID request has failed and MUST NOT attempt to reissue the PayID request using HTTP over a non-secure connection.

### PayID Server Responding to the PaymentInformation Resource Request
  Upon receiving a `GET` request for a payment accounts(s) information resource or a `PaymentInformation` resource, a PayID server that supports Verifiable PayID protocol returns the `PaymentInformation` resource for the `payment-network` and `environment` requested by the PayID client in the request `Accept` header field, along with other required and/or optional metadata.

  However, if PayID server does not support the Verifiable PayID protocol, the PayID server sends back a response as described in [PAYID-PROTOCOL][].

  If the PayID server does not contain the payment accounts(s) information resource or a `PaymentInformation` resource resource corresponding to the request, the PayID server MUST respond with an appropriate error message. 

### Parsing the PaymentInformation Response
   The PayID client MUST conform to the verification of JWS as specified in [RFC7515][] and [RFC7519][].

# Example Use of the Verifiable PayID Protocol
  This section shows sample use of this extension of verifiable PayID protocol in a hypothetical scenario.

## Verifiable PayID Protocol by a Non-Custodial Wallet as PayID Server
  Suppose Alice wishes to send a friend some XRP from a web-based wallet provider that Alice has an account on. Alice would log-in to the wallet provider and enter Bob's PayID (say, `bob$wallet.com`) into the wallet UI to start the payment.
  The Wallet application would first discover the PayID URL for the PayID service-provider using one of the mechanisms described in PayID discovery [PAYID-DISCOVERY][] protocol.

  The Wallet application would then issue an HTTPS GET request:

     GET /users/bob HTTP/1.1
     Host: www.wallet.com
     Accept: application/xrpl-testnet+json
     PayID-version: 1.0

  Bob's wallet who is a non-custodial PayID server might respond like this:

     HTTP/1.1 200 OK
     Content-Type: application/json
     Content-Length: 403
     PayID-Version: 1.0
     Cache-Control: "no-store"
     Server: Apache/1.3.11
    {
     "payId": "bob$wallet.com",
     "addresses": [],
     "verifiedAddresses": [
        {
            "signatures": [
              {
                "protected": {
                  "name": "identityKey",
                  "alg": "ES256K", 
                  "typ": "JOSE+JSON",
                  "b64": "false",
                  "crit": ["b64"],
                  "jwk": {
                      "kty": "EC",
                      "use": "sig",
                      "crv": "secp256k1", 
                      "x": "b8w36l6eCf7GyD5fvXp0Xj7ugdFuvYYcnmb1VRjBl5g=", 
                      "y": "Tp8RPAf4dWkd+K/BApSW/Ey5UJs53NOPJRqDNZzItPc=",
                  },
                },
                "signature": "base64Signature",
              }
              ]
            "payload": {
                "exp" : "1234574940",
                "payId": "bob$wallet.com",
                "payIdAddress": {
                  "expTime": "34874613475",
                  "paymentNetwork": "XRPL",
                  "environment": "TESTNET",
                  "addressDetailsType": "CryptoAddressDetails",
                  "addressDetails": {
                    "address": "T7CKYKhRujaxEs9fSxQwJApHsQVPKUgD7EtLWCGTAFBwTha"
                   }
                 }
              }
            }
          ]
      }

 In the above example, the `PaymentInformation` resource is a pre-signed message with the PayID private keys of the PayID owner Bob. Bob's non-custodial wallet retrieves this response and sends it to the PayID client.

# Security Considerations
  This security considerations section only considers verifiable PayID clients and servers bound to implementations as defined in this document.

  The security guarantees mentioned in [PAYID-PROTOCOL][] applies to this extension of verifiable PayID protocol. In this section, we discuss a security model for the Verifiable PayID protocol for non-custodial service providers.

## Security Model for Non-Custodial PayID Service Providers

  In the current security model, non-custodial wallets do not store their customers’ keys on their servers. The customers hold their private keys on their device. There is a no trust requirement between the service provided by the non-custodial wallets and the customers of this service. Since the customers hold the private keys:
  * The wallets are not liable for any consequences coming from the lost, compromised or hacked private keys of the customers.
  * The non-custodial wallets do not require their customers to trust their servers in case wallets servers go malicious or are compromised.

  This extension of verifiable PayID protocol preserves this trust model. Consequently, a non-custodial wallet running a PayID server has no liability for providing accurate `PaymentInformation` for their customers that is signed with the private key of the non-custodial PayID server wallet. Instead, the PayID owners can generate these signed mappings with their own PayID private key locally on their app/device. The sender of the payment (PayID client wallet’s customer) can easily verify these signatures based on the trust relationship between the sender and the receiver (PayID owner). This eliminates any risk of the non-custodial PayID server wallet losing its private keys, going malicious, getting hacked, or becoming otherwise compromised in a way that customers might lose funds.

## Using JWTs
The implementations of this extension of Verifiable PayID protocol MUST refer to the Security Considerations sections of [RFC7515][] and 

## Using addresses Array
The `addresses` array in the `PaymentInformation` response is an array of unsigned payment addresses. Implementations of this extension of verifiable PayID that choose to populate this array along with the `verifiedAddresses` array MAY be vulnerable to downgrade attacks. We RECOMMEND against populating this array unless absolutely necessary depending on the use-case.
ALso, note that this approach is not backwards-compatible with the PayID clients that do not understand verifiable PayID.

# Privacy Considerations
All privacy guarantees in the Privacy Considerations section of [PAYID-PROTOCOL][] apply to this extension of verifiable PayID protocol.

