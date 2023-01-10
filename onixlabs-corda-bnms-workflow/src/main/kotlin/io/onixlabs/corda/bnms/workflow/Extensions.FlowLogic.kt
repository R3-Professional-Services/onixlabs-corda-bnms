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

package io.onixlabs.corda.bnms.workflow

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.core.services.any
import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.core.workflow.currentStep
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.utilities.unwrap

@Suspendable
fun FlowLogic<*>.checkMembership(checkMembership: Boolean, relationship: Relationship, sessions: Set<FlowSession>) {
    currentStep(SendCheckMembershipInstructionStep)
    sessions.forEach { it.send(checkMembership) }
    if (checkMembership) checkMembershipsAndAttestations(relationship)
}

@Suspendable
fun FlowLogic<*>.checkMembershipHandler(session: FlowSession): Boolean {
    currentStep(ReceiveCheckMembershipInstructionStep)
    return session.receive<Boolean>().unwrap { it }
}

@Suspendable
fun FlowLogic<*>.checkMembershipExists(membership: Membership) {
    val existingMembership = serviceHub.vaultServiceFor<Membership>().singleOrNull {
        stateStatus(Vault.StateStatus.ALL)
        membershipHash(membership.hash)
    }

    if (existingMembership != null) {
        throw FlowException("The specified membership already exists: ${existingMembership.state.data}.")
    }
}

@Suspendable
fun FlowLogic<*>.checkMembershipAttestationExistsForIssuance(attestation: MembershipAttestation) {
    val existingAttestation = serviceHub.vaultServiceFor<MembershipAttestation>().singleOrNull {
        membershipAttestationAttestor(attestation.attestor)
        membershipAttestationHolder(attestation.holder)
        membershipAttestationNetworkHash(attestation.network.hash)
    }

    if (existingAttestation != null) {
        throw FlowException("The specified membership attestation already exists and should be amended: ${existingAttestation.state.data}.")
    }
}

@Suspendable
fun FlowLogic<*>.checkMembershipAttestationExistsForAmendment(attestation: MembershipAttestation) {
    val existingAttestation = serviceHub.vaultServiceFor<MembershipAttestation>().singleOrNull {
        stateStatus(Vault.StateStatus.ALL)
        membershipAttestationHash(attestation.hash)
    }

    if (existingAttestation != null) {
        throw FlowException("The specified membership attestation already exists: ${existingAttestation.state.data}.")
    }
}

@Suspendable
fun FlowLogic<*>.checkMembershipsAndAttestations(relationship: Relationship, ourAttestorIdentity: AbstractParty = ourIdentity) {
    currentStep(CheckMembershipStep)
    val counterparties = relationship.participants
        .map { serviceHub.identityService.requireWellKnownPartyFromAnonymous(it) }
        .filter { it !in serviceHub.myInfo.legalIdentities }

    counterparties.forEach {
        val membership = serviceHub.vaultServiceFor<Membership>().singleOrNull {
            membershipHolder(it)
            membershipNetworkHash(relationship.network.hash)
        } ?: throw FlowException(buildString {
            append("Membership with the specified details could not be found, or has not been witnessed by this node: ")
            append("Holder = $it, ")
            append("Network = ${relationship.network}.")
        })

        serviceHub.vaultServiceFor<MembershipAttestation>().singleOrNull {
            membershipAttestationAttestor(ourAttestorIdentity)
            membershipAttestationPointer(membership.ref)
        } ?: throw FlowException(buildString {
            append("Membership attestation with the specified details could not be found, or has not been witnessed by this node: ")
            append("Holder = ${membership.state.data.holder}, ")
            append("Attestor = $ourAttestorIdentity, ")
            append("Network = ${membership.state.data.network}.")
        })
    }
}

@Suspendable
fun FlowLogic<*>.checkRevocationLockExists(owner: AbstractParty, state: LinearState) {
    val revocationLockExists = serviceHub.vaultServiceFor<RevocationLock<*>>().any {
        revocationLockOwner(owner)
        revocationLockPointerStateClass(state.javaClass)
        revocationLockPointerStateLinearId(state.linearId.id)
    }

    if (revocationLockExists) {
        throw FlowException("Revocation of this relationship is locked by counter-party: $owner.")
    }
}

@Suspendable
fun FlowLogic<*>.findMembershipForAttestation(attestation: MembershipAttestation): StateAndRef<Membership> {
    return attestation.pointer.resolve(serviceHub) ?: throw FlowException(buildString {
        append("Membership with the specified details could not be found, or has not been witnessed by this node: ")
        append("Holder = ${attestation.holder}, ")
        append("Network = ${attestation.network}.")
    })
}

@Suspendable
fun FlowLogic<*>.findRelationshipForAttestation(attestation: RelationshipAttestation): StateAndRef<Relationship> {
    return attestation.pointer.resolve(serviceHub) ?: throw FlowException(buildString {
        append("Relationship with the specified details could not be found, or has not been witnessed by this node: ")
        append("Network = ${attestation.network}.")
    })
}
