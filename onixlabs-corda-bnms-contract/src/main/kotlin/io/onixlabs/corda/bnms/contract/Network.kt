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

import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty

/**
 * Represents a reference to a business network.
 *
 * @property property The property of the network setting, which is always "Network".
 * @property normalizedProperty The normalized property of the network setting, which is always "NETWORK".
 * @property value The value of the network, which is usually the name of the business network.
 * @property normalizedValue The normalized value of the network.
 * @property operator The business network operator, or null if the business network is decentralized.
 * @property hash The hash that uniquely identifies the network reference.
 */
class Network(value: String, val operator: AbstractParty? = null) : StringSetting(NETWORK, value) {

    companion object;

    override val hash: SecureHash
        get() = SecureHash.sha256("${super.hash}$operator")
}
