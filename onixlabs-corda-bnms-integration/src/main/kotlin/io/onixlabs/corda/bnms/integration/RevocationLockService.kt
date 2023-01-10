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

package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.workflow.revocation.LockRevocationLockFlow
import io.onixlabs.corda.bnms.workflow.revocation.UnlockRevocationLockFlow
import io.onixlabs.corda.core.integration.RPCService
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.*
import net.corda.core.transactions.SignedTransaction
import java.util.*

class RevocationLockService(rpc: CordaRPCOps) : RPCService(rpc) {

    fun <T : LinearState> lock(
        state: T,
        owner: AbstractParty = ourIdentity,
        notary: Party? = null
    ): FlowProgressHandle<SignedTransaction> {
        val lock = RevocationLock(owner, state)
        return rpc.startTrackedFlow(
            ::LockRevocationLockFlow,
            lock,
            notary,
            LockRevocationLockFlow.tracker()
        )
    }

    fun <T : LinearState> lock(
        state: T,
        owner: AbstractParty = ourIdentity,
        notary: Party? = null,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        val lock = RevocationLock(owner, state)
        return rpc.startFlowWithClientId(
            clientId,
            ::LockRevocationLockFlow,
            lock,
            notary,
            LockRevocationLockFlow.tracker()
        )
    }

    fun <T : LinearState> unlock(
        lock: StateAndRef<RevocationLock<T>>
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            ::UnlockRevocationLockFlow,
            lock,
            UnlockRevocationLockFlow.tracker()
        )
    }

    fun <T : LinearState> unlock(
        lock: StateAndRef<RevocationLock<T>>,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            ::UnlockRevocationLockFlow,
            lock,
            UnlockRevocationLockFlow.tracker()
        )
    }
}
