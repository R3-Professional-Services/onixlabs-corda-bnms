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

package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.accept
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class AmendDuplicateMembershipAttestationFlowTests : FlowTest() {

    private lateinit var membership: StateAndRef<Membership>
    private lateinit var oldAttestation: StateAndRef<MembershipAttestation>
    private lateinit var newAttestation: MembershipAttestation
    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueMembershipFlow.Initiator(MEMBERSHIP, observers = setOf(partyB))
            }
            .run(nodeB) {
                membership = it.tx.outRefsOfType<Membership>().single()
                val attestation = membership.accept(partyB)
                IssueMembershipAttestationFlow.Initiator(attestation)
            }
            .finally {
                oldAttestation = it.tx.outRefsOfType<MembershipAttestation>().single()
                transaction = it
            }
    }

    @Test
    fun `AmendMembershipAttestationFlow should fail because an existing membership attestation already exists`() {
        val exception = assertFailsWith<FlowException> {
            Pipeline
                .create(network)
                .run(nodeB) {
                    newAttestation = oldAttestation.state.data
                    AmendMembershipAttestationFlow.Initiator(oldAttestation, newAttestation)
                }
        }

        assert(exception.message!!.startsWith("The specified membership attestation already exists:"))
    }
}
