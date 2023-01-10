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

import io.onixlabs.corda.bnms.contract.ContractTest
import io.onixlabs.corda.bnms.contract.IDENTITY_A
import io.onixlabs.corda.bnms.contract.RELATIONSHIP
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationContract
import io.onixlabs.corda.bnms.contract.relationship.reject
import io.onixlabs.corda.bnms.contract.withWrongNetwork
import io.onixlabs.corda.identityframework.contract.attestations.AttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipAttestationContractAmendCommandTests : ContractTest() {

    @Test
    fun `On relationship attestation amending, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedAttestation1 = attestRelationship(issuedRelationship1, IDENTITY_A.party)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(RelationshipAttestationContract.ID, amendedAttestation1)
                reference(issuedRelationship1.ref)
                fails()
                command(keysOf(IDENTITY_A), AttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation amending, only one relationship state must be referenced`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                val issuedAttestation1 = attestRelationship(issuedRelationship1, IDENTITY_A.party)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(RelationshipAttestationContract.ID, amendedAttestation1)
                reference(issuedRelationship1.ref)
                reference(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A), AttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation amending, the attestation pointer must point to the referenced relationship state`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                val issuedAttestation1 = attestRelationship(issuedRelationship1, IDENTITY_A.party)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(RelationshipAttestationContract.ID, amendedAttestation1)
                reference(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A), AttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On relationship attestation amending, the attestation network must be equal to the relationship network`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedAttestation1 = attestRelationship(issuedRelationship1, IDENTITY_A.party)
                val amendedAttestation1 = issuedAttestation1.reject().withWrongNetwork()
                input(issuedAttestation1.ref)
                output(RelationshipAttestationContract.ID, amendedAttestation1)
                reference(issuedRelationship1.ref)
                command(keysOf(IDENTITY_A), AttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_NETWORK)
            }
        }
    }
}
