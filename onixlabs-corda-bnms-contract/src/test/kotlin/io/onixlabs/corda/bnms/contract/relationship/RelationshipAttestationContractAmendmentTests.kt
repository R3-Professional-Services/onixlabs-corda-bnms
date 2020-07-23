package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.*
import net.corda.core.contracts.UniqueIdentifier
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipAttestationContractAmendmentTests : ContractTest() {

    @Test
    fun `On relationship attestation amendment, the transaction must include the Amend command (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                fails()
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, the transaction must include the Amend command (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                fails()
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only one state must be consumed (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only one state must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only one state must be created (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only one state must be created (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only one relationship state must be referenced (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(RelationshipContract.ID, relationship.state.data)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only one relationship state must be referenced (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(RelationshipContract.ID, relationship.state.data)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, the attestation pointer must point to the referenced relationship state (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                val oldPointer = attestation.pointer
                val newPointer = AttestationPointer(UniqueIdentifier(), oldPointer.stateRef, oldPointer.type)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation.copy(pointer = newPointer))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, the attestation pointer must point to the referenced relationship state (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                val oldPointer = attestation.pointer
                val newPointer = AttestationPointer(UniqueIdentifier(), oldPointer.stateRef, oldPointer.type)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation.copy(pointer = newPointer))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, all relationship participants must be included (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation.copy(attestees = setOf(IDENTITY_B.party)))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_PARTICIPANTS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, all relationship participants must be included (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation.copy(attestees = setOf(IDENTITY_B.party)))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_PARTICIPANTS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, the attestation and relationship network hash must be equal (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation.copy(network = INVALID_NETWORK))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, the attestation and relationship network hash must be equal (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation.copy(network = INVALID_NETWORK))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only the attestor must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_B), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only the attestor must sign the transaction  (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_B), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}