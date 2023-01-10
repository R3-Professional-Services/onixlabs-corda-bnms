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

package io.onixlabs.corda.bnms.contract.revocation

import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object RevocationLockSchema {

    object RevocationLockSchemaV1 :
        MappedSchema(RevocationLockSchema.javaClass, 1, listOf(RevocationLockEntity::class.java)) {
        override val migrationResource = "revocation-lock-schema.changelog-master"
    }

    @Entity
    @Table(name = "revocation_lock_states")
    class RevocationLockEntity(
        @Column(name = "owner", nullable = false)
        val owner: AbstractParty = NULL_PARTY,

        @Column(name = "pointer_state_linear_id", nullable = false)
        val pointerStateLinearId: UUID = UUID.randomUUID(),

        @Column(name = "pointer_state_external_id", nullable = true)
        val pointerStateExternalId: String? = null,

        @Column(name = "pointer_state_class", nullable = false)
        val pointerStateClass: String = ""
    ) : PersistentState() {
        constructor(revocationLock: RevocationLock<*>) : this(
            owner = revocationLock.owner,
            pointerStateLinearId = revocationLock.pointer.pointer.id,
            pointerStateExternalId = revocationLock.pointer.pointer.externalId,
            pointerStateClass = revocationLock.pointer.type.canonicalName
        )
    }
}
