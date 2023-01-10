package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.workflow.ReceiveMembershipAttestationStep
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.core.workflow.publishTransactionHandler
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class PublishMembershipAttestationFlowHandler(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(ReceiveMembershipAttestationStep)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        return publishTransactionHandler(session, progressTrackerStep = ReceiveMembershipAttestationStep)
    }

    @InitiatedBy(PublishMembershipAttestationFlow.Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object HandlePublishedMembershipAttestationStep : Step("Handling membership attestation publication.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(HandlePublishedMembershipAttestationStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(HandlePublishedMembershipAttestationStep)
            return subFlow(
                PublishMembershipAttestationFlowHandler(
                    session,
                    HandlePublishedMembershipAttestationStep.childProgressTracker()
                )
            )
        }
    }
}
