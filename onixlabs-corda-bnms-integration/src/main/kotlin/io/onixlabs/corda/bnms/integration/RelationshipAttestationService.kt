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

import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.attest
import io.onixlabs.corda.bnms.workflow.relationship.AmendRelationshipAttestationFlow
import io.onixlabs.corda.bnms.workflow.relationship.IssueRelationshipAttestationFlow
import io.onixlabs.corda.bnms.workflow.relationship.RevokeRelationshipAttestationFlow
import io.onixlabs.corda.core.integration.RPCService
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.*
import net.corda.core.transactions.SignedTransaction
import java.util.*

class RelationshipAttestationService(rpc: CordaRPCOps) : RPCService(rpc) {

    fun issueRelationshipAttestation(
        relationship: StateAndRef<Relationship>,
        attestor: AbstractParty = ourIdentity,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null
    ): FlowProgressHandle<SignedTransaction> {
        val attestation = relationship.attest(attestor, status, metadata, linearId)
        return issueRelationshipAttestation(attestation, notary)
    }

    fun issueRelationshipAttestation(
        relationship: StateAndRef<Relationship>,
        attestor: AbstractParty = ourIdentity,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        val attestation = relationship.attest(attestor, status, metadata, linearId)
        return issueRelationshipAttestation(attestation, notary, clientId)
    }

    fun issueRelationshipAttestation(
        attestation: RelationshipAttestation,
        notary: Party? = null
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueRelationshipAttestationFlow::Initiator,
            attestation,
            notary
        )
    }

    fun issueRelationshipAttestation(
        attestation: RelationshipAttestation,
        notary: Party? = null,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            IssueRelationshipAttestationFlow::Initiator,
            attestation,
            notary
        )
    }

    fun amendRelationshipAttestation(
        oldAttestation: StateAndRef<RelationshipAttestation>,
        newAttestation: RelationshipAttestation
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendRelationshipAttestationFlow::Initiator,
            oldAttestation,
            newAttestation
        )
    }

    fun amendRelationshipAttestation(
        oldAttestation: StateAndRef<RelationshipAttestation>,
        newAttestation: RelationshipAttestation,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            AmendRelationshipAttestationFlow::Initiator,
            oldAttestation,
            newAttestation
        )
    }

    fun revokeRelationshipAttestation(
        attestation: StateAndRef<RelationshipAttestation>
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeRelationshipAttestationFlow::Initiator,
            attestation
        )
    }

    fun revokeRelationshipAttestation(
        attestation: StateAndRef<RelationshipAttestation>,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            RevokeRelationshipAttestationFlow::Initiator,
            attestation
        )
    }
}
