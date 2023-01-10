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
import io.onixlabs.corda.bnms.workflow.checkRevocationLockExists
import io.onixlabs.corda.core.workflow.*
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class RevokeRelationshipFlowHandler(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(
            SignTransactionStep,
            ReceiveStatesToRecordStep,
            RecordFinalizedTransactionStep
        )
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val transaction = collectSignaturesHandler(session) {
            val relationshipStateRef = it.tx.inputs.singleOrNull() ?: throw FlowException(
                "Failed to obtain a single state reference from the transaction."
            )

            val relationship = serviceHub.toStateAndRef<Relationship>(relationshipStateRef)
            checkRevocationLockExists(ourIdentity, relationship.state.data)
        }

        return finalizeTransactionHandler(session, transaction?.id, StatesToRecord.ONLY_RELEVANT)
    }

    @InitiatedBy(RevokeRelationshipFlow.Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object HandleRevokeRelationshipStep : Step("Handling relationship revocation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(HandleRevokeRelationshipStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(HandleRevokeRelationshipStep)
            return subFlow(RevokeRelationshipFlowHandler(session, HandleRevokeRelationshipStep.childProgressTracker()))
        }
    }
}
