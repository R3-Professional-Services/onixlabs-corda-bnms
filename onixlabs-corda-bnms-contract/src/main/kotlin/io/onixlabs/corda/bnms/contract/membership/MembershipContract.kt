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

import io.onixlabs.corda.core.contract.ContractID
import io.onixlabs.corda.core.contract.VerifiedCommandData
import io.onixlabs.corda.core.contract.allowCommands
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class MembershipContract : Contract {

    companion object : ContractID

    override fun verify(tx: LedgerTransaction) = tx.allowCommands(
        Issue::class.java,
        Amend::class.java,
        Revoke::class.java
    )

    interface MembershipContractCommand : VerifiedCommandData

    object Issue : MembershipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership issuing, zero membership states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership issuing, only one membership state must be created."

        internal const val CONTRACT_RULE_PREVIOUS_STATE_REF =
            "On membership issuing, the previous state reference of the created membership state must be null."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership issuing, either the holder or the network operator of the created membership state must sign the transaction."

        override fun verify(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inputsOfType<Membership>()
            val outputs = transaction.outputsOfType<Membership>()

            CONTRACT_RULE_INPUTS using (inputs.isEmpty())
            CONTRACT_RULE_OUTPUTS using (outputs.size == 1)

            val output = outputs.single()

            CONTRACT_RULE_PREVIOUS_STATE_REF using (output.previousStateRef == null)
            CONTRACT_RULE_SIGNERS using (output.participants.any { it.owningKey in signers })
        }
    }

    object Amend : MembershipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership amending, only one membership state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership amending, only one membership state must be created."

        internal const val CONTRACT_RULE_CHANGES =
            "On membership amending, the network, holder and linear ID of the membership must not change."

        internal const val CONTRACT_RULE_PREVIOUS_STATE_REF =
            "On membership amending, the previous state reference of the created membership state must be equal to the state reference of the consumed membership state."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership amending, either the holder or the network operator of the created membership state must sign the transaction."

        override fun verify(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inRefsOfType<Membership>()
            val outputs = transaction.outputsOfType<Membership>()

            CONTRACT_RULE_INPUTS using (inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (outputs.size == 1)

            val input = inputs.single()
            val output = outputs.single()

            CONTRACT_RULE_CHANGES using (input.state.data.immutableEquals(output))
            CONTRACT_RULE_PREVIOUS_STATE_REF using (input.ref == output.previousStateRef)
            CONTRACT_RULE_SIGNERS using (output.participants.any { it.owningKey in signers })
        }
    }

    object Revoke : MembershipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership revoking, only one membership state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership revoking, zero membership states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership revoking, either the holder or the network operator of the consumed membership state must sign the transaction."

        override fun verify(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inRefsOfType<Membership>()
            val outputs = transaction.outputsOfType<Membership>()

            CONTRACT_RULE_INPUTS using (inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (outputs.isEmpty())

            val input = inputs.single()

            CONTRACT_RULE_SIGNERS using (input.state.data.participants.any { it.owningKey in signers })
        }
    }
}
