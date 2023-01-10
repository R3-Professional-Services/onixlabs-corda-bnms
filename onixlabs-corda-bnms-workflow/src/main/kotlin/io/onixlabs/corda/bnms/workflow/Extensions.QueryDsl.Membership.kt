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
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.core.services.QueryDsl
import io.onixlabs.corda.core.services.QueryDslContext
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.isNull
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty

@QueryDslContext
fun QueryDsl<Membership>.membershipHolder(value: AbstractParty) {
    expression(MembershipEntity::holder equalTo value)
}

@QueryDslContext
fun QueryDsl<Membership>.membershipNetworkValue(value: String) {
    expression(MembershipEntity::networkValue equalTo value)
}

@QueryDslContext
fun QueryDsl<Membership>.membershipNormalizedNetworkValue(value: String) {
    expression(MembershipEntity::normalizedNetworkValue equalTo value.toUpperCase())
}

@QueryDslContext
fun QueryDsl<Membership>.membershipNetworkOperator(value: AbstractParty?) {
    if (value == null) expression(MembershipEntity::networkOperator.isNull())
    else expression(MembershipEntity::networkOperator equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<Membership>.membershipNetworkHash(value: SecureHash) {
    expression(MembershipEntity::networkHash equalTo value.toString())
}

@QueryDslContext
fun QueryDsl<Membership>.membershipIsNetworkOperator(value: Boolean) {
    expression(MembershipEntity::isNetworkOperator equalTo value)
}

@QueryDslContext
fun QueryDsl<Membership>.membershipHash(value: SecureHash) {
    expression(MembershipEntity::hash equalTo value.toString())
}
