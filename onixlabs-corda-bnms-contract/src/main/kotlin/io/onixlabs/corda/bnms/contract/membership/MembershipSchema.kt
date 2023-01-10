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

import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object MembershipSchema {

    object MembershipSchemaV1 :
        MappedSchema(MembershipSchema.javaClass, 1, listOf(MembershipEntity::class.java)) {
        override val migrationResource = "membership-schema.changelog-master"
    }

    @Entity
    @Table(name = "membership_states")
    class MembershipEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

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

        @Column(name = "is_network_operator", nullable = false)
        val isNetworkOperator: Boolean = false,

        @Column(name = "hash", nullable = false, unique = true)
        val hash: String = ""
    ) : PersistentState() {
        constructor(membership: Membership) : this(
            linearId = membership.linearId.id,
            externalId = membership.linearId.externalId,
            holder = membership.holder,
            networkValue = membership.network.value,
            normalizedNetworkValue = membership.network.normalizedValue,
            networkOperator = membership.network.operator,
            networkHash = membership.network.hash.toString(),
            isNetworkOperator = membership.isNetworkOperator,
            hash = membership.hash.toString()
        )
    }
}
