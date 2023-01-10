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

import io.onixlabs.corda.bnms.contract.revocation.RevocationLockSchema.RevocationLockEntity
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockSchema.RevocationLockSchemaV1
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

@BelongsToContract(RevocationLockContract::class)
data class RevocationLock<T : LinearState>(
    override val owner: AbstractParty,
    val pointer: LinearPointer<T>
) : OwnableState, QueryableState {

    constructor(owner: AbstractParty, state: T) : this(owner, LinearPointer(state.linearId, state.javaClass, false))

    override val participants: List<AbstractParty>
        get() = listOf(owner)

    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        throw IllegalStateException("Cannot change ownership of a revocation lock.")
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RevocationLockSchemaV1 -> RevocationLockEntity(this)
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(RevocationLockSchemaV1)
    }
}
