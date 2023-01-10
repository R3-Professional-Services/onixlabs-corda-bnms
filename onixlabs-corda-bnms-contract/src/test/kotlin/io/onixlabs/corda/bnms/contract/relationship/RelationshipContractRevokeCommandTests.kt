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

import io.onixlabs.corda.bnms.contract.ContractTest
import io.onixlabs.corda.bnms.contract.IDENTITY_A
import io.onixlabs.corda.bnms.contract.IDENTITY_B
import io.onixlabs.corda.bnms.contract.RELATIONSHIP
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipContractRevokeCommandTests : ContractTest() {

    @Test
    fun `On relationship revoking, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship revoking, only one relationship state must be consumed`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                input(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship revoking, zero relationship states must be created`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = RELATIONSHIP
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, issuedRelationship2)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship revoking, all participants must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                command(keysOf(IDENTITY_B), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
