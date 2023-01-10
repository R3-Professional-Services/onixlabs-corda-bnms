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

import io.onixlabs.corda.core.contract.Hashable
import io.onixlabs.corda.identityframework.contract.filterByProperty
import io.onixlabs.corda.identityframework.contract.filterByType
import net.corda.core.crypto.SecureHash
import net.corda.core.serialization.CordaSerializable

/**
 * Represents a configuration consisting of a set of settings.
 *
 * @property settings The set of settings in this configuration.
 * @property permissions The set of permissions in this configuration.
 * @property roles The set of roles in this configuration.
 * @property hash The unique hash of all settings in this configuration.
 */
@CordaSerializable
data class Configuration(val settings: Set<Setting<*>> = emptySet()) : Hashable {

    companion object

    val permissions: Set<Permission>
        get() = getSettingsByType()

    val roles: Set<Role>
        get() = getSettingsByType()

    override val hash: SecureHash
        get() = computeHash()

    /**
     * Gets all settings by type and optionally by property.
     *
     * @param V The underlying setting value type.
     * @param S the underlying setting type.
     * @param settingType The setting type.
     * @param valueType The setting's value type.
     * @param property The setting's property.
     * @param ignoreCase Determines whether to ignore case when filtering by property.
     * @return Returns all settings matching the specified type and property.
     */
    fun <V, S : Setting<in V>> getSettingsByType(
        settingType: Class<S>,
        valueType: Class<V>? = null,
        property: String? = null,
        ignoreCase: Boolean = false
    ): Set<S> {
        val resultsFilteredByType = settings.filterByType(settingType, valueType)
        return if (property != null) resultsFilteredByType.filterByProperty(property, ignoreCase).toSet()
        else resultsFilteredByType.toSet()
    }

    /**
     * Gets all settings by type and optionally by property.
     *
     * @param T The underlying [Setting] type.
     * @param property The setting's property.
     * @param ignoreCase Determines whether to ignore case when filtering by property.
     * @return Returns all settings matching the specified type and property.
     */
    inline fun <reified T : Setting<*>> getSettingsByType(
        property: String? = null,
        ignoreCase: Boolean = false
    ): Set<T> {
        val resultsFilteredByType = settings.filterByType<T>()
        return if (property != null) resultsFilteredByType.filterByProperty(property, ignoreCase).toSet()
        else resultsFilteredByType.toSet()
    }

    /**
     * Gets a single setting by type and optionally by property.
     *
     * @param T The underlying [Setting] type.
     * @param settingType The setting type.
     * @param valueType The setting's value type.
     * @param property The setting's property.
     * @param ignoreCase Determines whether to ignore case when filtering by property.
     * @return Returns a single setting matching the specified type and property.
     */
    fun <V, S : Setting<in V>> getSettingByType(
        settingType: Class<S>,
        valueType: Class<V>? = null,
        property: String? = null,
        ignoreCase: Boolean = false
    ): S = getSettingsByType(settingType, valueType, property, ignoreCase).single()

    /**
     * Gets a single setting by type and optionally by property.
     *
     * @param T The underlying [Setting] type.
     * @param property The setting's property.
     * @param ignoreCase Determines whether to ignore case when filtering by property.
     * @return Returns a single setting matching the specified type and property.
     */
    inline fun <reified T : Setting<*>> getSettingByType(
        property: String? = null,
        ignoreCase: Boolean = false
    ): T = getSettingsByType<T>(property, ignoreCase).single()

    /**
     * Gets a single setting by type and optionally by property, or null if no matching setting exists.
     *
     * @param T The underlying [Setting] type.
     * @param settingType The setting type.
     * @param valueType The setting's value type.
     * @param property The setting's property.
     * @param ignoreCase Determines whether to ignore case when filtering by property.
     * @return Returns a single setting matching the specified type and property, or null if no matching setting exists.
     */
    fun <V, S : Setting<in V>> getSettingByTypeOrNull(
        settingType: Class<S>,
        valueType: Class<V>? = null,
        property: String? = null,
        ignoreCase: Boolean = false
    ): S? = getSettingsByType(settingType, valueType, property, ignoreCase).singleOrNull()

    /**
     * Gets a single setting by type and optionally by property, or null if no matching setting exists.
     *
     * @param T The underlying [Setting] type.
     * @param property The setting's property.
     * @param ignoreCase Determines whether to ignore case when filtering by property.
     * @return Returns a single setting matching the specified type and property, or null if no matching setting exists.
     */
    inline fun <reified T : Setting<*>> getSettingByTypeOrNull(
        property: String? = null,
        ignoreCase: Boolean = false
    ): T? = getSettingsByType<T>(property, ignoreCase).singleOrNull()

    /**
     * Determines whether the specified setting is contained in this configuration.
     *
     * @param setting The setting to find in this configuration.
     * @return Returns true if the specified setting is contained in this configuration; otherwise, false.
     */
    fun hasSetting(setting: Setting<*>): Boolean {
        return setting in settings
    }

    /**
     * Determines whether the specified setting is contained in this configuration.
     *
     * @param property The setting property to find in this configuration.
     * @param ignoreCase Determines whether to ignore case when filtering by property.
     * @return Returns true if the specified setting is contained in this configuration; otherwise, false.
     */
    fun hasSetting(property: String, ignoreCase: Boolean = false): Boolean {
        return settings.any { it.property.equals(property, ignoreCase) }
    }

    /**
     * Determines whether the specified role is contained in this configuration.
     *
     * @param role The role to find in this configuration.
     * @return Returns true if the specified role is contained in this configuration; otherwise, false.
     */
    fun hasRole(role: Role): Boolean {
        return hasSetting(role)
    }

    /**
     * Determines whether the specified role is contained in this configuration.
     *
     * @param role The role to find in this configuration.
     * @return Returns true if the specified role is contained in this configuration; otherwise, false.
     */
    fun hasRole(role: String): Boolean {
        return roles.any { it.normalizedValue == role.toUpperCase() }
    }

    /**
     * Determines whether the specified permission is contained in this configuration.
     *
     * @param permission The permission to find in this configuration.
     * @return Returns true if the specified permission is contained in this configuration; otherwise, false.
     */
    fun hasPermission(permission: Permission): Boolean {
        return hasSetting(permission)
    }

    /**
     * Determines whether the specified permission is contained in this configuration.
     *
     * @param permission The permission to find in this configuration.
     * @return Returns true if the specified permission is contained in this configuration; otherwise, false.
     */
    fun hasPermission(permission: String): Boolean {
        return permissions.any { it.normalizedValue == permission.toUpperCase() }
    }

    /**
     * Adds the specified settings to this configuration.
     *
     * @param settings The settings to add to this configuration.
     * @return Returns a new configuration containing the existing and new settings.
     */
    fun addSettings(settings: Iterable<Setting<*>>): Configuration {
        return copy(settings = this.settings + settings)
    }

    /**
     * Adds the specified settings to this configuration.
     *
     * @param settings The settings to add to this configuration.
     * @return Returns a new configuration with the specified settings added.
     */
    fun addSettings(vararg settings: Setting<*>): Configuration {
        return addSettings(settings.toList())
    }

    /**
     * Removes the specified settings from this configuration.
     *
     * @param settings The settings to remove from this configuration.
     * @return Returns a new configuration with the specified settings removed.
     */
    fun removeSettings(settings: Iterable<Setting<*>>): Configuration {
        return copy(settings = this.settings - settings)
    }

    /**
     * Removes the specified settings from this configuration.
     *
     * @param settings The settings to remove from this configuration.
     * @return Returns a new configuration with the specified settings removed.
     */
    fun removeSettings(vararg settings: Setting<*>): Configuration {
        return removeSettings(settings.toList())
    }

    /**
     * Adds the specified roles to this configuration.
     *
     * @param roles The roles to add to this configuration.
     * @return Returns a new configuration with the specified roles added.
     */
    fun addRoles(roles: Iterable<Role>): Configuration {
        return copy(settings = this.settings + roles)
    }

    /**
     * Adds the specified roles to this configuration.
     *
     * @param roles The roles to add to this configuration.
     * @return Returns a new configuration with the specified roles added.
     */
    fun addRoles(vararg roles: Role): Configuration {
        return addRoles(roles.toList())
    }

    /**
     * Removes the specified roles from this configuration.
     *
     * @param roles The roles to remove from this configuration.
     * @return Returns a new configuration with the specified roles removed.
     */
    fun removeRoles(roles: Iterable<Role>): Configuration {
        return copy(settings = this.settings - roles)
    }

    /**
     * Removes the specified roles from this configuration.
     *
     * @param roles The roles to remove from this configuration.
     * @return Returns a new configuration with the specified roles removed.
     */
    fun removeRoles(vararg roles: Role): Configuration {
        return removeRoles(roles.toList())
    }

    /**
     * Adds the specified permissions to this configuration.
     *
     * @param permissions The permissions to add to this configuration.
     * @return Returns a new configuration with the specified permissions added.
     */
    fun addPermissions(permissions: Iterable<Permission>): Configuration {
        return copy(settings = this.settings + permissions)
    }

    /**
     * Adds the specified permissions to this configuration.
     *
     * @param permissions The permissions to add to this configuration.
     * @return Returns a new configuration with the specified permissions added.
     */
    fun addPermissions(vararg permissions: Permission): Configuration {
        return addPermissions(permissions.toList())
    }

    /**
     * Removes the specified permissions from this configuration.
     *
     * @param permissions The permissions to remove from this configuration.
     * @return Returns a new configuration with the specified permissions removed.
     */
    fun removePermissions(permissions: Iterable<Permission>): Configuration {
        return copy(settings = this.settings - permissions)
    }

    /**
     * Removes the specified permissions from this configuration.
     *
     * @param permissions The permissions to remove from this configuration.
     * @return Returns a new configuration with the specified permissions removed.
     */
    fun removePermissions(vararg permissions: Permission): Configuration {
        return removePermissions(permissions.toList())
    }

    /**
     * Computes the hash of all settings.
     */
    private fun computeHash(): SecureHash {
        return if (settings.count() == 0) return SecureHash.zeroHash
        else settings.map { it.hash }.sorted().reduce { lhs, rhs -> SecureHash.sha256("$lhs$rhs") }
    }
}
