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

package io.onixlabs.corda.bnms.workflow.relationship

import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import io.onixlabs.corda.bnms.workflow.revocation.UnlockRevocationLockFlow
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeRelationshipFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        val issuanceTransaction = Pipeline
            .create(network)
            .run(nodeA) { IssueRelationshipFlow.Initiator(RELATIONSHIP, checkMembership = false) }
            .result

        Pipeline
            .create(network)
            .run(nodeA) {
                val lock = issuanceTransaction
                    .tx.outRefsOfType<RevocationLock<Relationship>>()
                    .single { it.state.data.owner == partyA }

                UnlockRevocationLockFlow(lock)
            }
            .run(nodeB) {
                val lock = issuanceTransaction
                    .tx.outRefsOfType<RevocationLock<Relationship>>()
                    .single { it.state.data.owner == partyB }

                UnlockRevocationLockFlow(lock)
            }
            .run(nodeB) {
                val relationship = issuanceTransaction.tx.outRefsOfType<Relationship>().single()
                RevokeRelationshipFlow.Initiator(relationship)
            }
            .finally { transaction = it }
    }

    @Test
    fun `RevokeRelationshipFlow should be signed by the initiator`() {
        transaction.verifySignaturesExcept(partyA.owningKey)
    }

    @Test
    fun `RevokeRelationshipFlow should be signed by all participants`() {
        transaction.verifySignaturesExcept(partyB.owningKey)
    }

    @Test
    fun `RevokeRelationshipFlow should record a transaction for all relationship members`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }
}
