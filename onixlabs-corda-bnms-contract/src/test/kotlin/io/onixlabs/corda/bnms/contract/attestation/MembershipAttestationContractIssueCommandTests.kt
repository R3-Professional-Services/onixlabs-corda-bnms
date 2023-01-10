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
import io.onixlabs.corda.bnms.contract.membership.accept
import io.onixlabs.corda.identityframework.contract.attestations.AttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipAttestationContractIssueCommandTests : ContractTest() {

    @Test
    fun `On membership attestation issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedAttestationA = issuedMembershipA.accept(OPERATOR_A.party)
                output(MembershipAttestationContract.ID, issuedAttestationA)
                reference(issuedMembershipA.ref)
                fails()
                command(keysOf(OPERATOR_A), AttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation issuing, only one membership state must be referenced`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedMembershipB = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_B)
                val issuedAttestationA = issuedMembershipA.accept(OPERATOR_A.party)
                output(MembershipAttestationContract.ID, issuedAttestationA)
                reference(issuedMembershipA.ref)
                reference(issuedMembershipB.ref)
                command(keysOf(OPERATOR_A), AttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, the attestation pointer must point to the referenced membership state`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedMembershipB = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_B)
                val issuedAttestationA = issuedMembershipA.accept(OPERATOR_A.party)
                output(MembershipAttestationContract.ID, issuedAttestationA)
                reference(issuedMembershipB.ref)
                command(keysOf(OPERATOR_A), AttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, the attestation holder must be equal to the membership holder`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedAttestationA = issuedMembershipA.accept(OPERATOR_A.party).withWrongHolder()
                output(MembershipAttestationContract.ID, issuedAttestationA)
                reference(issuedMembershipA.ref)
                command(keysOf(OPERATOR_A), AttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_HOLDER)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, the attestation network must be equal to the membership network`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedAttestationA = issuedMembershipA.accept(OPERATOR_A.party).withWrongNetwork()
                output(MembershipAttestationContract.ID, issuedAttestationA)
                reference(issuedMembershipA.ref)
                command(keysOf(OPERATOR_A), AttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_NETWORK)
            }
        }
    }

//    @Test
//    fun `On membership attestation issuing, if present, only the network operator can attest a membership state`() {
//        services.ledger {
//            transaction {
//                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
//                val issuedAttestationA = issuedMembershipA.accept(IDENTITY_C.party)
//                output(MembershipAttestationContract.ID, issuedAttestationA)
//                reference(issuedMembershipA.ref)
//                command(keysOf(IDENTITY_C), AttestationContract.Issue)
//                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_OPERATOR_ATTESTATION)
//            }
//        }
//    }

//    @Test
//    fun `On membership attestation issuing, if present, only the network operator can self-attest their membership state`() {
//        services.ledger {
//            transaction {
//                val issuedMembershipA = issue(DECENTRALIZED_MEMBERSHIP_IDENTITY_A)
//                val issuedAttestationA = issuedMembershipA.accept(IDENTITY_A.party)
//                output(MembershipAttestationContract.ID, issuedAttestationA)
//                reference(issuedMembershipA.ref)
//                command(keysOf(IDENTITY_A), AttestationContract.Issue)
//                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_SELF_ATTESTATION)
//            }
//        }
//    }
}
