package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.workflow.ReceiveMembershipStep
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.core.workflow.publishTransactionHandler
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class PublishMembershipFlowHandler(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(ReceiveMembershipStep)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        return publishTransactionHandler(session, progressTrackerStep = ReceiveMembershipStep)
    }

    @InitiatedBy(PublishMembershipFlow.Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object HandlePublishedMembershipTransactionStep : Step("Handling membership publication.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(HandlePublishedMembershipTransactionStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(HandlePublishedMembershipTransactionStep)
            return subFlow(
                PublishMembershipFlowHandler(
                    session,
                    HandlePublishedMembershipTransactionStep.childProgressTracker()
                )
            )
        }
    }
}
