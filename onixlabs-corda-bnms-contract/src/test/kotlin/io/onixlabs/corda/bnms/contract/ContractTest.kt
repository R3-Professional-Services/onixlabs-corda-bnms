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

package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.bnms.contract.membership.*
import io.onixlabs.corda.bnms.contract.relationship.*
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import io.onixlabs.corda.identityframework.contract.attestations.AttestationContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.node.NotaryInfo
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import org.junit.jupiter.api.BeforeEach

abstract class ContractTest {

    companion object {
        private val cordapps = listOf(
            "io.onixlabs.corda.identityframework.contract",
            "io.onixlabs.corda.bnms.contract"
        )

        private val contracts = listOf(
            MembershipContract.ID,
            RelationshipContract.ID,
            RevocationLockContract.ID,
            MembershipAttestationContract.ID,
            RelationshipAttestationContract.ID
        )

        fun keysOf(vararg identities: TestIdentity) = identities.map { it.publicKey }
    }

    private lateinit var _services: MockServices
    protected val services: MockServices get() = _services

    @BeforeEach
    private fun setup() {
        val networkParameters = testNetworkParameters(
            minimumPlatformVersion = 11,
            notaries = listOf(NotaryInfo(TestIdentity(DUMMY_NOTARY_NAME, 20).party, true))
        )
        _services = MockServices(cordapps, IDENTITY_A, networkParameters, IDENTITY_B, IDENTITY_C)
        contracts.forEach { _services.addMockCordapp(it) }
    }

    protected fun MockLedger.issue(membership: Membership): StateAndRef<Membership> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(MembershipContract.ID, label, membership)
            command(listOf(membership.holder.owningKey), MembershipContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(Membership::class.java, label)
    }

    protected fun MockLedger.attestMembership(
        membership: StateAndRef<Membership>,
        attestor: AbstractParty
    ): StateAndRef<MembershipAttestation> {
        val label = SecureHash.randomSHA256().toString()
        val attestation = membership.accept(attestor)

        transaction {
            output(MembershipAttestationContract.ID, label, attestation)
            reference(membership.ref)
            command(listOf(attestation.attestor.owningKey), AttestationContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(MembershipAttestation::class.java, label)
    }

    protected fun MockLedger.issue(relationship: Relationship): StateAndRef<Relationship> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(RelationshipContract.ID, label, relationship)
            relationship.createRevocationLocks().forEach { output(RevocationLockContract.ID, it) }
            command(relationship.participants.map { it.owningKey }, RelationshipContract.Issue)
            command(relationship.participants.map { it.owningKey }, RevocationLockContract.Lock)
            verifies()
        }

        return retrieveOutputStateAndRef(Relationship::class.java, label)
    }

    protected fun MockLedger.attestRelationship(
        relationship: StateAndRef<Relationship>,
        attestor: AbstractParty
    ): StateAndRef<RelationshipAttestation> {
        val label = SecureHash.randomSHA256().toString()
        val attestation = relationship.accept(attestor)

        transaction {
            output(RelationshipAttestationContract.ID, label, attestation)
            reference(relationship.ref)
            command(listOf(attestation.attestor.owningKey), AttestationContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(RelationshipAttestation::class.java, label)
    }
}
