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

import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationSchema.RelationshipAttestationEntity
import io.onixlabs.corda.core.services.QueryDsl
import io.onixlabs.corda.core.services.QueryDslContext
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationAttestor(value: AbstractParty) {
    expression(RelationshipAttestationEntity::attestor equalTo value)
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationNetworkValue(value: String) {
    expression(RelationshipAttestationEntity::networkValue equalTo value)
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationNormalizedNetworkValue(value: String) {
    expression(RelationshipAttestationEntity::normalizedNetworkValue equalTo value.toUpperCase())
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationNetworkOperator(value: AbstractParty?) {
    if (value == null) expression(RelationshipAttestationEntity::networkOperator.isNull())
    else expression(RelationshipAttestationEntity::networkOperator equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationNetworkHash(value: SecureHash) {
    expression(RelationshipAttestationEntity::networkHash equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationPointer(value: Any) {
    expression(RelationshipAttestationEntity::pointer equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationPointerStateType(value: Class<out ContractState>) {
    expression(RelationshipAttestationEntity::pointerStateType equalTo value.canonicalName)
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationPointerHash(value: SecureHash) {
    expression(RelationshipAttestationEntity::pointerHash equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationStatus(value: AttestationStatus) {
    expression(RelationshipAttestationEntity::status equalTo value)
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationPreviousStateRef(value: StateRef?) {
    if (value == null) expression(RelationshipAttestationEntity::previousStateRef.isNull())
    else expression(RelationshipAttestationEntity::previousStateRef equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RelationshipAttestation>.relationshipAttestationHash(value: SecureHash) {
    expression(RelationshipAttestationEntity::hash equalTo value.toString())
}
