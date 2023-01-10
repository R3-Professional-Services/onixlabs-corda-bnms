package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.SendMembershipStep
import io.onixlabs.corda.core.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class PublishMembershipFlow(
    private val membership: StateAndRef<Membership>,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(
            InitializeFlowStep,
            SendMembershipStep
        )

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        val transaction = findTransaction(membership)
        return publishTransaction(transaction, sessions, SendMembershipStep)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val membership: StateAndRef<Membership>,
        private val observers: Set<Party>
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object PublishMembershipTransactionStep : Step("Publishing membership.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(PublishMembershipTransactionStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(PublishMembershipTransactionStep)
            return subFlow(
                PublishMembershipFlow(
                    membership,
                    initiateFlows(observers),
                    PublishMembershipTransactionStep.childProgressTracker()
                )
            )
        }
    }
}
