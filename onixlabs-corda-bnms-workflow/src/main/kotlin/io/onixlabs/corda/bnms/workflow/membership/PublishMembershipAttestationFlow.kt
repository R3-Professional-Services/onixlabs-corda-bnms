package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.workflow.SendMembershipAttestationStep
import io.onixlabs.corda.core.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

class PublishMembershipAttestationFlow(
    private val attestation: StateAndRef<MembershipAttestation>,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(InitializeFlowStep, SendMembershipAttestationStep)

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        val transaction = findTransaction(attestation)
        return publishTransaction(transaction, sessions, SendMembershipAttestationStep)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val attestation: StateAndRef<MembershipAttestation>,
        private val observers: Set<Party>
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object PublishMembershipAttestationStep : ProgressTracker.Step("Publishing membership attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(PublishMembershipAttestationStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(PublishMembershipAttestationStep)
            return subFlow(
                PublishMembershipAttestationFlow(
                    attestation,
                    initiateFlows(observers),
                    PublishMembershipAttestationStep.childProgressTracker()
                )
            )
        }
    }
}
