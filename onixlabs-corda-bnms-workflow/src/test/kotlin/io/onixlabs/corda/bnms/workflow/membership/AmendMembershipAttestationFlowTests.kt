package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.accept
import io.onixlabs.corda.bnms.contract.membership.reject
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import io.onixlabs.corda.identity.framework.contract.AttestationStatus
import io.onixlabs.corda.identity.framework.workflow.IssueEvolvableAttestationFlow
import net.corda.core.contracts.NoConstraintPropagation
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class AmendMembershipAttestationFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var attestation: MembershipAttestation

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueMembershipFlow.Initiator(MEMBERSHIP, observers = setOf(partyB))
            }
            .run(nodeB) {
                val membership = it.tx.outRefsOfType<Membership>().single()
                val attestation = membership.accept(partyB)
                IssueMembershipAttestationFlow.Initiator(attestation)
            }
            .run(nodeB) {
                val oldAttestation = it.tx.outRefsOfType<MembershipAttestation>().single()
                attestation = oldAttestation.reject()
                AmendMembershipAttestationFlow.Initiator(oldAttestation, attestation)
            }
            .finally { transaction = it }
    }

    @Test
    fun `AmendMembershipAttestationFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipAttestationFlow should record a transaction for the attestor and membership holder`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `AmendMembershipAttestationFlow should record an attestation for the attestor and membership holder`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedAttestation = recordedTransaction.tx.outputsOfType<MembershipAttestation>().singleOrNull()
                    ?: fail("Failed to find a recorded membership attestation.")

                assertEquals(attestation, recordedAttestation)
                assertEquals(attestation.status, AttestationStatus.REJECTED)
            }
        }
    }
}