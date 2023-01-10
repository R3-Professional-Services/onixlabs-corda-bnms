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
import io.onixlabs.corda.bnms.contract.relationship.RelationshipContract
import io.onixlabs.corda.bnms.workflow.*
import io.onixlabs.corda.core.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class AmendRelationshipFlow(
    private val oldRelationship: StateAndRef<Relationship>,
    private val newRelationship: Relationship,
    private val sessions: Set<FlowSession>,
    private val checkMembership: Boolean = false,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(
            InitializeFlowStep,
            SendCheckMembershipInstructionStep,
            CheckMembershipStep,
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
        checkSufficientSessionsForContractStates(sessions, newRelationship)
        checkMembership(checkMembership, newRelationship, sessions)

        val transaction = buildTransaction(oldRelationship.state.notary) {
            addAmendedRelationship(oldRelationship, newRelationship)
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
        private val oldRelationship: StateAndRef<Relationship>,
        private val newRelationship: Relationship,
        private val checkMembership: Boolean = false
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AmendRelationshipStep : Step("Amending relationship.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(AmendRelationshipStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AmendRelationshipStep)
            val sessions = initiateFlows(emptyList(), newRelationship, oldRelationship.state.data)

            return subFlow(
                AmendRelationshipFlow(
                    oldRelationship,
                    newRelationship,
                    sessions,
                    checkMembership,
                    AmendRelationshipStep.childProgressTracker()
                )
            )
        }
    }
}
