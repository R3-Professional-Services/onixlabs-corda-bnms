/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipContract
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.core.workflow.initiateFlows
import io.onixlabs.corda.identityframework.workflow.*
import io.onixlabs.corda.bnms.workflow.*
import io.onixlabs.corda.core.workflow.checkSufficientSessions
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class RevokeMembershipFlow(
    private val membership: StateAndRef<Membership>,
    private val sessions: Set<FlowSession> = emptySet(),
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, GENERATING, VERIFYING, SIGNING, FINALIZING)

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(INITIALIZING)
        checkSufficientSessions(sessions, membership.state.data)

        val transaction = transaction(membership.state.notary) {
            addInputState(membership)
            addCommand(MembershipContract.Revoke, ourIdentity.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction, ourIdentity.owningKey)
        return finalize(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val membership: StateAndRef<Membership>,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object REVOKING : Step("Revoking membership.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(REVOKING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(REVOKING)
            val sessions = initiateFlows(observers, membership.state.data)

            return subFlow(
                RevokeMembershipFlow(
                    membership,
                    sessions,
                    REVOKING.childProgressTracker()
                )
            )
        }
    }
}
