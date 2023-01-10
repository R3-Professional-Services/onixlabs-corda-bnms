/*
 * Copyright 2020-2022 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.contract.attestation

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationContract
import io.onixlabs.corda.bnms.contract.relationship.accept
import io.onixlabs.corda.identityframework.contract.attestations.AttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipAttestationContractIssueCommandTests : ContractTest() {

    @Test
    fun `On relationship attestation issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedAttestation1 = issuedRelationship1.accept(IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, issuedAttestation1)
                reference(issuedRelationship1.ref)
                fails()
                command(keysOf(IDENTITY_A), AttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation issuing, only one relationship state must be referenced`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                val issuedAttestation1 = issuedRelationship1.accept(IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, issuedAttestation1)
                reference(issuedRelationship1.ref)
                reference(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A), AttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation issuing, the attestation pointer must point to the referenced relationship state`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                val issuedAttestation1 = issuedRelationship1.accept(IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, issuedAttestation1)
                reference(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A), AttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On relationship attestation issuing, the attestation network must be equal to the relationship network`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedAttestation1 = issuedRelationship1.accept(IDENTITY_A.party).withWrongNetwork()
                output(RelationshipAttestationContract.ID, issuedAttestation1)
                reference(issuedRelationship1.ref)
                command(keysOf(IDENTITY_A), AttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_NETWORK)
            }
        }
    }

    @Test
    fun `On relationship attestation issuing, the attestor must be a participant of the referenced relationship state`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedAttestation1 = issuedRelationship1.accept(IDENTITY_C.party)
                output(RelationshipAttestationContract.ID, issuedAttestation1)
                reference(issuedRelationship1.ref)
                command(keysOf(IDENTITY_C), AttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_ATTESTOR)
            }
        }
    }
}