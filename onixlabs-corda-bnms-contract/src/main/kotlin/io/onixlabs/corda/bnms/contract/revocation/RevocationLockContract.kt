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

package io.onixlabs.corda.bnms.contract.revocation

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class RevocationLockContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<RevocationLockContractCommand>()
        when (command.value) {
            is Lock, is Unlock -> command.value.verifyCommand(tx, command.signers.toSet())
            else -> throw IllegalArgumentException("Unrecognised command: ${command.value}")
        }
    }

    interface RevocationLockContractCommand : CommandData {
        fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>)
    }

    object Lock : RevocationLockContractCommand {

        internal const val CONTRACT_RULE_OUTPUTS =
            "On revocation lock locking, at least one revocation lock state must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On revocation lock locking, the owner of the revocation lock state must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val outputs = transaction.outputsOfType<RevocationLock<*>>()
            CONTRACT_RULE_OUTPUTS using (outputs.isNotEmpty())
            CONTRACT_RULE_SIGNERS using (outputs.all { it.owner.owningKey in signers })
        }
    }

    object Unlock : RevocationLockContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On revocation lock unlocking, only one revocation lock state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On revocation lock unlocking, zero revocation lock states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On revocation lock unlocking, the owner of the revocation lock state must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inputsOfType<RevocationLock<*>>()
            val outputs = transaction.outputsOfType<RevocationLock<*>>()

            CONTRACT_RULE_INPUTS using (inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (outputs.isEmpty())

            val input = inputs.single()

            CONTRACT_RULE_SIGNERS using (input.owner.owningKey in signers)
        }
    }
}
