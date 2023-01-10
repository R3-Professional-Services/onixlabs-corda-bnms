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

import io.onixlabs.corda.bnms.contract.ContractTest
import io.onixlabs.corda.bnms.contract.IDENTITY_A
import io.onixlabs.corda.bnms.contract.IDENTITY_B
import io.onixlabs.corda.bnms.contract.REVOCATION_LOCK
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RevocationLockContractLockCommandTests : ContractTest() {

    @Test
    fun `On revocation lock locking, the transaction must include the Lock command`() {
        services.ledger {
            transaction {
                output(RevocationLockContract.ID, REVOCATION_LOCK)
                fails()
                command(keysOf(IDENTITY_A), RevocationLockContract.Lock)
                verifies()
            }
        }
    }

    @Test
    fun `On revocation lock locking, at least one revocation lock state must be created`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_A), RevocationLockContract.Lock)
                failsWith(RevocationLockContract.Lock.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock locking, the owner of the revocation lock state must sign the transaction`() {
        services.ledger {
            transaction {
                output(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_B), RevocationLockContract.Lock)
                failsWith(RevocationLockContract.Lock.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
