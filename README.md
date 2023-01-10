![ONIX Labs](https://raw.githubusercontent.com/onix-labs/onixlabs-website/main/src/assets/images/logo/full/original/original-md.png)

# ONIXLabs Corda BNMS

The ONIXLabs Corda BNMS (Business Network Management System) is a framework designed to provide membership and multi-lateral relationship management services for distributed business networks on Corda. The framework is flexible by design and requires minimal developer implementation into existing Corda applications, allowing fully customizable membership and relationship models which can be tailored to meet specific business and network requirements.

## Prerequisites

The ONIXLabs Corda BNMS utilizes the [ONIXLabs Corda Identity Framework](https://github.com/onix-labs/onixlabs-corda-identity-framework) as an underlying mechanism for creating, distributing and attesting to identity credentials used to define membership, as well as extending the claim and attestation models to create roles, permissions, settings, and membership and relationship attestations.

## Design Goals

### Business Network Interoperability

Corda nodes may require participation in multiple business networks, each with their own standards governing identity and configuration. Without standard APIs for constructiing business networks, there is a high likeliness that different business network implementations will be incompatible.

The ONIXLabs Corda BNMS solves this problem by providing protocol level, configurable standards for membership and multi-lateral relationship management implementations, allowing business networks to scale independently whilst remaining interoperable with one another.

The ONIXLabs Corda BNMS is built on top of the ONIXLabs Corda Identity Framework which not only improves business network interoperability, but also Corda application interoperability. Claims can be shared between applications and distributed across the network, helping to reduce data duplication and inconsistency.

The ONIXLabs Corda BNMS has been designed from the ground up to support centralised business networks that are governed by a business network operator and decentralised business networks that are governed  autonomously by the network members themselves.

### Centralised Business Networks

In cases where business networks are controlled by a central authority, or business network operator, membership and relationships can be managed by the members of the network, or by the business network operator. In this case, every membership or relationship needs to be attested by the business network operator to be considered valid.

This constitutes a full trust model, since the business network operator is explicitly aware of all memberships and relationships within the business network.

### Decentralised Business Networks

Whilst centrally governed networks can be useful in some cases, it might be considered going against the ethos of DLT. The ONIXLabs Corda BNMS does not mandate a central authority and allows business networks to be self governing. Membership and relationships are managed entirely by the members of the network. In this case, every membership or relationship needs to be attested by other members of the network to be considered valid, and is only considered valid by those members who have attested to those states.

This constitutes a partial trust model, with respect to the network as a whole since individual members of the network may not be aware of all other members operating within the same network.

### Data Privacy

Data privacy surrounding member identity is handled by the underlying ONIXLabs Corda Identity Framework. Membership in either centralised or decetralised networks will require either the business network operator or other members of the network to obtain and/or verify identity information. Claims can be issued, distributed and attested independly to membership states, thus allowing verified membership states to be distributed whilst keeping identity information private.

### Dynamic Smart Contract Execution

In certain scenarios, different business networks and groups of participants may wish to execute their smart contract logic under different conditions, and this still needs to be performed within a deterministic environment.

The ONIXLabs Corda BNMS facilitates the ability to perform dynamic smart contract execution through relationships which constitute a multi-lateral agreement and set of rules between counter-parties. Smart contracts can be written to utilize relationships that have been referenced in a transaction and therefore allows smart contract execution to rely on rules which are configured externally.

## Membership Management

Membership is defined as a reference to a business network, complete with configurable identity as well as roles, permissions and settings which provide levels of access and authorisation for utilitsation within the business network. A Corda node may define many relationship states; one per business network. These can be managed by the member or optionally by a network operator.

### Membership Issuance

Membership issuance can only be initiated by the member themselves. This represents a request to join a network and is only considered authorised when the membership state has been attested and accepted, either by the network operator, or by other members of the network. The member is the only participant required to sign a membership issuance transaction.

### Membership Amendment

Membership amendment can be performed by the member themselves, or optionally by the network operator. This represents a change to the member's network credentials and is only considered authorised when the membership state has been attested and accepted, either by the network operator, or by other members of the network. All participants are required to sign a membership amendment transaction.

### Membership Revocation

Membership revocation can be performed by the member themselves, or optionally by the network operator. This represents the exit of a member from a business network.  All participants are required to sign a membership revocation transaction.

## Relationship Management

A relationship is defined as reference to a business network, complete with a multi-lateral agreement defining configurable governance about how individual members of the relationship interact with one another. A Corda node may define many relationship states. These can be managed by the members of the relationship or by the network operator.

### Relationship Issuance

Relationship issuance can be performed by any member of a network, or optionally by the network operator. This represents a request to participate in a multi-lateral working relationship which is only considered authorised when all participants of the relationship have attested to and accepted the relationship state.

When issuing a relationship state, a revocation lock is issued to each participant of the relationship. This is because any member of the relationship is allowed to initiate a revocation transaction and could accidentally or maliciously revoke the relationship for all participants.

All participants of a relationship are required to sign a relationship issuance transaction. When requested to sign, the counter-parties will check whether the relationship has a network operator, and if so will sign the transaction since trust is handled by the network operator. If the relationship does not have a network operator then each counter-party will check their own node for attestations between themselves and the other participants of the relationship state for that network. Provided that attestations exist for all participants of the relationship, the counter-party will sign the transaction.

### Relationship Amendment

Relationship amendment can be performed by any member of a relationship, or optionally by the network operator. This represents a request to amend participation of a multi-lateral working relationship which is only considered authorised when all participants of the relationship have attested to and accepted the relationship state.

All participants of a relationship are required to sign a relationship amendment transaction. When requested to sign, the counter-parties will check whether the relationship has a network operator, and if so will sign the transaction since trust is handled by the network operator. If the relationship does not have a network operator then each counter-party will check their own node for attestations between themselves and the other participants of the relationship state for that network. Provided that attestations exist for all participants of the relationship, the counter-party will sign the transaction.

### Relationship Revocation

Relationship revocation can be performed by any member of a relationship, or optionally by the network operator. This represents closure of a multi-lateral working relationship. Any participant of the relationship may initiate a relationship revocation transaction, however in order for the transaction to succeed, all participants are required to sign.

When requested to sign a relationship revocation transaction, each counter-party node will check the revocation lock on their node for that relationship. If the lock is still in place then the counter-party node will not sign the transaction and revocation will not complete. This is a counter-measure against accidental or malicious revocation of relationship states.

## Attestations

Since members are able to manage membership and relationships themselves, they are essentially able to state their own credentials to the network. This  could have undesirable consequences when executing flows that check membership and relationship credentials, and could allow members to act maliciously.

As a security measure, members are required to attest to membership and relationship states that they want to interact with. Attestations are states that point to specific versions of membership and relationship states and provides a mechanism for members of the network, or the network operator to express their acceptance or rejection of membership or relationship states.

If a membership or relationship state is amended, all previous attestations become invalid because they point to historic versions of the state. In order to interact with a membership or relationship that has been amended, members are required to re-attest to the amended version of the membership or relationship state.

## Membership Attestation

Membership attestation is defined as an acknowledgement of a member, either accepting or rejecting their membership state and network credentials. In order for network members to participate with one another, their membership states must be attested either by other members of the network, or optionally by the network operator.

### Membership Attestation Issuance

Membership attestation issuance can only be performed by the attestor. This represents an accepted or rejected authorisation of a newly issued membership state. Only the attestor is required to sign a membership attestation issuance transaction.

### Membership Attestation Amendment

Membership attestation amendment can only be performed by the attestor. This represents an accepted or rejected authorisation of an amended membership state. Only the attestor is required to sign a membership attestation issuance transaction.

### Membership Attestation Revocation

Membership attestation revocation can only be performed by the attestor. This represents complete revocation of an attestation to a membership state, which can be used when a membership state is revoked. Only the attestor is required to sign a membership attestation issuance transaction.
