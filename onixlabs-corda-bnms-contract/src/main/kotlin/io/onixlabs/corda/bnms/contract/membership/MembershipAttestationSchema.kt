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

import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.*

object MembershipAttestationSchema {

    object MembershipAttestationSchemaV1 :
        MappedSchema(MembershipAttestationSchema.javaClass, 1, listOf(MembershipAttestationEntity::class.java)) {
        override val migrationResource = "membership-attestation-schema.changelog-master"
    }

    @Entity
    @Table(name = "membership_attestation_states")
    class MembershipAttestationEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "attestor", nullable = false)
        val attestor: AbstractParty = NULL_PARTY,

        @Column(name = "holder", nullable = false)
        val holder: AbstractParty = NULL_PARTY,

        @Column(name = "network_value", nullable = false)
        val networkValue: String = "",

        @Column(name = "normalized_network_value", nullable = false)
        val normalizedNetworkValue: String = "",

        @Column(name = "network_operator", nullable = true)
        val networkOperator: AbstractParty? = null,

        @Column(name = "network_hash", nullable = false)
        val networkHash: String = "",

        @Column(name = "pointer", nullable = false)
        val pointer: String = "",

        @Column(name = "pointer_state_type", nullable = false)
        val pointerStateType: String = "",

        @Column(name = "pointer_hash", nullable = false)
        val pointerHash: String = "",

        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        val status: AttestationStatus = AttestationStatus.REJECTED,

        @Column(name = "previous_state_ref", nullable = true)
        val previousStateRef: String? = null,

        @Column(name = "hash", nullable = false, unique = true)
        val hash: String = ""
    ) : PersistentState() {
        constructor(attestation: MembershipAttestation) : this(
            linearId = attestation.linearId.id,
            externalId = attestation.linearId.externalId,
            attestor = attestation.attestor,
            holder = attestation.holder,
            networkValue = attestation.network.value,
            normalizedNetworkValue = attestation.network.normalizedValue,
            networkOperator = attestation.network.operator,
            networkHash = attestation.network.hash.toString(),
            pointer = attestation.pointer.statePointer.toString(),
            pointerStateType = attestation.pointer.stateType.canonicalName,
            pointerHash = attestation.pointer.hash.toString(),
            status = attestation.status,
            previousStateRef = attestation.previousStateRef?.toString(),
            hash = attestation.hash.toString()
        )
    }
}
