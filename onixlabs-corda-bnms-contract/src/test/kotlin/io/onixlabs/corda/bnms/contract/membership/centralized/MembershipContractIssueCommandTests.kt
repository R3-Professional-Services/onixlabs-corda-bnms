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

class MembershipContractIssueCommandTests : ContractTest() {

    @Test
    fun `On membership issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                val issuedMembershipA = CENTRALIZED_MEMBERSHIP_IDENTITY_A
                output(MembershipContract.ID, issuedMembershipA)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership issuing, zero membership states must be consumed`() {
        services.ledger {
            transaction {
                val issuedMembershipA = CENTRALIZED_MEMBERSHIP_IDENTITY_A
                input(MembershipContract.ID, issuedMembershipA)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership issuing, only one membership state must be created`() {
        services.ledger {
            transaction {
                val issuedMembershipA = CENTRALIZED_MEMBERSHIP_IDENTITY_A
                val issuedMembershipB = CENTRALIZED_MEMBERSHIP_IDENTITY_B
                output(MembershipContract.ID, issuedMembershipA)
                output(MembershipContract.ID, issuedMembershipB)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership issuing, the previous state reference of the created membership state must be null`() {
        services.ledger {
            transaction {
                val issuedMembershipA = CENTRALIZED_MEMBERSHIP_IDENTITY_A.withWrongRef()
                output(MembershipContract.ID, issuedMembershipA)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_PREVIOUS_STATE_REF)
            }
        }
    }

    @Test
    fun `On membership issuing, either the holder or the network operator of the created membership state must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedMembershipA = CENTRALIZED_MEMBERSHIP_IDENTITY_A
                output(MembershipContract.ID, issuedMembershipA)
                command(keysOf(IDENTITY_B), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
