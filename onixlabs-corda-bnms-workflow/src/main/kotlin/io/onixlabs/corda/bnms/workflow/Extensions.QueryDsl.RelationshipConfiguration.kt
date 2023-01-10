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

import io.onixlabs.corda.bnms.contract.relationship.RelationshipConfiguration
import io.onixlabs.corda.bnms.contract.relationship.RelationshipConfigurationSchema.RelationshipConfigurationEntity
import io.onixlabs.corda.core.services.QueryDsl
import io.onixlabs.corda.core.services.QueryDslContext
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.isNull
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import java.util.*

@QueryDslContext
fun QueryDsl<RelationshipConfiguration>.relationshipConfigurationRelationshipLinearId(value: UUID) {
    expression(RelationshipConfigurationEntity::relationshipLinearId equalTo value)
}

@QueryDslContext
fun QueryDsl<RelationshipConfiguration>.relationshipConfigurationRelationshipExternalId(value: String?) {
    if (value == null) expression(RelationshipConfigurationEntity::relationshipExternalId.isNull())
    else expression(RelationshipConfigurationEntity::relationshipExternalId equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RelationshipConfiguration>.relationshipConfigurationNetworkValue(value: String) {
    expression(RelationshipConfigurationEntity::networkValue equalTo value)
}

@QueryDslContext
fun QueryDsl<RelationshipConfiguration>.relationshipConfigurationNormalizedNetworkValue(value: String) {
    expression(RelationshipConfigurationEntity::normalizedNetworkValue equalTo value.toUpperCase())
}

@QueryDslContext
fun QueryDsl<RelationshipConfiguration>.relationshipConfigurationNetworkOperator(value: AbstractParty?) {
    if (value == null) expression(RelationshipConfigurationEntity::networkOperator.isNull())
    else expression(RelationshipConfigurationEntity::networkOperator equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RelationshipConfiguration>.relationshipConfigurationNormalizedNetworkHash(value: SecureHash) {
    expression(RelationshipConfigurationEntity::networkHash equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RelationshipConfiguration>.relationshipConfigurationConfigurationHash(value: SecureHash) {
    expression(RelationshipConfigurationEntity::configurationHash equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RelationshipConfiguration>.relationshipConfigurationHash(value: SecureHash) {
    expression(RelationshipConfigurationEntity::hash equalTo value.toString())
}
