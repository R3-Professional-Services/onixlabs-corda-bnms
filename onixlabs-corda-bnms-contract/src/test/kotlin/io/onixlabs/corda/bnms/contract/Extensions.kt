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

package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

fun Membership.withWrongRef(previousStateRef: StateRef = EMPTY_REF): Membership {
    return copy(previousStateRef = previousStateRef)
}

fun Membership.withWrongNetwork(network: Network = INVALID_NETWORK): Membership {
    return copy(network = network)
}

fun Relationship.withWrongRef(previousStateRef: StateRef = EMPTY_REF): Relationship {
    return copy(previousStateRef = previousStateRef)
}

fun Relationship.withWrongNetwork(network: Network = INVALID_NETWORK): Relationship {
    return copy(network = network)
}

fun Relationship.withWrongLocks(): List<RevocationLock<Relationship>> {
    return participants.map {
        RevocationLock(it, LinearPointer(UniqueIdentifier(id = EMPTY_GUID), Relationship::class.java))
    }
}

fun MembershipAttestation.withWrongHolder(attestees: Set<AbstractParty> = setOf(IDENTITY_C.party)): MembershipAttestation {
    return MembershipAttestation(network, attestor, attestees, pointer, status, metadata, linearId, previousStateRef)
}

fun MembershipAttestation.withWrongNetwork(network: Network = INVALID_NETWORK): MembershipAttestation {
    return MembershipAttestation(network, attestor, attestees, pointer, status, metadata, linearId, previousStateRef)
}

fun RelationshipAttestation.withWrongNetwork(network: Network = INVALID_NETWORK): RelationshipAttestation {
    return RelationshipAttestation(network, attestor, attestees, pointer, status, metadata, linearId, previousStateRef)
}