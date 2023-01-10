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

package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.attest
import io.onixlabs.corda.bnms.workflow.membership.AmendMembershipAttestationFlow
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipAttestationFlow
import io.onixlabs.corda.bnms.workflow.membership.RevokeMembershipAttestationFlow
import io.onixlabs.corda.core.integration.RPCService
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.*
import net.corda.core.transactions.SignedTransaction
import java.util.*

class MembershipAttestationService(rpc: CordaRPCOps) : RPCService(rpc) {

    fun issueMembershipAttestation(
        membership: StateAndRef<Membership>,
        attestor: AbstractParty = ourIdentity,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        val attestation = membership.attest(attestor, status, metadata, linearId)
        return issueMembershipAttestation(attestation, notary, observers)
    }

    fun issueMembershipAttestation(
        membership: StateAndRef<Membership>,
        attestor: AbstractParty = ourIdentity,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        val attestation = membership.attest(attestor, status, metadata, linearId)
        return issueMembershipAttestation(attestation, notary, observers, clientId)
    }

    fun issueMembershipAttestation(
        attestation: MembershipAttestation,
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueMembershipAttestationFlow::Initiator,
            attestation,
            notary,
            observers
        )
    }

    fun issueMembershipAttestation(
        attestation: MembershipAttestation,
        notary: Party? = null,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            IssueMembershipAttestationFlow::Initiator,
            attestation,
            notary,
            observers
        )
    }

    fun amendMembershipAttestation(
        oldAttestation: StateAndRef<MembershipAttestation>,
        newAttestation: MembershipAttestation,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendMembershipAttestationFlow::Initiator,
            oldAttestation,
            newAttestation,
            observers
        )
    }

    fun amendMembershipAttestation(
        oldAttestation: StateAndRef<MembershipAttestation>,
        newAttestation: MembershipAttestation,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            AmendMembershipAttestationFlow::Initiator,
            oldAttestation,
            newAttestation,
            observers
        )
    }

    fun revokeMembershipAttestation(
        attestation: StateAndRef<MembershipAttestation>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeMembershipAttestationFlow::Initiator,
            attestation,
            observers
        )
    }

    fun revokeMembershipAttestation(
        attestation: StateAndRef<MembershipAttestation>,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            RevokeMembershipAttestationFlow::Initiator,
            attestation,
            observers
        )
    }
}
