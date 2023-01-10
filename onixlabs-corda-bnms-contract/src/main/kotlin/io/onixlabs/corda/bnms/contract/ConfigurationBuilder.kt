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

class ConfigurationBuilder(private val settings: MutableSet<Setting<*>>) {

    constructor(configuration: Configuration) : this(configuration.settings.toMutableSet())

    /**
     * Adds the specified setting to this configuration builder.
     *
     * @param property The property of the setting to add.
     * @param value The value of the setting to add.
     */
    @ConfigurationBuilderDslContext
    fun addSetting(property: String, value: Any) {
        addSettings(Setting(property, value))
    }

    /**
     * Adds the specified settings to this configuration builder.
     *
     * @param settings The settings to add to this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun addSettings(settings: Iterable<Setting<*>>) {
        this.settings += settings
    }

    /**
     * Adds the specified settings to this configuration builder.
     *
     * @param settings The settings to add to this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun addSettings(vararg settings: Setting<*>) {
        return addSettings(settings.toList())
    }

    /**
     * Removes the specified settings from this configuration builder.
     *
     * @param settings The settings to remove from this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun removeSettings(settings: Iterable<Setting<*>>) {
        this.settings -= settings
    }

    /**
     * Removes the specified settings from this configuration builder.
     *
     * @param settings The settings to remove from this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun removeSettings(vararg settings: Setting<*>) {
        return removeSettings(settings.toList())
    }

    /**
     * Adds the specified roles to this configuration builder.
     *
     * @param roles The roles to add to this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun addRoles(roles: Iterable<Role>) {
        this.settings += roles
    }

    /**
     * Adds the specified roles to this configuration builder.
     *
     * @param roles The roles to add to this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun addRoles(vararg roles: Role) {
        return addRoles(roles.toList())
    }

    /**
     * Adds the specified roles to this configuration builder.
     *
     * @param roles The roles to add to this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun addRoles(vararg roles: String) {
        return addRoles(roles.map { Role(it) })
    }

    /**
     * Removes the specified roles from this configuration builder.
     *
     * @param roles The roles to remove from this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun removeRoles(roles: Iterable<Role>) {
        this.settings -= roles
    }

    /**
     * Removes the specified roles from this configuration builder.
     *
     * @param roles The roles to remove from this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun removeRoles(vararg roles: Role) {
        return removeRoles(roles.toList())
    }

    /**
     * Removes the specified roles from this configuration builder.
     *
     * @param roles The roles to remove from this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun removeRoles(vararg roles: String) {
        return removeRoles(roles.map { Role(it) })
    }

    /**
     * Adds the specified permissions to this configuration builder.
     *
     * @param permissions The permissions to add to this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun addPermissions(permissions: Iterable<Permission>) {
        this.settings += permissions
    }

    /**
     * Adds the specified permissions to this configuration builder.
     *
     * @param permissions The permissions to add to this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun addPermissions(vararg permissions: Permission) {
        return addPermissions(permissions.toList())
    }

    /**
     * Adds the specified permissions to this configuration builder.
     *
     * @param permissions The permissions to add to this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun addPermissions(vararg permissions: String) {
        return addPermissions(permissions.map { Permission(it) })
    }

    /**
     * Removes the specified permissions from this configuration builder.
     *
     * @param permissions The permissions to remove from this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun removePermissions(permissions: Iterable<Permission>) {
        this.settings -= permissions
    }

    /**
     * Removes the specified permissions from this configuration builder.
     *
     * @param permissions The permissions to remove from this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun removePermissions(vararg permissions: Permission) {
        return removePermissions(permissions.toList())
    }

    /**
     * Removes the specified permissions from this configuration builder.
     *
     * @param permissions The permissions to remove from this configuration builder.
     */
    @ConfigurationBuilderDslContext
    fun removePermissions(vararg permissions: String) {
        return removePermissions(permissions.map { Permission(it) })
    }

    /**
     * Converts this configuration builder to a configuration.
     *
     * @return Returns a configuration containing the settings built by this configuration builder.
     */
    internal fun toConfiguration(): Configuration {
        return Configuration(settings)
    }
}
