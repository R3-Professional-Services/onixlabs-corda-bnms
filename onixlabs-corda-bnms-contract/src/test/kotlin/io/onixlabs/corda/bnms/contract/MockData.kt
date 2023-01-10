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

package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.dsl.LedgerDSL
import net.corda.testing.dsl.TestLedgerDSLInterpreter
import net.corda.testing.dsl.TestTransactionDSLInterpreter
import net.corda.testing.internal.vault.DummyLinearContract
import java.util.*

typealias MockLedger = LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>

val EMPTY_REF = StateRef(SecureHash.zeroHash, 0)
val EMPTY_GUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

val IDENTITY_A = TestIdentity(CordaX500Name("PartyA", "London", "GB"))
val IDENTITY_B = TestIdentity(CordaX500Name("PartyB", "New York", "US"))
val IDENTITY_C = TestIdentity(CordaX500Name("PartyC", "Paris", "FR"))
val OPERATOR_A = TestIdentity(CordaX500Name("Operator", "London", "GB"))

val CENTRALIZED_NETWORK = Network("Centralized Network", OPERATOR_A.party)
val DECENTRALIZED_NETWORK = Network("Decentralized Network")
val INVALID_NETWORK = Network("Invalid Network")

val CENTRALIZED_MEMBERSHIP_IDENTITY_A = Membership(CENTRALIZED_NETWORK, IDENTITY_A.party)
val CENTRALIZED_MEMBERSHIP_IDENTITY_B = Membership(CENTRALIZED_NETWORK, IDENTITY_B.party)
val CENTRALIZED_MEMBERSHIP_IDENTITY_C = Membership(CENTRALIZED_NETWORK, IDENTITY_C.party)
val CENTRALIZED_MEMBERSHIP_OPERATOR_A = Membership(CENTRALIZED_NETWORK, OPERATOR_A.party)

val DECENTRALIZED_MEMBERSHIP_IDENTITY_A = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party)
val DECENTRALIZED_MEMBERSHIP_IDENTITY_B = Membership(DECENTRALIZED_NETWORK, IDENTITY_B.party)
val DECENTRALIZED_MEMBERSHIP_IDENTITY_C = Membership(DECENTRALIZED_NETWORK, IDENTITY_C.party)

val MEMBERS = mapOf(IDENTITY_A.party to Configuration(), IDENTITY_B.party to Configuration())
val RELATIONSHIP = Relationship(CENTRALIZED_NETWORK, MEMBERS)

val REVOCATION_LOCK = RevocationLock(IDENTITY_A.party, DummyLinearContract.State())