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

package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.NetworkState
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationSchemaV1
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
 * Represents a membership attestation; a proof that a particular [Membership] state has been witnessed.
 *
 * @property network The business network that this membership attestation belongs to.
 * @property attestor The party who is attesting to the witnessed [Membership] state.
 * @property attestees The parties of this attestation, usually the participants of the attested [Membership] state.
 * @property pointer The pointer to the attested [Membership] state.
 * @property status The status of the attestation.
 * @property metadata Additional information about the attestation.
 * @property linearId The unique identifier of the attestation.
 * @property previousStateRef The state reference of the previous state in the chain.
 * @property hash The unique hash which represents this attestation.
 * @property participants The participants of this attestation; namely the attestor and attestees.
 * @property holder A reference to the only attestee of the attestation; namely the [Membership] holder.
 * @property isNetworkOperator Determines whether this membership attestation is attesting to the business network operator's [Membership] state.
 */
@BelongsToContract(MembershipAttestationContract::class)
class MembershipAttestation internal constructor(
    override val network: Network,
    attestor: AbstractParty,
    attestees: Set<AbstractParty>,
    pointer: AttestationPointer<Membership>,
    status: AttestationStatus,
    metadata: Map<String, String>,
    linearId: UniqueIdentifier,
    previousStateRef: StateRef?
) : Attestation<Membership>(
    attestor,
    attestees,
    pointer,
    status,
    metadata,
    linearId,
    previousStateRef
), NetworkState {

    /**
     * Represents a membership attestation; a proof that a particular [Membership] state has been witnessed.
     *
     * @param attestor The party who is attesting to the witnessed [Membership] state.
     * @param membership The [Membership] state that is being witnessed and attested.
     * @param status The status of the attestation.
     * @param metadata Additional information about the attestation.
     * @param linearId The unique identifier of the attestation.
     * @param previousStateRef The state reference of the previous state in the chain.
     *
     * The primary constructor of the [MembershipAttestation] class is deliberately private
     * to enforce static attestation pointers via the secondary constructor.
     */
    constructor(
        attestor: AbstractParty,
        membership: StateAndRef<Membership>,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        previousStateRef: StateRef? = null
    ) : this(
        membership.state.data.network,
        attestor,
        setOf(membership.state.data.holder),
        membership.toStaticAttestationPointer(),
        status,
        metadata,
        linearId,
        previousStateRef
    )

    val holder: AbstractParty
        get() = attestees.single()

    val isNetworkOperator: Boolean
        get() = holder == network.operator

    /**
     * Amends this attestation.
     *
     * @property previousStateRef The state reference of the previous state in the chain.
     * @param status The amended attestation status.
     * @param pointer The pointer to the attested state, if a new version of the state is being attested.
     * @param metadata Additional information about the attestation.
     * @return Returns a new, amended version of this attestation state.
     */
    override fun amend(
        previousStateRef: StateRef,
        status: AttestationStatus,
        pointer: AttestationPointer<Membership>,
        metadata: Map<String, String>
    ): MembershipAttestation {
        return MembershipAttestation(
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

    /**
     * Generates a persistent state entity from this contract state.
     *
     * @param schema The mapped schema from which to generate a persistent state entity.
     * @return Returns a persistent state entity.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is MembershipAttestationSchemaV1 -> MembershipAttestationEntity(this)
        else -> super.generateMappedObject(schema)
    }

    /**
     * Gets the supported schemas of this state.
     *
     * @return Returns the supported schemas of this state.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> {
        return super.supportedSchemas() + MembershipAttestationSchemaV1
    }
}
