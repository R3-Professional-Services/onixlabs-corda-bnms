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

package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.corda.identityframework.contract.toStaticAttestationPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

fun StateAndRef<Relationship>.getNextOutput(): Relationship {
    return state.data.copy(previousStateRef = ref)
}

fun StateAndRef<Relationship>.attest(
    attestor: AbstractParty,
    status: AttestationStatus,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
) = RelationshipAttestation(attestor, this, status, metadata, linearId)

fun StateAndRef<Relationship>.accept(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
) = attest(attestor, AttestationStatus.ACCEPTED, metadata, linearId)

fun StateAndRef<Relationship>.reject(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
) = attest(attestor, AttestationStatus.REJECTED, metadata, linearId)

fun StateAndRef<RelationshipAttestation>.amend(
    status: AttestationStatus,
    metadata: Map<String, String> = this.state.data.metadata
) = this.state.data.amend(ref, status, state.data.pointer, metadata)

fun StateAndRef<RelationshipAttestation>.amend(
    relationship: StateAndRef<Relationship>,
    status: AttestationStatus,
    metadata: Map<String, String> = this.state.data.metadata
) = this.state.data.amend(ref, status, relationship.toStaticAttestationPointer(), metadata)

fun StateAndRef<RelationshipAttestation>.accept(
    metadata: Map<String, String> = this.state.data.metadata
) = amend(AttestationStatus.ACCEPTED, metadata)

fun StateAndRef<RelationshipAttestation>.accept(
    relationship: StateAndRef<Relationship>,
    metadata: Map<String, String> = this.state.data.metadata
) = amend(relationship, AttestationStatus.ACCEPTED, metadata)

fun StateAndRef<RelationshipAttestation>.reject(
    metadata: Map<String, String> = this.state.data.metadata
) = amend(AttestationStatus.REJECTED, metadata)

fun StateAndRef<RelationshipAttestation>.reject(
    relationship: StateAndRef<Relationship>,
    metadata: Map<String, String> = this.state.data.metadata
) = amend(relationship, AttestationStatus.REJECTED, metadata)
