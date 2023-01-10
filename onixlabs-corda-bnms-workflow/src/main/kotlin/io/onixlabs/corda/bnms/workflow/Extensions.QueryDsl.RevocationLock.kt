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

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockSchema.RevocationLockEntity
import io.onixlabs.corda.core.services.QueryDsl
import io.onixlabs.corda.core.services.QueryDslContext
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.isNull
import net.corda.core.contracts.LinearState
import net.corda.core.identity.AbstractParty
import java.util.*

@QueryDslContext
fun QueryDsl<RevocationLock<*>>.revocationLockOwner(value: AbstractParty) {
    expression(RevocationLockEntity::owner equalTo value)
}

@QueryDslContext
fun QueryDsl<RevocationLock<*>>.revocationLockPointerStateLinearId(value: UUID) {
    expression(RevocationLockEntity::pointerStateLinearId equalTo value)
}

@QueryDslContext
fun QueryDsl<RevocationLock<*>>.pointerStateExternalId(value: String?) {
    if (value == null) expression(RevocationLockEntity::pointerStateExternalId.isNull())
    else expression(RevocationLockEntity::pointerStateExternalId equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<RevocationLock<*>>.revocationLockPointerStateClass(value: Class<out LinearState>) {
    expression(RevocationLockEntity::pointerStateClass equalTo value.canonicalName)
}
