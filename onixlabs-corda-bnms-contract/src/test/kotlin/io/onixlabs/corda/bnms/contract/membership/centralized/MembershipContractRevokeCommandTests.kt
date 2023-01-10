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

package io.onixlabs.corda.bnms.contract.membership.centralized

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.membership.MembershipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipContractRevokeCommandTests : ContractTest() {

    @Test
    fun `On membership revoking, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                input(issuedMembershipA.ref)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On membership revoking, only one membership state must be consumed`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedMembershipB = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_B)
                input(issuedMembershipA.ref)
                input(issuedMembershipB.ref)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership revoking, zero membership states must be created`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                val issuedMembershipB = CENTRALIZED_MEMBERSHIP_IDENTITY_B
                input(issuedMembershipA.ref)
                output(MembershipContract.ID, issuedMembershipB)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership revoking, either the holder or the network operator of the consumed membership state must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedMembershipA = issue(CENTRALIZED_MEMBERSHIP_IDENTITY_A)
                input(issuedMembershipA.ref)
                command(keysOf(IDENTITY_B), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
