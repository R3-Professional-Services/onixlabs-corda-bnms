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

package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.workflow.addIssuedMembershipAttestation
import io.onixlabs.corda.bnms.workflow.checkMembershipAttestationExistsForAmendment
import io.onixlabs.corda.bnms.workflow.checkMembershipAttestationExistsForIssuance
import io.onixlabs.corda.bnms.workflow.findMembershipForAttestation
import io.onixlabs.corda.core.workflow.*
import io.onixlabs.corda.identityframework.workflow.checkAttestationExistsForIssuance
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class IssueMembershipAttestationFlow(
    private val attestation: MembershipAttestation,
    private val notary: Party,
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
            SendStatesToRecordStep,
            FinalizeTransactionStep
        )

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        checkSufficientSessionsForContractStates(sessions, attestation)
        checkMembershipAttestationExistsForIssuance(attestation)

        val membership = findMembershipForAttestation(attestation).referenced()

        val transaction = buildTransaction(notary) {
            addIssuedMembershipAttestation(attestation, membership)
        }

        verifyTransaction(transaction)
        val signedTransaction = signTransaction(transaction)
        return finalizeTransaction(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val attestation: MembershipAttestation,
        private val notary: Party? = null,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object IssueMembershipAttestationStep : Step("Issuing membership attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(IssueMembershipAttestationStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(IssueMembershipAttestationStep)
            val sessions = initiateFlows(observers, attestation)

            return subFlow(
                IssueMembershipAttestationFlow(
                    attestation,
                    notary ?: getPreferredNotary(),
                    sessions,
                    IssueMembershipAttestationStep.childProgressTracker()
                )
            )
        }
    }
}
