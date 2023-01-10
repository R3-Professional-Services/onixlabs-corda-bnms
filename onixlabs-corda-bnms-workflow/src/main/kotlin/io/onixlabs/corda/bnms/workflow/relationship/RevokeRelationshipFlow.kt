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

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.addRevokedRelationship
import io.onixlabs.corda.core.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class RevokeRelationshipFlow(
    private val relationship: StateAndRef<Relationship>,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(
            InitializeFlowStep,
            BuildTransactionStep,
            VerifyTransactionStep,
            SignTransactionStep,
            CollectTransactionSignaturesStep,
            SendStatesToRecordStep,
            FinalizeTransactionStep
        )

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        checkSufficientSessionsForContractStates(sessions, relationship.state.data)

        val transaction = buildTransaction(relationship.state.notary) {
            addRevokedRelationship(relationship)
        }

        verifyTransaction(transaction)
        val partiallySignedTransaction = signTransaction(transaction)
        val fullySignedTransaction = collectSignatures(partiallySignedTransaction, sessions)
        return finalizeTransaction(fullySignedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val relationship: StateAndRef<Relationship>
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object RevokeRelationshipStep : Step("Revoking relationship.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(RevokeRelationshipStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(RevokeRelationshipStep)
            val sessions = initiateFlows(emptyList(), relationship.state.data)

            return subFlow(
                RevokeRelationshipFlow(
                    relationship,
                    sessions,
                    RevokeRelationshipStep.childProgressTracker()
                )
            )
        }
    }
}
