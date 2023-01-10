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

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipSchema.RelationshipEntity
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
fun QueryDsl<Relationship>.relationshipNetworkValue(value: String) {
    expression(RelationshipEntity::networkValue equalTo value)
}

@QueryDslContext
fun QueryDsl<Relationship>.relationshipNormalizedNetworkValue(value: String) {
    expression(RelationshipEntity::normalizedNetworkValue equalTo value.toUpperCase())
}

@QueryDslContext
fun QueryDsl<Relationship>.relationshipNetworkOperator(value: AbstractParty?) {
    if (value == null) expression(RelationshipEntity::networkOperator.isNull())
    else expression(RelationshipEntity::networkOperator equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<Relationship>.relationshipNetworkHash(value: SecureHash) {
    expression(RelationshipEntity::networkHash equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<Relationship>.relationshipHash(value: SecureHash) {
    expression(RelationshipEntity::hash equalTo value.toString())
}
