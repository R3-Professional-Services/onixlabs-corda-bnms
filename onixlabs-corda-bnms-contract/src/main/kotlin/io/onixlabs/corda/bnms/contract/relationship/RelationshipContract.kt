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

package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class RelationshipContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<RelationshipContractCommand>()
        when (command.value) {
            is Issue, is Amend, is Revoke -> command.value.verifyCommand(tx, command.signers.toSet())
            else -> throw IllegalArgumentException("Unrecognised command: ${command.value}")
        }
    }

    interface RelationshipContractCommand : CommandData {
        fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>)
    }

    object Issue : RelationshipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On relationship issuing, zero relationship states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On relationship issuing, only one relationship state must be created."

        internal const val CONTRACT_RULE_LOCKS =
            "On relationship issuing, revocation locks must be issued for all relationship members."

        internal const val CONTRACT_REVOCATION_LOCK_POINTERS =
            "On relationship issuing, all revocation locks must point to the relationship state."

        internal const val CONTRACT_RULE_PREVIOUS_STATE_REF =
            "On relationship issuing, the previous state reference of the created relationship state must be null."

        internal const val CONTRACT_RULE_SIGNERS =
            "On relationship issuing, all participants must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val relationshipInputs = transaction.inputsOfType<Relationship>()
            val relationshipOutputs = transaction.outputsOfType<Relationship>()

            CONTRACT_RULE_INPUTS using (relationshipInputs.isEmpty())
            CONTRACT_RULE_OUTPUTS using (relationshipOutputs.size == 1)

            val relationshipOutput = relationshipOutputs.single()
            val locks = transaction.outputsOfType<RevocationLock<Relationship>>()
            val validLockCount = relationshipOutput.members.size == locks.size
            val locksForAllMembers = locks.all { it.owner in relationshipOutput.participants }

            CONTRACT_RULE_LOCKS using (validLockCount && locksForAllMembers)
            CONTRACT_REVOCATION_LOCK_POINTERS using (locks.all { it.pointer.pointer == relationshipOutput.linearId })
            CONTRACT_RULE_PREVIOUS_STATE_REF using (relationshipOutput.previousStateRef == null)
            CONTRACT_RULE_SIGNERS using (relationshipOutput.participants.all { it.owningKey in signers })
        }
    }

    object Amend : RelationshipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On relationship amending, only one relationship state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On relationship amending, only one relationship state must be created."

        internal const val CONTRACT_RULE_CHANGES =
            "On relationship amending, the network and linear ID of the relationship must not change."

        internal const val CONTRACT_RULE_PREVIOUS_STATE_REF =
            "On relationship amending, the previous state reference must be equal to the input state reference."

        internal const val CONTRACT_RULE_SIGNERS =
            "On relationship amending, all participants must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inRefsOfType<Relationship>()
            val outputs = transaction.outputsOfType<Relationship>()

            CONTRACT_RULE_INPUTS using (inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (outputs.size == 1)

            val input = inputs.single()
            val output = outputs.single()

            CONTRACT_RULE_CHANGES using (input.state.data.immutableEquals(output))
            CONTRACT_RULE_PREVIOUS_STATE_REF using (input.ref == output.previousStateRef)
            CONTRACT_RULE_SIGNERS using (output.participants.all { it.owningKey in signers })
        }
    }

    object Revoke : RelationshipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On relationship revoking, only one relationship state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On relationship revoking, zero relationship states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On relationship revoking, all participants must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (transaction.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (transaction.outputs.isEmpty())

            val relationshipInputState = transaction.inputsOfType<Relationship>().single()

            CONTRACT_RULE_SIGNERS using (relationshipInputState.participants.all { it.owningKey in signers })
        }
    }
}
