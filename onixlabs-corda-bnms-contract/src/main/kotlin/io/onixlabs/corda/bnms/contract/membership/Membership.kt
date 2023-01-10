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

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipSchemaV1
import io.onixlabs.corda.core.contract.ChainState
import io.onixlabs.corda.core.contract.Hashable
import io.onixlabs.corda.identityframework.contract.claims.AbstractClaim
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * Represents a membership to a business network.
 *
 * @property network The business network that this membership belongs to.
 * @property holder The holder of the membership.
 * @property identity The claims that represent the identity of the holder of the membership.
 * @property configuration The configuration of the membership, including settings, roles and permissions.
 * @property linearId The unique identifier of the membership.
 * @property previousStateRef The state reference of the previous membership state in the chain.
 * @property isNetworkOperator Determines whether this membership represents the business network operator.
 * @property hash The unique hash that represents this membership.
 * @property participants The participants of this membership; namely the holder and optionally the network operator.
 */
@BelongsToContract(MembershipContract::class)
data class Membership(
    override val network: Network,
    val holder: AbstractParty,
    val identity: Set<AbstractClaim<*>> = emptySet(),
    val configuration: Configuration = Configuration(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val previousStateRef: StateRef? = null
) : NetworkState, ChainState, LinearState, QueryableState, Hashable {

    constructor(
        network: Network,
        holder: AbstractParty,
        identity: Set<AbstractClaim<*>> = emptySet(),
        settings: Set<Setting<*>> = emptySet(),
        linearId: UniqueIdentifier = UniqueIdentifier()
    ) : this(network, holder, identity, Configuration(settings), linearId, null)

    val isNetworkOperator: Boolean
        get() = holder == network.operator

    override val hash: SecureHash
        get() = SecureHash.sha256("$network$holder${configuration.hash}$previousStateRef")

    override val participants: List<AbstractParty>
        get() = setOf(holder, network.operator).filterNotNull()

    /**
     * Configures this membership.
     *
     * @param action The action which will configure this membership.
     * @return Returns a new membership state containing the updated configuration.
     */
    @ConfigurationBuilderDslContext
    fun configure(action: ConfigurationBuilder.() -> Unit): Membership {
        val builder = ConfigurationBuilder(configuration)
        action(builder)
        return copy(configuration = builder.toConfiguration())
    }

    /**
     * Generates a persistent state entity from this contract state.
     *
     * @param schema The mapped schema from which to generate a persistent state entity.
     * @return Returns a persistent state entity.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is MembershipSchemaV1 -> MembershipEntity(this)
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets the supported schemas of this state.
     *
     * @return Returns the supported schemas of this state.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(MembershipSchemaV1)
    }

    /**
     * Ensures that the immutable properties of this membership have not changed.
     *
     * @param other The membership to compare with the current membership.
     * @return Returns true if the immutable properties have not changed; otherwise, false.
     */
    internal fun immutableEquals(other: Membership): Boolean {
        return this === other || (network == other.network
                && holder == other.holder
                && linearId == other.linearId)
    }
}
