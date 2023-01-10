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

package io.onixlabs.corda.bnms.contract.attestation

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationContract
import io.onixlabs.corda.bnms.contract.membership.reject
import io.onixlabs.corda.identityframework.contract.attestations.AttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipAttestationContractAmendCommandTests : ContractTest() {

    @Test
    fun `On membership attestation amending, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedAttestationA = attestMembership(issuedMembershipA, OPERATOR_A.party)
                val amendedAttestationA = issuedAttestationA.reject()
                input(issuedAttestationA.ref)
                output(MembershipAttestationContract.ID, amendedAttestationA)
                reference(issuedMembershipA.ref)
                fails()
                command(keysOf(OPERATOR_A), AttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation amending, only one membership state must be referenced`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedMembershipB = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_B)
                val issuedAttestationA = attestMembership(issuedMembershipA, OPERATOR_A.party)
                val amendedAttestationA = issuedAttestationA.reject()
                input(issuedAttestationA.ref)
                output(MembershipAttestationContract.ID, amendedAttestationA)
                reference(issuedMembershipA.ref)
                reference(issuedMembershipB.ref)
                command(keysOf(OPERATOR_A), AttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On membership attestation amending, the attestation pointer must point to the referenced membership state`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedMembershipB = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_B)
                val issuedAttestationA = attestMembership(issuedMembershipA, OPERATOR_A.party)
                val amendedAttestationA = issuedAttestationA.reject()
                input(issuedAttestationA.ref)
                output(MembershipAttestationContract.ID, amendedAttestationA)
                reference(issuedMembershipB.ref)
                command(keysOf(OPERATOR_A), AttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On membership attestation amending, the attestation holder must be equal to the membership holder`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedAttestationA = attestMembership(issuedMembershipA, OPERATOR_A.party)
                val amendedAttestationA = issuedAttestationA.reject().withWrongHolder()
                input(issuedAttestationA.ref)
                output(MembershipAttestationContract.ID, amendedAttestationA)
                reference(issuedMembershipA.ref)
                command(keysOf(OPERATOR_A), AttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_HOLDER)
            }
        }
    }

    @Test
    fun `On membership attestation amending, the attestation network must be equal to the membership network`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedAttestationA = attestMembership(issuedMembershipA, OPERATOR_A.party)
                val amendedAttestationA = issuedAttestationA.reject().withWrongNetwork()
                input(issuedAttestationA.ref)
                output(MembershipAttestationContract.ID, amendedAttestationA)
                reference(issuedMembershipA.ref)
                command(keysOf(OPERATOR_A), AttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_NETWORK)
            }
        }
    }
}
