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
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.identityframework.workflow.addAmendedAttestation
import io.onixlabs.corda.identityframework.workflow.addIssuedAttestation
import io.onixlabs.corda.identityframework.workflow.addRevokedAttestation
import net.corda.core.contracts.ReferencedStateAndRef
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.TransactionBuilder

@Suspendable
fun TransactionBuilder.addIssuedRelationshipAttestation(
    relationshipAttestation: RelationshipAttestation,
    relationship: ReferencedStateAndRef<Relationship>
): TransactionBuilder = apply {
    addIssuedAttestation(relationshipAttestation)
    addReferenceState(relationship)
}

@Suspendable
fun TransactionBuilder.addAmendedRelationshipAttestation(
    oldRelationshipAttestation: StateAndRef<RelationshipAttestation>,
    newRelationshipAttestation: RelationshipAttestation,
    relationship: ReferencedStateAndRef<Relationship>
): TransactionBuilder = apply {
    addAmendedAttestation(oldRelationshipAttestation, newRelationshipAttestation)
    addReferenceState(relationship)
}

@Suspendable
fun TransactionBuilder.addRevokedRelationshipAttestation(
    relationshipAttestation: StateAndRef<RelationshipAttestation>
): TransactionBuilder = apply {
    addRevokedAttestation(relationshipAttestation)
}
