/*
 * Copyright 2020-2021 ONIXLabs
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

import io.onixlabs.corda.identityframework.contract.AttestationContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class MembershipAttestationContract : AttestationContract(), Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    internal object Issue {
        const val CONTRACT_RULE_REFERENCES =
            "On membership attestation issuing, only one membership state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On membership attestation issuing, the attestation pointer must point to the referenced membership state."

        const val CONTRACT_RULE_HOLDER =
            "On membership attestation issuing, the attestation holder must be equal to the membership holder."

        const val CONTRACT_RULE_NETWORK =
            "On membership attestation issuing, the attestation network must be equal to the membership network."

        const val CONTRACT_RULE_OPERATOR_ATTESTATION =
            "On membership attestation issuing, if present, only the network operator can attest a membership state."

        const val CONTRACT_RULE_SELF_ATTESTATION =
            "On membership attestation issuing, if present, only the network operator can self-attest their membership state."
    }

    internal object Amend {
        const val CONTRACT_RULE_REFERENCES =
            "On membership attestation amending, only one membership state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On membership attestation amending, the attestation pointer must point to the referenced membership state."

        const val CONTRACT_RULE_HOLDER =
            "On membership attestation amending, the attestation holder must be equal to the membership holder."

        const val CONTRACT_RULE_NETWORK =
            "On membership attestation amending, the attestation network must be equal to the membership network."
    }

    override fun onVerifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val memberships = transaction.referenceInputRefsOfType<Membership>()

        Issue.CONTRACT_RULE_REFERENCES using (memberships.size == 1)

        val attestation = transaction.outputsOfType<MembershipAttestation>().single()
        val membership = memberships.single()

        Issue.CONTRACT_RULE_POINTER using (attestation.pointer.isPointingTo(membership))
        Issue.CONTRACT_RULE_HOLDER using (attestation.holder == membership.state.data.holder)
        Issue.CONTRACT_RULE_NETWORK using (attestation.network == membership.state.data.network)
       // Issue.CONTRACT_RULE_OPERATOR_ATTESTATION using (attestation.network.operator ?: attestation.attestor == attestation.attestor)
       // Issue.CONTRACT_RULE_SELF_ATTESTATION using (attestation.isNetworkOperator || attestation.attestor != attestation.holder)
    }

    override fun onVerifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val memberships = transaction.referenceInputRefsOfType<Membership>()

        Amend.CONTRACT_RULE_REFERENCES using (memberships.size == 1)

        val attestation = transaction.outputsOfType<MembershipAttestation>().single()
        val membership = memberships.single()

        Amend.CONTRACT_RULE_POINTER using (attestation.pointer.isPointingTo(membership))
        Amend.CONTRACT_RULE_HOLDER using (attestation.holder == membership.state.data.holder)
        Amend.CONTRACT_RULE_NETWORK using (attestation.network == membership.state.data.network)
    }
}
