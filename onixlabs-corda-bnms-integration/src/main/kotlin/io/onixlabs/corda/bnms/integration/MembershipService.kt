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

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.Setting
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.membership.AmendMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.RevokeMembershipFlow
import io.onixlabs.corda.core.integration.RPCService
import io.onixlabs.corda.identityframework.contract.claims.AbstractClaim
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.*
import net.corda.core.transactions.SignedTransaction
import java.util.*

class MembershipService(rpc: CordaRPCOps) : RPCService(rpc) {

    fun issueMembership(
        network: Network,
        holder: AbstractParty = ourIdentity,
        identity: Set<AbstractClaim<*>> = emptySet(),
        settings: Set<Setting<*>> = emptySet(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        val membership = Membership(network, holder, identity, settings, linearId)
        return issueMembership(membership, notary, observers)
    }

    fun issueMembership(
        network: Network,
        holder: AbstractParty = ourIdentity,
        identity: Set<AbstractClaim<*>> = emptySet(),
        settings: Set<Setting<*>> = emptySet(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        val membership = Membership(network, holder, identity, settings, linearId)
        return issueMembership(membership, notary, observers, clientId)
    }

    fun issueMembership(
        membership: Membership,
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueMembershipFlow::Initiator,
            membership,
            notary,
            observers
        )
    }

    fun issueMembership(
        membership: Membership,
        notary: Party? = null,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            IssueMembershipFlow::Initiator,
            membership,
            notary,
            observers
        )
    }

    fun amendMembership(
        oldMembership: StateAndRef<Membership>,
        newMembership: Membership,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendMembershipFlow::Initiator,
            oldMembership,
            newMembership,
            observers
        )
    }

    fun amendMembership(
        oldMembership: StateAndRef<Membership>,
        newMembership: Membership,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            AmendMembershipFlow::Initiator,
            oldMembership,
            newMembership,
            observers
        )
    }

    fun revokeMembership(
        membership: StateAndRef<Membership>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeMembershipFlow::Initiator,
            membership,
            observers
        )
    }

    fun revokeMembership(
        membership: StateAndRef<Membership>,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            RevokeMembershipFlow::Initiator,
            membership,
            observers
        )
    }
}
