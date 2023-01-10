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

import io.onixlabs.corda.identityframework.contract.attestations.AttestationContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class RelationshipAttestationContract : AttestationContract(), Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    internal object Issue {
        const val CONTRACT_RULE_REFERENCES =
            "On relationship attestation issuing, only one relationship state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On relationship attestation issuing, the attestation pointer must point to the referenced relationship state."

        const val CONTRACT_RULE_NETWORK =
            "On relationship attestation issuing, the attestation network must be equal to the relationship network."

        const val CONTRACT_RULE_ATTESTOR =
            "On relationship attestation issuing, the attestor must be a participant of the referenced relationship state."
    }

    internal object Amend {
        const val CONTRACT_RULE_REFERENCES =
            "On relationship attestation amending, only one relationship state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On relationship attestation amending, the attestation pointer must point to the referenced relationship state."

        const val CONTRACT_RULE_NETWORK =
            "On relationship attestation amending, the attestation network must be equal to the relationship network."
    }

    override fun onVerifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val references = transaction.referenceInputRefsOfType<Relationship>()

        Issue.CONTRACT_RULE_REFERENCES using (references.size == 1)

        val relationship = references.single()
        val attestation = transaction.outputsOfType<RelationshipAttestation>().single()

        Issue.CONTRACT_RULE_POINTER using (attestation.pointer.isPointingTo(relationship))
        Issue.CONTRACT_RULE_NETWORK using (attestation.network == relationship.state.data.network)
        Issue.CONTRACT_RULE_ATTESTOR using (attestation.attestor in relationship.state.data.participants)
    }

    override fun onVerifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val references = transaction.referenceInputRefsOfType<Relationship>()

        Amend.CONTRACT_RULE_REFERENCES using (references.size == 1)

        val relationship = references.single()
        val attestation = transaction.outputsOfType<RelationshipAttestation>().single()

        Amend.CONTRACT_RULE_POINTER using (attestation.pointer.isPointingTo(relationship))
        Amend.CONTRACT_RULE_NETWORK using (attestation.network == relationship.state.data.network)
    }
}
