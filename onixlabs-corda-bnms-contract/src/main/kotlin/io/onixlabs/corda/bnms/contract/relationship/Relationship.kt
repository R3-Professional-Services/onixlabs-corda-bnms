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

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.relationship.RelationshipConfigurationSchema.RelationshipConfigurationEntity
import io.onixlabs.corda.bnms.contract.relationship.RelationshipSchema.RelationshipEntity
import io.onixlabs.corda.bnms.contract.relationship.RelationshipSchema.RelationshipSchemaV1
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.core.contract.*
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.vaultQuery
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * Represents a multi-lateral relationship between participants of a business network.
 *
 * @property network The business network that this relationship belongs to.
 * @property members The members of this relationship and their relationship configuration.
 * @property linearId The unique identifier of the relationship.
 * @property previousStateRef The state reference of the previous relationship state in the chain.
 * @property configurations A resolver for all relationship configurations that belong to this relationship.
 * @property hash The unique hash that represents this relationship.
 * @property participants The participants of this relationship; namely the relationship members.
 */
@BelongsToContract(RelationshipContract::class)
data class Relationship(
    override val network: Network,
    val members: Map<out AbstractParty, Configuration> = emptyMap(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val previousStateRef: StateRef? = null
) : NetworkState, ChainState, LinearState, QueryableState, Hashable {

    val configurations: PluralResolvable<RelationshipConfiguration>
        get() = RelationshipConfigurationResolver(linearId)

    override val hash: SecureHash
        get() = SecureHash.sha256("$network${participants.participantHash}$previousStateRef")

    override val participants: List<AbstractParty>
        get() = members.map { it.key }

    /**
     * Creates revocation locks for each relationship participant.
     *
     * @return Returns a list of [RevocationLock] pointing to this relationship for each participant.
     */
    fun createRevocationLocks(): List<RevocationLock<Relationship>> {
        return participants.map { RevocationLock(it, LinearPointer(this.linearId, this.javaClass, false)) }
    }

    /**
     * Configures this relationship for the specified participant.
     *
     * @param member The member of the relationship which will be configured.
     * @param action The action which will configure this relationship.
     * @return Returns a new relationship state containing the updated configuration.
     */
    @ConfigurationBuilderDslContext
    fun configure(member: AbstractParty, action: ConfigurationBuilder.() -> Unit): Relationship {
        val configuration = members[member]
            ?: throw IllegalArgumentException("The specified member was not found in this relationship: $member.")
        val builder = ConfigurationBuilder(configuration)
        action(builder)
        val mutableMembers = members.toMutableMap()
        mutableMembers[member] = builder.toConfiguration()
        return copy(members = mutableMembers)
    }

    /**
     * Generates a persistent state entity from this contract state.
     *
     * @param schema The mapped schema from which to generate a persistent state entity.
     * @return Returns a persistent state entity.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RelationshipSchemaV1 -> RelationshipEntity(this)
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets the supported schemas of this state.
     *
     * @return Returns the supported schemas of this state.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(RelationshipSchemaV1)
    }

    /**
     * Ensures that the immutable properties of this relationship have not changed.
     *
     * @param other The relationship to compare with the current relationship.
     * @return Returns true if the immutable properties have not changed; otherwise, false.
     */
    internal fun immutableEquals(other: Relationship): Boolean {
        return this === other || (network == other.network && linearId == other.linearId)
    }

    /**
     * Represents the resolver which will be used to resolve relationship configurations for the specified relationship linear ID.
     *
     * @param relationshipLinearId The relationship linear ID for which to resolve relationship configurations.
     */
    private class RelationshipConfigurationResolver(
        private val relationshipLinearId: UniqueIdentifier
    ) : AbstractPluralResolvable<RelationshipConfiguration>() {

        /**
         * The vault query criteria which will be used to resolve relationship configurations.
         */
        override val criteria: QueryCriteria get() = vaultQuery<RelationshipConfiguration> {
            expression(RelationshipConfigurationEntity::relationshipLinearId equalTo relationshipLinearId.id)
        }

        /**
         * Determines whether this [PluralResolvable] is pointing to the specified [StateAndRef] instance.
         *
         * @param stateAndRef The [StateAndRef] to determine being pointed to.
         * @return Returns true if this [PluralResolvable] is pointing to the specified [StateAndRef]; otherwise, false.
         */
        override fun isPointingTo(stateAndRef: StateAndRef<RelationshipConfiguration>): Boolean {
            return relationshipLinearId == stateAndRef.state.data.relationshipLinearId
        }
    }
}
