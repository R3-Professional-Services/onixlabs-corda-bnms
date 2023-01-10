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

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.NetworkState
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationSchema.RelationshipAttestationEntity
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationSchema.RelationshipAttestationSchemaV1
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationPointer
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.corda.identityframework.contract.toStaticAttestationPointer
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

/**
 * Represents a relationship attestation; a proof that a particular [Relationship] state has been witnessed.
 *
 * @property network The business network that this relationship attestation belongs to.
 * @property attestor The party who is attesting to the witnessed [Relationship] state.
 * @property attestees The parties of this attestation, usually the participants of the attested [Relationship] state.
 * @property pointer The pointer to the attested [Relationship] state.
 * @property status The status of the attestation.
 * @property metadata Additional information about the attestation.
 * @property linearId The unique identifier of the attestation.
 * @property previousStateRef The state reference of the previous state in the chain.
 * @property hash The unique hash which represents this attestation.
 * @property participants The participants of this attestation; namely the attestor and attestees.
 */
@BelongsToContract(RelationshipAttestationContract::class)
class RelationshipAttestation internal constructor(
    override val network: Network,
    attestor: AbstractParty,
    attestees: Set<AbstractParty>,
    pointer: AttestationPointer<Relationship>,
    status: AttestationStatus,
    metadata: Map<String, String>,
    linearId: UniqueIdentifier,
    previousStateRef: StateRef?
) : Attestation<Relationship>(
    attestor,
    attestees,
    pointer,
    status,
    metadata,
    linearId,
    previousStateRef
), NetworkState {

    constructor(
        attestor: AbstractParty,
        relationship: StateAndRef<Relationship>,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        previousStateRef: StateRef? = null
    ) : this(
        relationship.state.data.network,
        attestor,
        relationship.state.data.participants.toSet(),
        relationship.toStaticAttestationPointer(),
        status,
        metadata,
        linearId,
        previousStateRef
    )

    override fun amend(
        previousStateRef: StateRef,
        status: AttestationStatus,
        pointer: AttestationPointer<Relationship>,
        metadata: Map<String, String>
    ): RelationshipAttestation {
        return RelationshipAttestation(
            network,
            attestor,
            attestees,
            pointer,
            status,
            metadata,
            linearId,
            previousStateRef
        )
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RelationshipAttestationSchemaV1 -> RelationshipAttestationEntity(this)
        else -> super.generateMappedObject(schema)
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return super.supportedSchemas() + RelationshipAttestationSchemaV1
    }
}
