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

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.reject
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipAttestationFlow
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipFlow
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class IssueRelationshipFlowWithMembershipCheckingTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var relationship: Relationship

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                val membership = Membership(NETWORK, partyA)
                IssueMembershipFlow.Initiator(membership, observers = setOf(partyB))
            }
            .run(nodeB) {
                val membership = it.tx.outRefsOfType<Membership>().single()
                val attestation = membership.reject(partyB)
                IssueMembershipAttestationFlow.Initiator(attestation)
            }
            .run(nodeB) {
                val membership = Membership(NETWORK, partyB)
                IssueMembershipFlow.Initiator(membership, observers = setOf(partyA))
            }
            .run(nodeA) {
                val membership = it.tx.outRefsOfType<Membership>().single()
                val attestation = membership.reject(partyA)
                IssueMembershipAttestationFlow.Initiator(attestation)
            }
            .run(nodeA) {
                relationship = RELATIONSHIP
                IssueRelationshipFlow.Initiator(relationship, checkMembership = true)
            }
            .finally { transaction = it }
    }

    @Test
    fun `IssueRelationshipFlow should be signed by the initiator`() {
        transaction.verifySignaturesExcept(partyB.owningKey)
    }

    @Test
    fun `IssueRelationshipFlow should be signed by all participants`() {
        transaction.verifySignaturesExcept(partyA.owningKey)
    }

    @Test
    fun `IssueRelationshipFlow should record a transaction for all relationship members`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `IssueRelationshipFlow should record a relationship for all relationship members`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedRelationship = recordedTransaction.tx.outputsOfType<Relationship>().singleOrNull()
                    ?: fail("Failed to find a recorded relationship.")

                assertEquals(relationship, recordedRelationship)
            }
        }
    }
}
