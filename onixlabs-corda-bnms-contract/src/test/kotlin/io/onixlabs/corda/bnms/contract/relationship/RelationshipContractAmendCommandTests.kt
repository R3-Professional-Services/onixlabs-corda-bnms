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
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipContractAmendCommandTests : ContractTest() {

    @Test
    fun `On relationship amending, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val amendedRelationship1 = issuedRelationship1.getNextOutput()
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, amendedRelationship1)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship amending, only one relationship state must be consumed`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                input(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship amending, only one relationship state must be created`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = RELATIONSHIP
                val amendedRelationship1 = issuedRelationship1.getNextOutput()
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, amendedRelationship1)
                output(RelationshipContract.ID, issuedRelationship2)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship amending, the network and linear ID of the relationship must not change`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val amendedRelationship1 = issuedRelationship1.getNextOutput().withWrongNetwork()
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, amendedRelationship1)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_CHANGES)
            }
        }
    }

    @Test
    fun `On relationship amending, the previous state reference must be equal to the input state reference`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val amendedRelationship1 = issuedRelationship1.getNextOutput().withWrongRef()
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, amendedRelationship1)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_PREVIOUS_STATE_REF)
            }
        }
    }

    @Test
    fun `On relationship amending, all participants must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val amendedRelationship1 = issuedRelationship1.getNextOutput()
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, amendedRelationship1)
                command(keysOf(IDENTITY_B), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
