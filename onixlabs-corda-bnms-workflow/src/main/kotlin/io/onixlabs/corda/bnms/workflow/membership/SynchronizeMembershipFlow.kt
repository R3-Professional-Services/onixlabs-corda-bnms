package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.onixlabs.corda.bnms.workflow.ReceiveMembershipAndAttestationsStep
import io.onixlabs.corda.bnms.workflow.SendMembershipAndAttestationsStep
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.core.workflow.InitializeFlowStep
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.identityframework.workflow.attestations.PublishAttestationFlow
import io.onixlabs.corda.identityframework.workflow.attestations.PublishAttestationFlowHandler
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.unwrap

class SynchronizeMembershipFlow(
    private val ourMembership: StateAndRef<Membership>,
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>?>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(
            InitializeFlowStep,
            SendMembershipAndAttestationsStep,
            ReceiveMembershipAndAttestationsStep
        )

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>? {
        currentStep(InitializeFlowStep)
        val network = ourMembership.state.data.network
        val holder = ourMembership.state.data.holder

        if (holder !in serviceHub.myInfo.legalIdentities) {
            throw FlowException("Membership synchronization can only occur when the membership is owned by this node.")
        }

        val attestations = serviceHub.vaultServiceFor<MembershipAttestation>().filter {
            expression(MembershipAttestationEntity::holder equalTo holder)
            expression(MembershipAttestationEntity::networkHash equalTo network.hash.toString())
        }.toList()

        if (network.operator != null && attestations.size > 1) {
            throw FlowException("Only one membership attestation is required when a network operator is present.")
        }

        val isMemberOfNetwork = session.sendAndReceive<Boolean>(network).unwrap { it }

        if (isMemberOfNetwork) {
            currentStep(SendMembershipAndAttestationsStep)
            sendOurMembership()
            sendOurAttestations(attestations)

            currentStep(ReceiveMembershipAndAttestationsStep)
            val theirMembership = receiveTheirMembership()
            val theirAttestations = receiveTheirAttestations()

            return theirMembership to theirAttestations
        }

        return null
    }

    @Suspendable
    private fun sendOurMembership() {
        subFlow(PublishMembershipFlow(ourMembership, setOf(session)))
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

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val ourMembership: StateAndRef<Membership>,
        private val counterparty: Party
    ) : FlowLogic<Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>?>() {

        private companion object {
            object SynchronizeMembershipAndAttestationsStep : Step("Synchronizing membership.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(SynchronizeMembershipAndAttestationsStep)

        @Suspendable
        override fun call(): Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>? {
            currentStep(SynchronizeMembershipAndAttestationsStep)
            val session = initiateFlow(counterparty)

            return subFlow(
                SynchronizeMembershipFlow(
                    ourMembership,
                    session,
                    SynchronizeMembershipAndAttestationsStep.childProgressTracker()
                )
            )
        }
    }
}
