package io.onixlabs.corda.bnms.contract.membership.general

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.membership.Membership
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MembershipSettingTests {

    @Test
    fun `Membership hasSetting should return true when the expected setting is present`() {

        // Arrange
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).configure { addSetting("Test", 123) }

        // Act
        val result = membership.configuration.hasSetting(Setting("Test", 123))

        // Assert
        assertTrue(result)
    }

    @Test
    fun `Membership hasSetting should return true when the expected setting by property is present`() {

        // Arrange
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).configure { addSetting("Test", 123) }

        // Act
        val result = membership.configuration.hasSetting("Test")

        // Assert
        assertTrue(result)
    }

    @Test
    fun `Membership getSettingsByType should return all settings matching a particular type`() {

        // Arrange
        val settings = setOf(
            Network("Example Network"),
            Setting("Key1", 123),
            Setting("Key2", 456),
            Setting("Key1", true),
            Setting("Key2", "Example Setting"),
            Role("User"),
            Role("Admin"),
            Permission("Example Permission 1"),
            Permission("Example Permission 2")
        )
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).configure { addSettings(settings) }

        // Act
        val result = membership.configuration.getSettingsByType<Role>()

        // Assert
        assertEquals(2, result.size)
    }

    @Test
    fun `Membership getSettingsByType should return all settings matching a particular type and key`() {

        // Arrange
        val settings = setOf(
            Network("Example Network"),
            Setting("Key1", 123),
            Setting("Key2", 456),
            Setting("Key1", true),
            Setting("Key2", "Example Setting"),
            Role("User"),
            Role("Admin"),
            Permission("Example Permission 1"),
            Permission("Example Permission 2")
        )
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).configure { addSettings(settings) }

        // Act
        val result = membership.configuration.getSettingsByType<Setting<*>>("Key1")

        // Assert
        assertEquals(2, result.size)
    }

    @Test
    fun `Membership getSettingsByType should return all settings matching a particular value type`() {

        // Arrange
        val settings = setOf(
            Network("Example Network"),
            Setting("Key1", 123),
            Setting("Key2", 456),
            Setting("Key1", true),
            Setting("Key2", "Example Setting"),
            Role("User"),
            Role("Admin"),
            Permission("Example Permission 1"),
            Permission("Example Permission 2")
        )
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).configure { addSettings(settings) }

        // Act
        val result = membership.configuration.getSettingsByType<Setting<Int>>()

        // Assert
        assertEquals(2, result.size)
    }

    @Test
    fun `Membership getSettingsByType should return all settings matching a particular value type and key`() {

        // Arrange
        val settings = setOf(
            Network("Example Network"),
            Setting("Key1", 123),
            Setting("Key2", 456),
            Setting("Key1", true),
            Setting("Key2", "Example Setting"),
            Role("User"),
            Role("Admin"),
            Permission("Example Permission 1"),
            Permission("Example Permission 2")
        )
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).configure { addSettings(settings) }

        // Act
        val result = membership.configuration.getSettingsByType<Setting<*>>("Key1")

        // Assert
        assertEquals(2, result.size)
    }
}
