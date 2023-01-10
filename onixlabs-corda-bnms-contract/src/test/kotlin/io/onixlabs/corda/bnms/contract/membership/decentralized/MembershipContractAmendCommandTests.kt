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

package io.onixlabs.corda.bnms.contract.membership.decentralized

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.membership.MembershipContract
import io.onixlabs.corda.bnms.contract.membership.getNextOutput
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipContractAmendCommandTests : ContractTest() {

    @Test
    fun `On membership amending, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(DECENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val amendedMembershipA = issuedMembershipA.getNextOutput().configure { addRoles("Example") }
                input(issuedMembershipA.ref)
                output(MembershipContract.ID, amendedMembershipA)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On membership amending, only one membership state must be consumed`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(DECENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedMembershipB = issue(DECENTRALIZED_MEMBERSHIP_IDENTITY_B)
                input(issuedMembershipA.ref)
                input(issuedMembershipB.ref)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership amending, only one membership state must be created`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(DECENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedMembershipB = DECENTRALIZED_MEMBERSHIP_IDENTITY_B
                val amendedMembershipA = issuedMembershipA.getNextOutput().configure { addRoles("Example") }
                input(issuedMembershipA.ref)
                output(MembershipContract.ID, amendedMembershipA)
                output(MembershipContract.ID, issuedMembershipB)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership amending, the network, holder and linear ID of the membership must not change`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(DECENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val amendedMembershipA = issuedMembershipA.getNextOutput().withWrongNetwork()
                input(issuedMembershipA.ref)
                output(MembershipContract.ID, amendedMembershipA)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_CHANGES)
            }
        }
    }

    @Test
    fun `On membership amending, the previous state reference of the created membership state must be equal to the state reference of the consumed membership state`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(DECENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val amendedMembershipA = issuedMembershipA.getNextOutput().withWrongRef()
                input(issuedMembershipA.ref)
                output(MembershipContract.ID, amendedMembershipA)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_PREVIOUS_STATE_REF)
            }
        }
    }

    @Test
    fun `On membership amending, either the holder or the network operator of the created membership state must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(DECENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val amendedMembershipA = issuedMembershipA.getNextOutput()
                input(issuedMembershipA.ref)
                output(MembershipContract.ID, amendedMembershipA)
                command(keysOf(IDENTITY_B), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
