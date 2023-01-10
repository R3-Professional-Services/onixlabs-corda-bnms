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

package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipContractIssueCommandTests : ContractTest() {

    @Test
    fun `On relationship issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = RELATIONSHIP
                val revocationLocks = issuedRelationship1.createRevocationLocks()
                output(RelationshipContract.ID, issuedRelationship1)
                revocationLocks.forEach { output(RevocationLockContract.ID, it) }
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Lock)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship issuing, zero relationship states must be consumed`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = RELATIONSHIP
                input(RelationshipContract.ID, issuedRelationship1)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship issuing, only one relationship state must be created`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = RELATIONSHIP
                val issuedRelationship2 = RELATIONSHIP
                output(RelationshipContract.ID, issuedRelationship1)
                output(RelationshipContract.ID, issuedRelationship2)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship issuing, revocation locks must be issued for all relationship members`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = RELATIONSHIP
                output(RelationshipContract.ID, issuedRelationship1)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_LOCKS)
            }
        }
    }

    @Test
    fun `On relationship issuing, all revocation locks must point to the relationship state`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = RELATIONSHIP
                val revocationLocks = issuedRelationship1.withWrongLocks()
                output(RelationshipContract.ID, issuedRelationship1)
                revocationLocks.forEach { output(RevocationLockContract.ID, it) }
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Lock)
                failsWith(RelationshipContract.Issue.CONTRACT_REVOCATION_LOCK_POINTERS)
            }
        }
    }

    @Test
    fun `On relationship issuing, the previous state reference of the created relationship state must be null`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = RELATIONSHIP.withWrongRef()
                val revocationLocks = issuedRelationship1.createRevocationLocks()
                output(RelationshipContract.ID, issuedRelationship1)
                revocationLocks.forEach { output(RevocationLockContract.ID, it) }
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Lock)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_PREVIOUS_STATE_REF)
            }
        }
    }

    @Test
    fun `On relationship issuing, all participants must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = RELATIONSHIP
                val revocationLocks = issuedRelationship1.createRevocationLocks()
                output(RelationshipContract.ID, issuedRelationship1)
                revocationLocks.forEach { output(RevocationLockContract.ID, it) }
                command(keysOf(IDENTITY_B), RelationshipContract.Issue)
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Lock)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
