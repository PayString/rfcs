---
coding: utf-8

title: Verifiable PayID Protocol
docname: draft-aanchal-verifiable-payid-protocol-01
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

informative:
    RFC4732:

--- note_Feedback

  This specification is a draft proposal, and is part of the
  [PayID Protocol](https://payid.org/) initiative. Feedback related to this
  document should be sent in the form of a Github issue at:
  https://github.com/payid-org/rfcs/issues.

--- abstract
This specification defines the Verifiable PayID protocol - an extension to [PAYID-PROTOCOL][] that aims to provide payment account(s) information associated with a PayID [PAYID-URI][] while allowing involved parties to exchange `identity` information and provides third-party verifiable cryptographic proof trail of the entire communication. More specifically, the Verifiable PayID protocol provides the following enhancements to the Basic PayID protocol[PAYID-PROTOCOL][].

* Verifiable Custodial PayID service: It allows custodial wallets and exchanges to send payment account(s) address information and other resources digitally signed with their off-ledger private key.

* Verifiable Non-Custodial PayID service: It allows non-custodial wallets and exchanges to send payment account(s) address information digitally signed with the off-ledger private key of the PayID owner along with PayID owner's `identity` information.

* Privacy-enhanced PayID service: It allows PayID service providers (both custodial and non-custodial) to deploy appropriate access control mechanisms by allowing the PayID clients or senders to transmit their `identity` information for authentication.

--- middle

# Terminology
   This protocol can be referred to as the `Verifiable PayID Protocol`. It uses the following terminology.

   * endpoint: either the client or the server of the connection.
   * sender: individual or entity originating the transaction.
   * PayID client: the endpoint that initiates PayID protocol/sending side of the transaction.
   * PayID server: the endpoint that returns payment account(s) information/receiving side of the transaction (custodial or non-custodial wallets, exchanges, etc).
   * receiver/PayID owner: individual or entity receiving the transaction/owner of the PayID[PayID-URI][].

   The terms `receiver` and `PayID owner` are used interchangeably.

   The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [RFC2119][] and [RFC9174][].

# Motivation
   Basic PayID protocol [PAYID-PROTOCOL][] specifies a protocol to interact with a PayID server and retrieve a payment account(s) information resource along with other meta-data corresponding to the queried PayID. The protocol relies on the underlying secure transport (TLS 1.3 [RFC8446][]) to ensure message integrity and privacy from network attackers. There are at least two assumptions in the security and privacy model of the basic PayID protocol that are less desirable.

   1. Trust requirement between the PayID client and PayID server: As pointed out in the security considerations section of the [PAYID-PROTOCOL][], PayID server has full control over the contents of the response message, and may go rogue or be compromised. The PayID client has no way of knowing if the PayID server behaves maliciously. This implicit trust assumption between the PayID client and server is not ideal in the world where the information provided by the PayID server may be used by the PayID client to transmit money.

   2. Privacy: Per [PAYID-PROTOCOL][], anyone can query the PayID server and retrieve the payment account(s) information corresponding to the queried PayID. The PayID server or PayID owner has no way of deploying access control mechanisms since the `identity` of the PayID client and the sender is unknown to the PayID server.

   The motivation for the Verifiable PayID protocol is threefold:

   1. Eliminate implicit trust assumption between the PayID client and server: While it is not possible for the protocol to prevent PayID server from acting maliciously, the best we can do is to allow for mechanisms in the protocol that enables PayID client to prove this misbehaviour to third-parties and potentially hold the PayID server legally accountable for misbehaving.

   2. Enhance privacy of the PayID protocol by allowing the PayID client to share their and sender's `identity` information with the PayID server. This information could then be used to:
      * Give the PayID owner and/or PayID server the ability to decide if they want to share their payment account(s) information and other resources with the PayID client or the sender.
      * Allow for an open standards based way for endpoints to keep verifiable records of their financial transactions, to better meet the needs of accounting practices or other reporting and regulatory requirements.

   3. Ensure that if the PayID server is compromised, an attacker cannot swap a payment address in the payment account information response and redirect funds to the attacker-controlled payment network and address. Allow the PayID server or PayID owner to pre-sign `PaymentInformation` in a cold/airgapped system offline instead of online on a hot wallet.

   4. Allows for non-custodial service providers to run non-custodial PayID service by allowing the PayID owners to digitally sign the `PaymentInformation` resource locally on their device with their off-ledger private keys and send PayID owner's `identity` information in the response. This information can then be used by the PayID client and sender to authenticate the PayID owner and decide if they want to proceed with the transaction.

# Verifiable PayID Protocol Specification
  The Verifiable PayID protocol is designed along the same design principles as [PAYID-PROTOCOL][].

## Basic Operations
  Following are the basic operations performed by the verifiable PayID client and PayID server to retrieve `PaymentInformation` resource corresponding to PayID.

### PayID Client Requesting the PaymentInformation Resource
  When requesting the payment accounts(s) information resource per [PAYID-PROTOCOL][] that is digitally signed and requires input parameters, the PayID client uses the HTTP `POST` method with path parameter `payment-setup-details` with an optional payload in JSON format. We define this resource as a `PaymentInformation` resource.

### PayID Server Responding to the PaymentInformation Resource Request
  Upon receiving a request for a `PaymentInformation` resource that the PayID server can provide, the PayID server normally returns the requested response. However, if PayID server does not support the Verifiable PayID protocol, the PayID server MAY send back an appropriate error code (TBD) to indicate to the PayID client that the resource is available via an HTTP `GET` request to an alternate URL.

## JSON Payloads

### PayID Client Request Query Body for PaymentInformation Resource
      {
       optional string identity,
       optional string memo
      }

#### identity
  The type/value of the `identity` field is TBD. We anticipate this being a mechanism for the PayID client to transmit their or the sender's `identity` information to the PayID server. This information can then be used by the PayID server/PayID owner to
  * Enhance privacy by exercising access control mechanisms such as authorized access via accept/deny lists, etc. for the `PaymentInformation` or other resources for a PayID.
  * Record-Keeping

#### memo
  The type/value of the `memo` field is TBD. `memo` field is a placeholder to ensure protocol extensibility. e.g. for the primary use-case of making payments, the PayID client MAY send information such as amount, scale, etc. necessary to make the payment.
  //TBD: The request body parameters will depend on the use-case.

### PayID Server Response Body for PaymentInformation Resource Request
  Refer to the payment account(s) information resource in [PAYID-PROTOCOL][].

### SignatureWrapper

  `SignatureWrapper` is an encapsulating wrapper for any verifiable PayID protocol messages. It allows for the generation of cryptographically signed third-party verifiable proofs of the contents of the messages exchanged between the participating endpoints. We define `SignatureWrapper` as JSON object with the following name/value pairs:

      {
       required string messageType,
       required Message message,
       required string publicKeyType,
       required array publicKeyData,
       required string publicKey,
       required string signature
      }

#### messageType
   The value of `messageType` is the message type of the signed `message`. `messageType` is essential for future extensibility of the protocol to include more message types. We define the following enum for message types:
    enum messageType
      {
        PaymentInformation
      }

#### message
   The value of `message` includes the contents of the Verifiable PayID protocol message of the type `messageType` to be signed.

#### publicKeyType
   The value of `publicKeyType` is the Public Key Infrastructure (PKI)/identity system being used to identify the signing endpoint. e.g. `X509+SHA512` means an X.509 certificate as described in [RFC5280][] and SHA512 hash algorithm used to hash the contents of `message` for signing. This field defaults to empty string. We define the following `publicKeyType` values. One can register more in future.

   | publicKeyType   | Description                  
   |-----------------|-----------------------------------------------------
   | X509+SHA512     | A X.509 certificate [RFC5280][]        
   | pgp+SHA512      | An OpenPGP certificate        
   | ecdsa+SHA256    | A secp256k1 ECDSA public key [RFC6979][] [RFC8422][]        

#### publicKeyData
   The value of `publicKeyData` is the PKI-system/identity data used to identify the signing endpoint who creates digital signatures over the hash of the contents of the `message`. e.g. in the case of X.509 certificates, it may contain one or more X.509 certificates as a list upto the root trust certificate. Defaults to empty.

#### publicKey
   The value of `publicKey` is the contents of the public key. Defaults to empty.

#### signature
   The value of `signature` is the digital signature over the hash of the contents of the `message` using the private key corresponding to the public key in `publicKey`. This is a proof that the `message` was signed by the corresponding private key holder.

# Custodial and Non-Custodial PayID Service Providers
We anticipate that the most common use-case for retrieving the `PaymentInformation` resource is to make transactions. We can categorize the providers of such services as follows:
* Custodial wallets and exchanges: Custodial wallets and exchanges hold the private keys of their customers on their servers and essentially hold their funds. There is an implicit trust between the custodial service provider and their customers.

* Non-Custodial wallets and exchanges: Non-custodial wallets and exchanges do not store their customers’ keys on their servers. The customers hold their private keys locally on their device. [Arguably] there is a no trust requirement between the non-custodial wallets and exchanges and their customers. Since the customers hold the private keys so the wallets are not liable for any consequences coming from the lost, compromised or hacked private keys of the customers. Nor do they need their customers to trust their servers in case wallet's servers go malicious or are compromised.

Notice that the custodial and non-custodial service providers operate under different trust models. To continue operating under the same trust model, verifiable PayID requires slightly different treatment for custodial and non-custodial service providers.

# Verifiable PayID Protocol for Custodial Wallets and Exchanges

  The Verifiable PayID protocol flow is similar to that of the Basic PayID protocol [PAYID-PROTOCOL][] with the following modifications.

    Sender  PayID client                                             PayID server    Receiver
      |           |                                                          |            |
      |PayID, etc.|                                                          |            |
      |---------->|                                                          |            |
      |           |     1.) POST /payment-setup-details request to PayID URL |            |
      |           |--------------------------------------------------------->|            |
      |           |     2.) 200 Ok                                           |            |
      |           |       Signed PaymentInformation response                 |            |
      |           |<---------------------------------------------------------|  Optional  |
      |           |                                                          |notification|
      |           |                                                          |----------->|
      |           |                                                          |            |


## Step 1: Preparing the HTTP Request to PayID URL using HTTP POST Method
  A verifiable PayID client issues a query using the HTTP `POST` method to the PayID URL with path parameter `payment-setup-details` and optional body parameters as described above.

## Step 2: Preparing the PaymentInformation Response
  In response, the PayID server returns a JSON representation of the `PaymentInformation` resource. `PaymentInformation` resource is the `signed` payment account(s) information message [PAYID-PROTOCOL][] for the payment-network and environment requested by PayID client in the request `Accept` header field along with other required and optional metadata as `message` field in the `SignatureWrapper`.

### Preparing the payment account(s) information message
* Set `payId` to the value of the PayID in the client query. This is a required field in the Verifiable PayID protocol.
* Set `addresses` to the value as described in [PAYID-PROTOCOL][]
* Optionally set `memo` to any additional information.
* `identity` field is optional.
* Optionally set `proofOfControlSignature` to the value as described in [PAYID-PROTOCOL][].

### Preparing SignatureWrapper message
* Set `messageType` to `PaymentInformation`.
* Set `message` to the value of the payment account(s) information message as generated above.
* Set `publicKeyType` to one of the values described in the Section above.
* Set `publicKeyData` to the data corresponding to the value in `publicKeyType`.
* Set `publicKey` to the value of the public key of the signing endpoint (PayID server.)
* Sign the `message` using the hash algorithm and the signature scheme corresponding to the value in `publicKeyType`
* Set `signature` to the result of the signature operation in the previous step.

Send the signed payment account(s) information message as `PaymentInformation` response to the client.

## Step 3: Parse PaymentInformation Response
  If the PayID server returns a valid response, the response will contain one or more of the fields defined above. The PayID client will then:

* Verify the `publicKey` using the information in the `publicKeyType` and `publicKeyData` in the response.
* Verify the signature retrieved from the `signature` field using the public key verified in the previous step.
* Retrieve payment account(s) information message from the `message` field of the `PaymentInformation` Response.

All the verification steps MUST pass. The PayID client proceeds to the next step only if the previous step passes, otherwise it generates the relevant Error message (//TBD).

# Verifiable PayID Protocol for Non-Custodial Wallets and Exchanges

Pre-step at PayID owner's (non-custodial wallet's customer) device locally.
  For each `payment-network` and `environment` as described in [PAYID-PROTOCOL][] that the PayID owner has a payment address for, generate the following payment account(s) information message
  * Set `payId` to the value of the PayID. This is a required field in the Verifiable PayID protocol.
  * Set `addresses` to the value as described in [PAYID-PROTOCOL][]
  * Optionally set `memo` to any additional information.
  * `identity` field is TBD.
  * `proofOfControlSignature` is optional described in [PAYID-PROTOCOL][] and is not required in this case.

For each payment account(s) information message, prepare `SignatureWrapper` message
  * Set `messageType` to `PaymentInformation`.
  * Set `message` to the value of payment account(s) information message as generated above.
  * Set `publicKeyType` to one of the values described in Section X.
  * Set `publicKeyData` to the data corresponding to the value in `publicKeyType`.
  * Set `publicKey` to the value of the public key of the signing endpoint (PayID server.)
  * Sign the `message` using the hash algorithm and the signature scheme corresponding to the value in `publicKeyType`
  * Set `signature` to the result of the signature operation in the previous step.

This signed payment account(s) information message is then securely transferred to the non-custodial PayID server and stored by the PayID server as a `PaymentInformation` resource.

## Discussion Section on distributing PayID owner's keys
  In this subsection, we discuss potential ways to distribute the keys of the PayID owner used to sign the message. Once we reach a consensus, it will be added to the relevant sections of this document and this subsection will be removed. Following are the two possible approaches:

### identity field in payment account(s) information message
 The following table enumerates the possible ways to share the public key of PayID owner using `identity` field.

 | identity                                   | Description               
|--------------------------------------------|---------------------------------
| Global Identifier (GiD) [Gid][]                   | digital identifier              
| Human Universally Unique Identifier (Human UUID) [HUUID][] | digital identifier              
| Digital Identifier (DID) [DID][]                  | digital identifier              
| Certificate                                | attested certificate that associates digital identifier to PayID and public key
| URL                                        | URL for secure retrieval of public key of the PayID owner
| Public key                                 | out-of-band pre-shared public key between PayID client and PayID owner

  * Digital identifier: A global digital identifier that uniquely associates the `PayID owner's identity` as defined by the identifier (GiD, HUID, DID, etc.) to the `PayID` and `public key`. The PayID client can then verify the `public key` using the digital identifier. This could be a direct retrieval of the corresponding `public key` from a digital identity service provider if PayID is a part of that digital identifier.

  * Certificate: An attested certificate that associates digital identifier such as GiD, Human UUID, DID, etc. to the `PayID` and `public key`.

  * URL: A URL for secure retrieval of `public key` of the PayID owner.

  * Pre-shared public Key: The public key that has been pre-shared out-of-band between the PayID client and PayID owner.

### Embed the public key of PayID owner in the PayID
  PayID [PAYID-URI][] could support non-custodial systems with a fairly simple extension to the protocol to run non-custodial PayID servers that could not be hacked or tricked into sending money to the wrong place. The idea is to reserve the hostname `pkh` for `public key hashes` and support a PayID format like `public_key_hash`$pkh.provider.domain. PayID client implementations would require that any `PaymentInformation` resource that resulted from the PayID of that form be signed with the `private key` corresponding to that `public key hash`. So only a `PaymentInformation` signed by the owner of the PayID would work.

  The caveat is that the PayID format is not human-readable anymore. The solution is simple: the non-custodial wallets and exchanges would provide a non-human-readable PayID of the form `public_key_hash`$pkh.provider.domain, but the customers may get a human-readable PayID from another trusted service providers (say from their email provider) that maps to the non-human-readable PayID they got from their non-custodial service-provider. Non-custodial service-providers could even automate this process by allowing the user to choose a mapping provider.

  // Details TBD

## Step 1: Preparing HTTP Request to PayID URL using HTTP POST Method
  Same as in the previous section.

## Step 2: Preparing PaymentInformation Response

  The PayID server MUST parse the request body. The protocol does not provide specification on how the PayID server MAY use this information.

  If the PayID server were to proceed, the PayID server retrieves the pre-signed `PaymentInformation` response to the PayID client.

## Step 3: Parsing the PaymentInformation Response
   The PayID client follows the same verification steps as in the previous section. Details to be decided based on `identity` solution.

# Example Use of the Verifiable PayID Protocol
   This section shows sample use of the Verifiable PayID protocol in several hypothetical scenarios.

## Verifiable PayID Protocol by a Custodial Wallet as PayID server
   Suppose Alice (sender) wishes to send a friend Bob (PayID owner) some XRP from a web-based wallet provider (PayID client) that Alice has an account on. Alice would log-in to the wallet provider and enter Bob's PayID (say, `bob$receiver.example.com`) into the wallet UI to start the payment.

   The Wallet application would first discover the PayID URL for the PayID service-provider using one of the mechanisms described in PayID discovery protocol [PAYID-DISCOVERY][].

   The Wallet application would then issue an HTTPS POST request:

     POST /users/bob/payment-setup-details HTTP/1.1
     Host: www.receiver.example.com
     Accept: application/xrpl-testnet+json
     PayID-version: 1.0

     {
      "identity": "TBD",
      "memo": "Any additional required information"
     }

   Bob's wallet who is a custodial PayID server wallet might respond like this:

     HTTP/1.1 200 OK
     Content-Type: application/json
     Content-Length: 403
     PayID-version: 1.0
     Cache-Control: max-age=0
     Server: Apache/1.3.11
    {
    "messageType" : "PaymentInformation",
    "message" :
     {
       "payId" : "bob$receiver.example.com",
       "addresses" :
       [
         {  
           "paymentNetwork" : "xrpl",
           "environment" : "testnet",
           "addressDetailsType" : "CryptoAddressDetails",
           "addressDetails" : {
           		"address" : "XTVQWr6BhgBLW2jbFyqqufgq8T9eN7KresB684ZSHKQ3oDth"
           	}
          }
       	],
        "memo" : "Additional optional Information",
        "proofOfControlSignature" :
        {
          "publicKey" : "sdkfhjasdvkakjnasdv",
          "payId" : "bob$receiver.example.com",
          "hashAlgorithm" : "SHA512",
          "signature" : "9743b52063cd84097a65d1633f5c74f5"
        }
     }
    "publicKeyType" : "X509+SHA512",
    "publicKeyData": [],
    "publicKey" : "00:c9:22:69:31:8a:d6:6c:ea:da:c3:7f:2c:ac:a5:af:c0:02:ea:81:cb:65:b9:fd:0c:6d:46:5b:c9:1e:9d:3b:ef...",
    "signature" : "8b:c3:ed:d1:9d:39:6f:af:40:72:bd:1e:18:5e:30:54:23:35..."
    }

  In the above example we see that Bob's custodial PayID server wallet returned a signed X-Address on XRPL testnet identified by PayID `bob$receiver.example.com`. This is because Alice's wallet asked for XRPL and testnet payment accounts corresponding to the PayID in the `Accept` header.

  Alice's Wallet MAY then use the payment account information to make payments.

## Verifiable PayID Protocol by a Non-Custodial Wallet as PayID Server
  Consider the same scenario as above.

  Bob's wallet who is a non-custodial PayID server might respond like this:

     HTTP/1.1 200 OK
     Content-Type: application/json
     Content-Length: 403
     PayID-version: 1.0
     Cache-Control: max-age=0
     Server: Apache/1.3.11
    {
    "messageType" : "PaymentInformation",
    "message" :
     {
       "payId" : "bob$receiver.example.com",
       "addresses" :
       [
         {  
           "paymentNetwork" : "xrpl",
           "environment" : "testnet",
           "addressDetailsType" : "CryptoAddressDetails",
           "addressDetails" : {
              "address" : "XTVQWr6BhgBLW2jbFyqqufgq8T9eN7KresB684ZSHKQ3oDth"
            }
          }
        ],
        "memo" : "Additional optional Information",
        "identity" : "TBD",
     }
    "signature" : "TBD"
    }

 In the above example, the `PaymentInformation` resource is a pre-signed message with the off-ledger private keys of the PayID owner Bob. Bob's non-custodial wallet retrieves this response and sends it to the PayID client.

//TODO Add example for PayID owner's public key embedded in PayID.

# Security Considerations
  This security considerations section only considers verifiable PayID clients and servers bound to implementations as defined in this document.

  The security guarantees mentioned in [PAYID-PROTOCOL][] applies to the Verifiable PayID protocol. In this section, we discuss a security model for the Verifiable PayID protocol.  

## Fully-Malicious Adversary Model for PayID Client Wallet and Custodial Wallets and Exchanges as PayID Servers

  While the Verifiable PayID protocol operates between a PayID client and a PayID server, there are actually four parties to any payment. The other two parties are the sender of the payment whose funds are being transferred and the PayID owner or the receiver of the payment who the sender wishes to pay.

  In the current security model, there is necessarily some existing trust between the sender and the sender's wallet. The sender's wallet is holding the sender's private keys and consequently their funds before the payment is made. Similarly, there is necessarily some existing trust between the receiver and their custodial wallet since the receiver has directed that the custodial wallet receive their funds.

  The Verifiable PayID protocol provides a stronger security guarantee: The ideal scenario that we strive for is that the sender should be able to hold the PayID client wallet legally accountable if the institution provably mishandles their funds. Similarly, the PayID owner/receiver should be able to hold the PayID server wallet legally accountable if their funds are mishandled. However, this mechanism requires that it be possible for either wallet to establish that it acted properly and that the other wallet acted improperly.

  Of course, the preferred outcome of any payment is that nothing goes wrong and both the sender and PayID owner/receiver of the payment are satisfied that the payment took place as agreed. A less desirable outcome is that the payment cannot take place for some reason and the sender still has their money and understands why the payment cannot take place.

  While the protocol cannot possibly prevent the PayID client wallet from sending the funds to the wrong address or the PayID server wallet from receiving the funds but refusing to release them to the PayID owner/receiver, it is vital that the institutions not be able to plausibly blame each other for a failure where the sender has been debited but the PayID client/wallet has not been credited.

  Accordingly, the security model of the Verifiable PayID protocol permits four acceptable outcomes:

  1. The payment succeeds, the sender is debited, and the PayID owner/receiver is credited.
  2. The payment fails, the sender is not debited, and the PayID owner/receiver is not credited.
  3. The payment fails, the sender is debited, the PayID owner/receiver is not credited, and the sender can show that the PayID client wallet did not follow the protocol.
  4. The payment fails, the sender is debited, the PayID owner/receiver is not credited, and the sender can show the receiver that their PayID server wallet did not follow the protocol.

  Again, the protocol cannot possibly prevent outcomes 3 or 4 because the PayID client wallet can always send the money to the wrong address and the PayID server wallet can always refuse to credit the PayID owner/receiver. It is, however, critical that the PayID client and PayID server wallets not need to trust each other to ensure that one of these four outcomes occurs and that they cannot point blame at each other.

### Cryptographic Proofs

//TODO

## Fully Compromisable Custodial PayID Server Wallet (hot/always online systems): Adding another Layer of Security.

  The Verifiable PayID protocol's security model assumes that the online servers can be physically or remotely compromised by an adversary. These are the most attractive attack vectors. There is sufficient evidence that hot/always online systems are more vulnerable.

  There are multiple cryptographic operations that the PayID server wallet MUST perform to establish secure communication channels, to generate signed messages as verifiable cryptographic proofs, etc.

  These operations have very different security requirements and compromising the cryptographic keys required for these operations have different security implications.

  * High-risk impersonation attack to steal funds: If the PayID server wallet’s cryptographic keys used to sign `PaymentInformation` resource are compromised, an attacker may impersonate as the PayID server wallet and sign malicious mappings (‘Receiver's PayID → attacker controlled payment address’) to send to the PayID client wallet. This may lead to indirection of funds by the PayID client wallet to the attacker-controlled address. Therefore, it is extremely important to keep these keys safe offline.

  * Lower-risk impersonation attacks: An attacker can never steal funds if only cryptographic keys used to establish secure network connection between the PayID client wallet and PayID server wallet are compromised.

  These differing security implications warrant a separation of generating cryptographically signed proofs and storing the cryptographic keys used to perform these two operations. Some observations that inform us on how we can deal with this are that:
  * Generating the cryptographic signatures on `PaymentInformation` resource need not be an online operation. This can be performed offline in a safe, cold system with a separate set of keys,

  * All other cryptographic operations need to be performed online, such as signing any additional information needed to fulfill the payment or establishing secure communication channels.

  Based on these observations, we propose to maintain two separate systems (hot and cold) and two separate sets of cryptographic keys for the two operations.

  We propose that the PayID client wallet and PayID server wallet SHOULD follow best practices to reduce the attack surface and be more robust.

  //TODO Key Management sub-section.

## Security Model for Non-Custodial PayID Server Wallets

  In the current security model, non-custodial wallets do not store their customers’ keys on their servers. The customers hold their private keys on their device. There is a no trust requirement between the service provided by the non-custodial wallets and the customers of this service. Since the customers hold the private keys:
  * The wallets are not liable for any consequences coming from the lost, compromised or hacked private keys of the customers.
  * The non-custodial wallets do not require their customers to trust their servers in case wallets servers go malicious or are compromised.

  The Verifiable PayID protocol preserves this trust model. For non-custodial PayID server wallets, this means that

  * On the receiving side of the payment (as a PayID server) non-custodial wallets have no liability on their end for providing `PaymentInformation`, that is, the `PayID --> Payment Address` mappings for their customers that are signed with the private key of the non-custodial PayID server wallet. The PayID owners or the customers can generate this signed mapping with their own off-ledger private key locally on their app/device. The PayID client can easily verify this signature based on the trust relationship between the sender of the payment (PayID client wallet’s customer) and the receiver (non-custodial PayID server's wallet). The non-custodial PayID server wallet has no role whatsoever. This eliminates any risk of the non-custodial PayID server wallet having lost their private keys, going malicious, or getting hacked, etc. because if this happens then their customers might lose funds.

# Privacy Considerations
  All privacy guarantees in the Privacy Considerations section of [PAYID-PROTOCOL][] apply to the Verifiable PayID protocol and further address some of the privacy issues mentioned in [PAYID-PROTOCOL][].

## Access Control
  PayID protocol MUST not be used to provide `PaymentInformation` or any other resources corresponding to a PayID unless providing that data via PayID protocol by the relevant PayID server was explicitly authorized by the PayID owner. If the PayID owner wishes to limit access to information, PayID servers MAY provide an interface by which PayID owners can select which information is exposed through the PayID server interface. For example, PayID servers MAY allow PayID owners to mark certain data as `public` and then utilize that marking as a means of determining what information to expose via PayID protocol. The PayID servers MAY also allow PayID owners to provide a whitelist of users who are authorized to access the specific information. In such a case, the PayID server MUST authenticate the PayID client. The additional `identity` field in the PayID client query request allows for this.


# IANA Considerations
//TODO

# Acknowledgments
//TODO
