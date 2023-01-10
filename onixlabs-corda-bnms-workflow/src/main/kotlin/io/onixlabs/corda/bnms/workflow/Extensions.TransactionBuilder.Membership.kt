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

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.TransactionBuilder
import java.security.PublicKey

@Suspendable
fun TransactionBuilder.addIssuedMembership(
    state: Membership,
    signingKey: PublicKey
): TransactionBuilder = apply {
    addOutputState(state)
    addCommand(MembershipContract.Issue, signingKey)
}

@Suspendable
fun TransactionBuilder.addAmendedMembership(
    oldMembership: StateAndRef<Membership>,
    newMembership: Membership,
    signingKey: PublicKey
): TransactionBuilder = apply {
    addInputState(oldMembership)
    addOutputState(newMembership, MembershipContract.ID)
    addCommand(MembershipContract.Amend, signingKey)
}

@Suspendable
fun TransactionBuilder.addRevokedMembership(
    membership: StateAndRef<Membership>,
    signingKey: PublicKey
): TransactionBuilder = apply {
    addInputState(membership)
    addCommand(MembershipContract.Revoke, signingKey)
}
