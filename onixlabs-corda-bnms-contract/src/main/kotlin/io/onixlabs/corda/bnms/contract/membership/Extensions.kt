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

import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.corda.identityframework.contract.toStaticAttestationPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

/**
 * Gets the next [Membership] output, appending the previous [Membership] state's [StateRef].
 *
 * @return Returns the next [Membership] output, appending the previous [Membership] state's [StateRef].
 */
fun StateAndRef<Membership>.getNextOutput(): Membership {
    return state.data.copy(previousStateRef = ref)
}

/**
 * Creates a [MembershipAttestation] for the specified [Membership].
 *
 * @param attestor The attestor who is attesting to the [Membership].
 * @param status The status of the attestation.
 * @param metadata Any extra metadata that should be applied to the attestation.
 * @param linearId The linear ID of the attestation.
 * @return Returns a [MembershipAttestation] for the specified [Membership].
 */
fun StateAndRef<Membership>.attest(
    attestor: AbstractParty,
    status: AttestationStatus,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
) = MembershipAttestation(attestor, this, status, metadata, linearId)

/**
 * Creates an accepted [MembershipAttestation] for the specified [Membership].
 *
 * @param attestor The attestor who is attesting to the [Membership].
 * @param metadata Any extra metadata that should be applied to the attestation.
 * @param linearId The linear ID of the attestation.
 * @return Returns an accepted [MembershipAttestation] for the specified [Membership].
 */
fun StateAndRef<Membership>.accept(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
) = attest(attestor, AttestationStatus.ACCEPTED, metadata, linearId)

/**
 * Creates a rejected [MembershipAttestation] for the specified [Membership].
 *
 * @param attestor The attestor who is attesting to the [Membership].
 * @param metadata Any extra metadata that should be applied to the attestation.
 * @param linearId The linear ID of the attestation.
 * @return Returns a rejected [MembershipAttestation] for the specified [Membership].
 */
fun StateAndRef<Membership>.reject(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
) = attest(attestor, AttestationStatus.REJECTED, metadata, linearId)

/**
 * Amends a [MembershipAttestation] pointing to the existing [Membership] state.
 *
 * @param status The amended status of the [MembershipAttestation].
 * @param metadata Any extra metadata that should be applied to the attestation.
 * @return Returns an amended [MembershipAttestation] pointing to the existing [Membership] state.
 */
fun StateAndRef<MembershipAttestation>.amend(
    status: AttestationStatus,
    metadata: Map<String, String> = this.state.data.metadata
) = this.state.data.amend(ref, status, state.data.pointer, metadata)

/**
 * Amends a [MembershipAttestation] pointing to an evolved [Membership] state.
 *
 * @param status The amended status of the [MembershipAttestation].
 * @param membership The evolved [Membership] state that the amended [MembershipAttestation] will point to.
 * @param metadata Any extra metadata that should be applied to the attestation.
 * @return Returns an amended [MembershipAttestation] pointing to an evolved [Membership] state.
 */
fun StateAndRef<MembershipAttestation>.amend(
    membership: StateAndRef<Membership>,
    status: AttestationStatus,
    metadata: Map<String, String> = this.state.data.metadata
) = this.state.data.amend(ref, status, membership.toStaticAttestationPointer(), metadata)

/**
 * Amends and accepts a [MembershipAttestation] pointing to the existing [Membership] state.
 *
 * @param metadata Any extra metadata that should be applied to the attestation.
 * @return Returns an amended and accepted [MembershipAttestation] pointing to the existing [Membership] state.
 */
fun StateAndRef<MembershipAttestation>.accept(
    metadata: Map<String, String> = this.state.data.metadata
) = amend(AttestationStatus.ACCEPTED, metadata)

/**
 * Amends and accepts a [MembershipAttestation] pointing to an evolved [Membership] state.
 *
 * @param membership The evolved [Membership] state that the amended [MembershipAttestation] will point to.
 * @param metadata Any extra metadata that should be applied to the attestation.
 * @return Returns an amended and accepted [MembershipAttestation] pointing to an evolved [Membership] state.
 */
fun StateAndRef<MembershipAttestation>.accept(
    membership: StateAndRef<Membership>,
    metadata: Map<String, String> = this.state.data.metadata
) = amend(membership, AttestationStatus.ACCEPTED, metadata)

/**
 * Amends and rejects a [MembershipAttestation] pointing to the existing [Membership] state.
 *
 * @param metadata Any extra metadata that should be applied to the attestation.
 * @return Returns an amended and rejected [MembershipAttestation] pointing to the existing [Membership] state.
 */
fun StateAndRef<MembershipAttestation>.reject(
    metadata: Map<String, String> = this.state.data.metadata
) = amend(AttestationStatus.REJECTED, metadata)

/**
 * Amends and rejects a [MembershipAttestation] pointing to an evolved [Membership] state.
 *
 * @param membership The evolved [Membership] state that the amended [MembershipAttestation] will point to.
 * @param metadata Any extra metadata that should be applied to the attestation.
 * @return Returns an amended and rejected [MembershipAttestation] pointing to an evolved [Membership] state.
 */
fun StateAndRef<MembershipAttestation>.reject(
    membership: StateAndRef<Membership>,
    metadata: Map<String, String> = this.state.data.metadata
) = amend(membership, AttestationStatus.REJECTED, metadata)
