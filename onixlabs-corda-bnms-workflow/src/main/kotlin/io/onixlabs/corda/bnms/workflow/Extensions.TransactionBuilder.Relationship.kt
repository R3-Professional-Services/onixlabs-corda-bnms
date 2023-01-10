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
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipContract
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.TransactionBuilder

@Suspendable
fun TransactionBuilder.addIssuedRelationship(
    relationship: Relationship
): TransactionBuilder = apply {
    addOutputState(relationship, RelationshipContract.ID)
    relationship.createRevocationLocks().forEach { addOutputState(it) }
    addCommand(RelationshipContract.Issue, relationship.participants.map { it.owningKey })
    addCommand(RevocationLockContract.Lock, relationship.participants.map { it.owningKey })
}

@Suspendable
fun TransactionBuilder.addAmendedRelationship(
    oldRelationship: StateAndRef<Relationship>,
    newRelationship: Relationship
): TransactionBuilder = apply {
    addInputState(oldRelationship)
    addOutputState(newRelationship, RelationshipContract.ID)
    addCommand(RelationshipContract.Amend, newRelationship.participants.map { it.owningKey })
}

@Suspendable
fun TransactionBuilder.addRevokedRelationship(
    relationship: StateAndRef<Relationship>
): TransactionBuilder = apply {
    addInputState(relationship)
    addCommand(RelationshipContract.Revoke, relationship.state.data.participants.map { it.owningKey })
}
