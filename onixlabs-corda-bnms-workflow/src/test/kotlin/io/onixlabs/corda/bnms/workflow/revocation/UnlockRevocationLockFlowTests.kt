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

package io.onixlabs.corda.bnms.workflow.revocation

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.internal.vault.DummyLinearContract
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class UnlockRevocationLockFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                LockRevocationLockFlow(REVOCATION_LOCK)
            }
            .run(nodeA) {
                val lock = it.tx.outRefsOfType<RevocationLock<DummyLinearContract.State>>().single()
                UnlockRevocationLockFlow(lock)
            }
            .finally { transaction = it }
    }

    @Test
    fun `DeleteRevocationLockFlow should be signed by the owner`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `DeleteRevocationLockFlow should record a transaction for the owner`() {
        nodeA.transaction {
            val recordedTransaction = nodeA.services.validatedTransactions.getTransaction(transaction.id)
                ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

            assertEquals(transaction, recordedTransaction)
        }
    }
}
