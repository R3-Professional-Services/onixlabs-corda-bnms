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

import io.onixlabs.corda.bnms.contract.Configuration
import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.NetworkState
import io.onixlabs.corda.bnms.contract.relationship.RelationshipConfigurationSchema.RelationshipConfigurationEntity
import io.onixlabs.corda.bnms.contract.relationship.RelationshipConfigurationSchema.RelationshipConfigurationSchemaV1
import io.onixlabs.corda.core.contract.AbstractSingularResolvable
import io.onixlabs.corda.core.contract.Hashable
import io.onixlabs.corda.core.contract.SingularResolvable
import io.onixlabs.corda.core.services.vaultQuery
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.QueryableState

@BelongsToContract(RelationshipConfigurationContract::class)
class RelationshipConfiguration private constructor(
    override val network: Network,
    override val participants: List<AbstractParty>,
    val configuration: Configuration,
    val relationshipLinearId: UniqueIdentifier,
    override val linearId: UniqueIdentifier
) : NetworkState, LinearState, QueryableState, Hashable {

    constructor(
        relationship: Relationship,
        configuration: Configuration,
        linearId: UniqueIdentifier = UniqueIdentifier()
    ) : this(relationship.network, relationship.participants, configuration, relationship.linearId, linearId)

    val relationship: SingularResolvable<Relationship>
        get() = RelationshipResolver(relationshipLinearId)

    override val hash: SecureHash
        get() = SecureHash.sha256("${configuration.hash}$relationshipLinearId")

    override fun generateMappedObject(schema: MappedSchema) = when (schema) {
        is RelationshipConfigurationSchemaV1 -> RelationshipConfigurationEntity(this)
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(RelationshipConfigurationSchemaV1)
    }

    private class RelationshipResolver(
        private val relationshipLinearId: UniqueIdentifier
    ) : AbstractSingularResolvable<Relationship>() {

        override val criteria: QueryCriteria get() = vaultQuery<Relationship> {
            linearIds(relationshipLinearId)
        }

        override fun isPointingTo(stateAndRef: StateAndRef<Relationship>): Boolean {
            return relationshipLinearId == stateAndRef.state.data.linearId
        }
    }
}
