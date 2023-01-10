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
import java.util.*

/**
 * Represents the base class for implementing string value settings.
 *
 * @property property The property of the setting.
 * @property normalizedProperty The normalized property of the setting, which is an upper-case property.
 * @property value The value of the setting.
 * @property normalizedValue The normalized value of the setting, which is an upper-case value.
 * @property hash The hash that uniquely identifies the setting.
 */
open class StringSetting(property: String, value: String) : Setting<String>(property, value) {

    companion object

    val normalizedValue: String
        get() = value.toUpperCase()

    override val hash: SecureHash
        get() = SecureHash.sha256("$normalizedProperty$normalizedValue")

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is StringSetting
                && other.javaClass == javaClass
                && normalizedProperty == other.normalizedProperty
                && normalizedValue == other.normalizedValue)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(normalizedProperty, normalizedValue)
    }
}
