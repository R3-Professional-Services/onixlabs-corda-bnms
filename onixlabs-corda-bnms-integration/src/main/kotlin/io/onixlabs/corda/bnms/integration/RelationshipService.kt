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

import io.onixlabs.corda.bnms.contract.Configuration
import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.relationship.AmendRelationshipFlow
import io.onixlabs.corda.bnms.workflow.relationship.IssueRelationshipFlow
import io.onixlabs.corda.bnms.workflow.relationship.RevokeRelationshipFlow
import io.onixlabs.corda.core.integration.RPCService
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.*
import net.corda.core.transactions.SignedTransaction
import java.util.*

class RelationshipService(rpc: CordaRPCOps) : RPCService(rpc) {

    fun issueRelationship(
        network: Network,
        members: Map<out AbstractParty, Configuration> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        checkMembership: Boolean = false
    ): FlowProgressHandle<SignedTransaction> {
        val relationship = Relationship(network, members, linearId)
        return issueRelationship(relationship, notary, checkMembership)
    }

    fun issueRelationship(
        network: Network,
        members: Map<out AbstractParty, Configuration> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        checkMembership: Boolean = false,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        val relationship = Relationship(network, members, linearId)
        return issueRelationship(relationship, notary, checkMembership, clientId)
    }

    fun issueRelationship(
        relationship: Relationship,
        notary: Party? = null,
        checkMembership: Boolean = false
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueRelationshipFlow::Initiator,
            relationship,
            notary,
            checkMembership
        )
    }

    fun issueRelationship(
        relationship: Relationship,
        notary: Party? = null,
        checkMembership: Boolean = false,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            IssueRelationshipFlow::Initiator,
            relationship,
            notary,
            checkMembership
        )
    }

    fun amendRelationship(
        oldRelationship: StateAndRef<Relationship>,
        newRelationship: Relationship,
        checkMembership: Boolean = false
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendRelationshipFlow::Initiator,
            oldRelationship,
            newRelationship,
            checkMembership
        )
    }

    fun amendRelationship(
        oldRelationship: StateAndRef<Relationship>,
        newRelationship: Relationship,
        checkMembership: Boolean = false,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            AmendRelationshipFlow::Initiator,
            oldRelationship,
            newRelationship,
            checkMembership
        )
    }

    fun revokeRelationship(
        relationship: StateAndRef<Relationship>
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeRelationshipFlow::Initiator,
            relationship
        )
    }

    fun revokeRelationship(
        relationship: StateAndRef<Relationship>,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            RevokeRelationshipFlow::Initiator,
            relationship
        )
    }
}
