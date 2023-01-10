package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema
import io.onixlabs.corda.bnms.workflow.ReceiveMembershipAndAttestationsStep
import io.onixlabs.corda.bnms.workflow.SendMembershipAndAttestationsStep
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.core.workflow.InitializeFlowStep
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.identityframework.workflow.attestations.PublishAttestationFlow
import io.onixlabs.corda.identityframework.workflow.attestations.PublishAttestationFlowHandler
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.unwrap

class SynchronizeMembershipFlowHandler(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>?>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(
            InitializeFlowStep,
            ReceiveMembershipAndAttestationsStep,
            SendMembershipAndAttestationsStep
        )
    }

    @Suspendable
    override fun call(): Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>? {
        currentStep(InitializeFlowStep)
        val network = session.receive<Network>().unwrap { it }
        val membership = serviceHub.vaultServiceFor<Membership>().singleOrNull {
            expression(MembershipSchema.MembershipEntity::holder equalTo ourIdentity)
            expression(MembershipSchema.MembershipEntity::networkHash equalTo network.hash.toString())
        }

        session.send(membership != null)

        if (membership != null) {
            val attestations = serviceHub.vaultServiceFor<MembershipAttestation>().filter {
                expression(MembershipAttestationEntity::holder equalTo ourIdentity)
                expression(MembershipAttestationEntity::networkHash equalTo network.hash.toString())
            }.toList()

            currentStep(ReceiveMembershipAndAttestationsStep)
            val theirMembership = receiveTheirMembership()
            val theirAttestations = receiveTheirAttestations()

            currentStep(SendMembershipAndAttestationsStep)
            sendOurMembership(membership)
            sendOurAttestations(attestations)
            return theirMembership to theirAttestations
        }

        return null
    }

    @Suspendable
    private fun sendOurMembership(membership: StateAndRef<Membership>) {
        subFlow(PublishMembershipFlow(membership, setOf(session)))
    }

    @Suspendable
    private fun sendOurAttestations(attestations: Iterable<StateAndRef<MembershipAttestation>>) {
        session.send(attestations.count())

        for (attestation in attestations) {
            subFlow(PublishAttestationFlow(attestation, setOf(session)))
        }
    }

    @Suspendable
    private fun receiveTheirMembership(): StateAndRef<Membership> {
        val transaction = subFlow(PublishMembershipFlowHandler(session))
        return transaction.tx.outRefsOfType<Membership>().single()
    }

    @Suspendable
    private fun receiveTheirAttestations(): Set<StateAndRef<MembershipAttestation>> {
        val attestations = mutableSetOf<StateAndRef<MembershipAttestation>>()
        val theirAttestationSize = session.receive<Int>().unwrap { it }

        for (index in 0 until theirAttestationSize) {
            val transaction = subFlow(PublishAttestationFlowHandler(session))
            val attestation = transaction.tx.outRefsOfType<MembershipAttestation>().single()
            attestations.add(attestation)
        }

        return attestations
    }

    @InitiatedBy(SynchronizeMembershipFlow.Initiator::class)
    private class Handler(
        private val session: FlowSession
    ) : FlowLogic<Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>?>() {

        private companion object {
            object HandleSynchronizedMembershipAndAttestationsStep : Step("Handling membership synchronization.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(HandleSynchronizedMembershipAndAttestationsStep)

        @Suspendable
        override fun call(): Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>? {
            currentStep(HandleSynchronizedMembershipAndAttestationsStep)
            return subFlow(
                SynchronizeMembershipFlowHandler(
                    session,
                    HandleSynchronizedMembershipAndAttestationsStep.childProgressTracker()
                )
            )
        }
    }
}
