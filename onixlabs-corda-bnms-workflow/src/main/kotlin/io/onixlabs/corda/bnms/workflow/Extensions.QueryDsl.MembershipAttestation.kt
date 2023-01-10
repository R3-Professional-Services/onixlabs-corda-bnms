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

import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
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
fun QueryDsl<MembershipAttestation>.membershipAttestationAttestor(value: AbstractParty) {
    expression(MembershipAttestationEntity::attestor equalTo value)
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationHolder(value: AbstractParty) {
    expression(MembershipAttestationEntity::holder equalTo value)
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationNetworkValue(value: String) {
    expression(MembershipAttestationEntity::networkValue equalTo value)
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationNormalizedNetworkValue(value: String) {
    expression(MembershipAttestationEntity::normalizedNetworkValue equalTo value.toUpperCase())
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationNetworkOperator(value: AbstractParty?) {
    if (value == null) expression(MembershipAttestationEntity::networkOperator.isNull())
    else expression(MembershipAttestationEntity::networkOperator equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationNetworkHash(value: SecureHash) {
    expression(MembershipAttestationEntity::networkHash equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationPointer(value: Any) {
    expression(MembershipAttestationEntity::pointer equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationPointerStateType(value: Class<out ContractState>) {
    expression(MembershipAttestationEntity::pointerStateType equalTo value.canonicalName)
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationPointerHash(value: SecureHash) {
    expression(MembershipAttestationEntity::pointerHash equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationStatus(value: AttestationStatus) {
    expression(MembershipAttestationEntity::status equalTo value)
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationPreviousStateRef(value: StateRef?) {
    if (value == null) expression(MembershipAttestationEntity::previousStateRef.isNull())
    else expression(MembershipAttestationEntity::previousStateRef equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<MembershipAttestation>.membershipAttestationHash(value: SecureHash) {
    expression(MembershipAttestationEntity::hash equalTo value.toString())
}
