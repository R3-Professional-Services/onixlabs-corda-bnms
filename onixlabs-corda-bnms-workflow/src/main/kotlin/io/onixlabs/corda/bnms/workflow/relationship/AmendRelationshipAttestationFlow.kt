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
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.workflow.addAmendedRelationshipAttestation
import io.onixlabs.corda.bnms.workflow.findRelationshipForAttestation
import io.onixlabs.corda.core.workflow.*
import io.onixlabs.corda.identityframework.workflow.checkAttestationExistsForAmendment
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class AmendRelationshipAttestationFlow(
    private val oldAttestation: StateAndRef<RelationshipAttestation>,
    private val newAttestation: RelationshipAttestation,
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
        checkSufficientSessionsForContractStates(sessions, newAttestation, oldAttestation.state.data)
        checkAttestationExistsForAmendment(newAttestation)

        val relationship = findRelationshipForAttestation(newAttestation).referenced()

        val transaction = buildTransaction(oldAttestation.state.notary) {
            addAmendedRelationshipAttestation(oldAttestation, newAttestation, relationship)
        }

        verifyTransaction(transaction)
        val signedTransaction = signTransaction(transaction)
        return finalizeTransaction(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val oldAttestation: StateAndRef<RelationshipAttestation>,
        private val newAttestation: RelationshipAttestation
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AmendRelationshipAttestationStep : Step("Amending relationship attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(AmendRelationshipAttestationStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AmendRelationshipAttestationStep)
            val sessions = initiateFlows(emptyList(), newAttestation, oldAttestation.state.data)

            return subFlow(
                AmendRelationshipAttestationFlow(
                    oldAttestation,
                    newAttestation,
                    sessions,
                    AmendRelationshipAttestationStep.childProgressTracker()
                )
            )
        }
    }
}
