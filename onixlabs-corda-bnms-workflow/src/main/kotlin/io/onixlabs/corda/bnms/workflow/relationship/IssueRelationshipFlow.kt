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
import io.onixlabs.corda.bnms.workflow.CheckMembershipStep
import io.onixlabs.corda.bnms.workflow.SendCheckMembershipInstructionStep
import io.onixlabs.corda.bnms.workflow.addIssuedRelationship
import io.onixlabs.corda.bnms.workflow.checkMembership
import io.onixlabs.corda.core.workflow.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class IssueRelationshipFlow(
    private val relationship: Relationship,
    private val notary: Party,
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
        checkSufficientSessionsForContractStates(sessions, relationship)
        checkMembership(checkMembership, relationship, sessions)

        val transaction = buildTransaction(notary) {
            addIssuedRelationship(relationship)
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
        private val relationship: Relationship,
        private val notary: Party? = null,
        private val checkMembership: Boolean = false
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object IssueRelationshipStep : Step("Issuing relationship.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(IssueRelationshipStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(IssueRelationshipStep)
            val sessions = initiateFlows(emptyList(), relationship)

            return subFlow(
                IssueRelationshipFlow(
                    relationship,
                    notary ?: getPreferredNotary(),
                    sessions,
                    checkMembership,
                    IssueRelationshipStep.childProgressTracker()
                )
            )
        }
    }
}
